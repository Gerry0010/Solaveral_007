from typing import Literal, Optional
from dotenv import load_dotenv
from fastapi import Depends, FastAPI, HTTPException
from pydantic import BaseModel, Field

load_dotenv()

import calculos
import solar
from db import get_db, transaccion
from seguridad import requiere_rol, usuario_actual
from tokens import crear_token, hash_password, verificar_password

app = FastAPI(title="API SOLAREVAL")

AVISO = "Estimación preliminar: no es cotización ni garantía de ahorro."


# ----------------------------------------------------------------- modelos
class RegistroIn(BaseModel):
    nombre: str = Field(min_length=2)
    correo: str = Field(pattern=r"^[^@\s]+@[^@\s]+\.[^@\s]+$")
    password: str = Field(min_length=8)
    rol: Literal["pyme", "instalador"]
    razon_social: Optional[str] = None      # PyME
    giro: Optional[str] = None              # PyME
    empresa: Optional[str] = None           # Instalador
    zona_cobertura: Optional[str] = None    # Instalador


class LoginIn(BaseModel):
    correo: str
    password: str


class ReporteIn(BaseModel):
    consumo_kwh: float = Field(gt=0)        # consumo del periodo del recibo
    tarifa: str = Field(min_length=1)       # clave, por ejemplo PDBT
    importe: float = Field(ge=0)
    periodo: Literal["mensual", "bimestral"]
    latitud: float = Field(ge=-90, le=90)
    longitud: float = Field(ge=-180, le=180)
    direccion: Optional[str] = None
    consentimiento: bool                    # RF-04 / RN-01


class SolicitudIn(BaseModel):
    reporte_id: int
    instalador_ids: list[int] = Field(min_length=1)
    consentimiento: bool                    # RN-08


class EstadoIn(BaseModel):
    estado: Literal["aceptada", "rechazada"]


class VerificadoIn(BaseModel):
    verificado: bool


class ValorIn(BaseModel):
    valor: float


class TarifaIn(BaseModel):
    precio_kwh: float = Field(gt=0)


def cargar_parametros(cur) -> dict:
    cur.execute("SELECT clave, valor FROM parametros")
    return {r["clave"]: r["valor"] for r in cur.fetchall()}


# ------------------------------------------------------------------- inicio
@app.get("/")
def inicio():
    return {"mensaje": "API SOLAREVAL funcionando"}


# --------------------------------------------------------- auth (RF-01 a RF-03)
@app.post("/auth/registro", status_code=201)
def registro(d: RegistroIn, conn=Depends(get_db)):
    if d.rol == "pyme" and not (d.razon_social and d.giro):
        raise HTTPException(422, "La PyME debe indicar razón social y giro")
    if d.rol == "instalador" and not (d.empresa and d.zona_cobertura):
        raise HTTPException(422, "El instalador debe indicar empresa y zona de cobertura")
    with transaccion(conn) as cur:
        cur.execute("SELECT 1 FROM usuarios WHERE correo = %s", (d.correo.lower(),))
        if cur.fetchone():
            raise HTTPException(409, "Ya existe una cuenta con ese correo")
        cur.execute(
            """INSERT INTO usuarios (nombre, correo, password_hash, rol, razon_social, giro,
                                     empresa, zona_cobertura, verificado)
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
               RETURNING id, nombre, rol, verificado""",
            (d.nombre, d.correo.lower(), hash_password(d.password), d.rol, d.razon_social,
             d.giro, d.empresa, d.zona_cobertura, d.rol == "pyme"))
        return cur.fetchone()


@app.post("/auth/login")
def login(d: LoginIn, conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute("SELECT id, nombre, rol, password_hash, activo FROM usuarios WHERE correo = %s",
                    (d.correo.lower(),))
        u = cur.fetchone()
    if not u or not u["activo"] or not verificar_password(d.password, u["password_hash"]):
        raise HTTPException(401, "Correo o contraseña incorrectos")
    return {"access_token": crear_token(u["id"], u["rol"]), "token_type": "bearer",
            "rol": u["rol"], "nombre": u["nombre"]}


@app.get("/auth/yo")
def yo(u=Depends(usuario_actual), conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute("""SELECT id, nombre, correo, rol, razon_social, giro, empresa,
                              zona_cobertura, verificado FROM usuarios WHERE id = %s""", (u["id"],))
        return cur.fetchone()


# ------------------------------------------------------------ reportes (RF-04 a RF-13)
@app.get("/tarifas")
def listar_tarifas(u=Depends(usuario_actual), conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute("SELECT clave, precio_kwh FROM tarifas_cfe ORDER BY clave")
        return cur.fetchall()


def precio_por_kwh(cur, d: ReporteIn):
    """RF-10: precio de la tabla de tarifas CFE; si la clave no existe, importe entre kWh del recibo."""
    cur.execute("SELECT precio_kwh FROM tarifas_cfe WHERE clave = %s", (d.tarifa.strip().upper(),))
    fila = cur.fetchone()
    if fila:
        return float(fila["precio_kwh"]), "tabla_cfe"
    if d.importe <= 0:
        raise HTTPException(422, "La tarifa no está en la tabla y el importe del recibo es inválido")
    return d.importe / d.consumo_kwh, "recibo"


@app.post("/reportes", status_code=201)
def crear_reporte(d: ReporteIn, u=Depends(requiere_rol("pyme")), conn=Depends(get_db)):
    if not d.consentimiento:
        raise HTTPException(400, "Se requiere el consentimiento del aviso de privacidad")
    with transaccion(conn) as cur:
        p = cargar_parametros(cur)
        precio, precio_fuente = precio_por_kwh(cur, d)
        consumo = calculos.consumo_anual(d.consumo_kwh, d.periodo)
        try:
            potencial = solar.obtener_potencial(d.latitud, d.longitud, p)
        except solar.SolarError as e:
            raise HTTPException(e.codigo, str(e))          # RF-13
        cfg = calculos.elegir_configuracion(potencial["configs"], consumo, p["derate"])
        potencia_kw = cfg["panelsCount"] * potencial["panel_w"] / 1000
        generacion = cfg["yearlyEnergyDcKwh"] * p["derate"]
        r = calculos.calcular_reporte(
            consumo_kwh=d.consumo_kwh, periodo=d.periodo, precio_kwh=precio,
            potencia_kw=potencia_kw, generacion_anual_kwh=generacion,
            costo_por_kw=p["costo_por_kw"], umbral_ahora=p["umbral_ahora"],
            umbral_condiciones=p["umbral_condiciones"], cobertura_minima=p["cobertura_minima_ahora"])

        cur.execute("""INSERT INTO recibos (usuario_id, consumo_kwh, tarifa, importe, periodo)
                       VALUES (%s, %s, %s, %s, %s) RETURNING id""",
                    (u["id"], d.consumo_kwh, d.tarifa.strip().upper(), d.importe, d.periodo))
        recibo_id = cur.fetchone()["id"]
        cur.execute("INSERT INTO ubicaciones (latitud, longitud, direccion) VALUES (%s, %s, %s) RETURNING id",
                    (d.latitud, d.longitud, d.direccion))
        ubicacion_id = cur.fetchone()["id"]
        cur.execute(
            """INSERT INTO reportes (usuario_id, recibo_id, ubicacion_id, paneles, potencia_kw,
                   generacion_anual_kwh, cobertura, precio_kwh, precio_fuente, ahorro_estimado,
                   inversion_referencial, retorno_inversion, recomendacion, fuente_solar)
               VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s) RETURNING id, creado_en""",
            (u["id"], recibo_id, ubicacion_id, cfg["panelsCount"], round(potencia_kw, 2),
             r["generacion_anual_kwh"], r["cobertura"], round(precio, 4), precio_fuente,
             r["ahorro_estimado"], r["inversion_referencial"], r["retorno_inversion"],
             r["recomendacion"], potencial["fuente"]))
        fila = cur.fetchone()
    return {"id": fila["id"], "creado_en": fila["creado_en"], "paneles": cfg["panelsCount"],
            "potencia_kw": round(potencia_kw, 2), "precio_kwh": round(precio, 4),
            "precio_fuente": precio_fuente, "fuente_solar": potencial["fuente"],
            "supuestos": {"costo_por_kw": p["costo_por_kw"], "derate": p["derate"]},
            "aviso": AVISO, **r}


@app.get("/reportes")
def historial(u=Depends(requiere_rol("pyme")), conn=Depends(get_db)):      # RF-17
    with transaccion(conn) as cur:
        cur.execute(
            """SELECT r.id, r.creado_en, r.potencia_kw, r.ahorro_estimado, r.inversion_referencial,
                      r.retorno_inversion, r.recomendacion, r.fuente_solar, ub.direccion,
                      (SELECT s.estado FROM solicitudes s WHERE s.reporte_id = r.id
                        ORDER BY s.id DESC LIMIT 1) AS estado_solicitud
               FROM reportes r JOIN ubicaciones ub ON ub.id = r.ubicacion_id
               WHERE r.usuario_id = %s ORDER BY r.creado_en DESC""", (u["id"],))
        return cur.fetchall()


@app.get("/reportes/{reporte_id}")
def ver_reporte(reporte_id: int, u=Depends(requiere_rol("pyme")), conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute(
            """SELECT r.*, ub.direccion, ub.latitud, ub.longitud,
                      rc.consumo_kwh, rc.tarifa, rc.periodo
               FROM reportes r JOIN ubicaciones ub ON ub.id = r.ubicacion_id
                               JOIN recibos rc ON rc.id = r.recibo_id
               WHERE r.id = %s AND r.usuario_id = %s""", (reporte_id, u["id"]))   # RN-10
        fila = cur.fetchone()
        if not fila:
            raise HTTPException(404, "Reporte no encontrado")
        fila["aviso"] = AVISO
        return fila


# --------------------------------------------------- solicitudes (RF-19 a RF-21)
@app.get("/instaladores")
def listar_instaladores(u=Depends(requiere_rol("pyme")), conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute("""SELECT id, empresa, zona_cobertura FROM usuarios
                       WHERE rol = 'instalador' AND verificado AND activo ORDER BY empresa""")
        return cur.fetchall()


@app.post("/solicitudes", status_code=201)
def crear_solicitudes(d: SolicitudIn, u=Depends(requiere_rol("pyme")), conn=Depends(get_db)):
    if not d.consentimiento:
        raise HTTPException(400, "Se requiere tu autorización para compartir el reporte")
    with transaccion(conn) as cur:
        cur.execute("SELECT 1 FROM reportes WHERE id = %s AND usuario_id = %s", (d.reporte_id, u["id"]))
        if not cur.fetchone():
            raise HTTPException(404, "Reporte no encontrado")
        creadas = []
        for inst_id in d.instalador_ids:
            cur.execute("""SELECT 1 FROM usuarios WHERE id = %s AND rol = 'instalador'
                           AND verificado AND activo""", (inst_id,))
            if not cur.fetchone():
                raise HTTPException(400, f"El instalador {inst_id} no está disponible")   # RN-12
            cur.execute(
                """INSERT INTO solicitudes (reporte_id, pyme_id, instalador_id, consentimiento)
                   VALUES (%s, %s, %s, TRUE) ON CONFLICT (reporte_id, instalador_id) DO NOTHING
                   RETURNING id, instalador_id, estado""", (d.reporte_id, u["id"], inst_id))
            fila = cur.fetchone()
            if fila:
                creadas.append(fila)
        return {"solicitudes": creadas}


@app.get("/solicitudes")
def bandeja(u=Depends(requiere_rol("instalador")), conn=Depends(get_db)):   # RF-20
    with transaccion(conn) as cur:
        cur.execute(
            """SELECT s.id, s.estado, s.creado_en, p.razon_social AS negocio, p.giro,
                      ub.direccion, r.recomendacion
               FROM solicitudes s JOIN reportes r ON r.id = s.reporte_id
                                  JOIN usuarios p ON p.id = s.pyme_id
                                  JOIN ubicaciones ub ON ub.id = r.ubicacion_id
               WHERE s.instalador_id = %s ORDER BY s.creado_en DESC""", (u["id"],))     # RN-10
        return cur.fetchall()


def detalle_solicitud(cur, solicitud_id: int, instalador_id: int):
    cur.execute(
        """SELECT s.id, s.estado, s.creado_en, p.razon_social AS negocio, p.giro, ub.direccion,
                  r.potencia_kw, r.ahorro_estimado, r.inversion_referencial, r.retorno_inversion,
                  r.recomendacion, p.nombre AS contacto_nombre, p.correo AS contacto_correo
           FROM solicitudes s JOIN reportes r ON r.id = s.reporte_id
                              JOIN usuarios p ON p.id = s.pyme_id
                              JOIN ubicaciones ub ON ub.id = r.ubicacion_id
           WHERE s.id = %s AND s.instalador_id = %s""", (solicitud_id, instalador_id))
    fila = cur.fetchone()
    if not fila:
        raise HTTPException(404, "Solicitud no encontrada")
    if fila["estado"] != "aceptada":          # RN-08: el contacto solo se revela al aceptar
        fila["contacto_nombre"] = None
        fila["contacto_correo"] = None
    return fila


@app.get("/solicitudes/{solicitud_id}")
def ver_solicitud(solicitud_id: int, u=Depends(requiere_rol("instalador")), conn=Depends(get_db)):
    with transaccion(conn) as cur:
        return detalle_solicitud(cur, solicitud_id, u["id"])


@app.patch("/solicitudes/{solicitud_id}/estado")
def cambiar_estado(solicitud_id: int, d: EstadoIn, u=Depends(requiere_rol("instalador")),
                   conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute(
            """SELECT s.estado, r.inversion_referencial FROM solicitudes s
               JOIN reportes r ON r.id = s.reporte_id
               WHERE s.id = %s AND s.instalador_id = %s FOR UPDATE OF s""", (solicitud_id, u["id"]))
        fila = cur.fetchone()
        if not fila:
            raise HTTPException(404, "Solicitud no encontrada")
        if fila["estado"] != "pendiente":
            raise HTTPException(409, "La solicitud ya fue atendida")
        cur.execute("UPDATE solicitudes SET estado = %s WHERE id = %s", (d.estado, solicitud_id))
        if d.estado == "aceptada":              # RF-21: se registra la comisión (sin cobro en línea)
            pct = cargar_parametros(cur)["comision_pct"]
            cur.execute("""INSERT INTO comisiones (solicitud_id, instalador_id, monto)
                           VALUES (%s, %s, %s)""",
                        (solicitud_id, u["id"], round(fila["inversion_referencial"] * pct, 2)))
        return detalle_solicitud(cur, solicitud_id, u["id"])


# --------------------------------------------------------- administración (RF-22, RF-23)
@app.get("/admin/usuarios")
def admin_usuarios(u=Depends(requiere_rol("admin")), conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute("""SELECT id, nombre, correo, rol, verificado, activo FROM usuarios ORDER BY id""")
        return cur.fetchall()


@app.patch("/admin/instaladores/{usuario_id}/verificar")
def admin_verificar(usuario_id: int, d: VerificadoIn, u=Depends(requiere_rol("admin")),
                    conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute("""UPDATE usuarios SET verificado = %s WHERE id = %s AND rol = 'instalador'
                       RETURNING id, empresa, verificado""", (d.verificado, usuario_id))
        fila = cur.fetchone()
        if not fila:
            raise HTTPException(404, "Instalador no encontrado")
        return fila


@app.patch("/admin/parametros/{clave}")
def admin_parametro(clave: str, d: ValorIn, u=Depends(requiere_rol("admin")), conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute("UPDATE parametros SET valor = %s WHERE clave = %s RETURNING clave, valor",
                    (d.valor, clave))
        fila = cur.fetchone()
        if not fila:
            raise HTTPException(404, "Parámetro no encontrado")
        return fila


@app.put("/admin/tarifas/{clave}")
def admin_tarifa(clave: str, d: TarifaIn, u=Depends(requiere_rol("admin")), conn=Depends(get_db)):
    with transaccion(conn) as cur:
        cur.execute("""INSERT INTO tarifas_cfe (clave, precio_kwh) VALUES (%s, %s)
                       ON CONFLICT (clave) DO UPDATE SET precio_kwh = EXCLUDED.precio_kwh
                       RETURNING clave, precio_kwh""", (clave.strip().upper(), d.precio_kwh))
        return cur.fetchone()
