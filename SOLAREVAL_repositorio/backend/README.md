# SOLAREVAL – Backend (FastAPI + PostgreSQL)

Mismo patrón del backend de ejemplo del profe (`bakend_prueba`): FastAPI, `psycopg2` con pool de conexiones (`PooledDB`), SQL directo y variables en `.env`. La app Android lo consume con Retrofit.

## Cómo correrlo

1. Crea la base de datos en PostgreSQL: `CREATE DATABASE solareval;`
2. Instala dependencias:
   ```
   python -m venv venv
   venv\Scripts\activate        (Windows)   |   source venv/bin/activate   (Linux/Mac)
   pip install -r requirements.txt
   ```
3. Copia `.env.example` como `.env` y llena tus datos. **Las variables se llaman `DB_*`** (en el ejemplo del profe eran `HOST`, `USER`, etc.; `USER` choca con una variable del sistema en Linux, y eso rompería el despliegue).
4. Crea las tablas y los datos iniciales: `psql -U postgres -d solareval -f schema.sql`
5. Crea las cuentas de prueba: `python crear_demo.py`
6. Arranca el servidor: `uvicorn main:app --host 0.0.0.0 --reload`
7. Abre `http://127.0.0.1:8000/docs`: ahí puedes probar todos los endpoints.

Prueba la lógica sin base de datos: `python pruebas_logica.py`

## Cuentas de prueba (ficticias)

| Rol | Correo | Contraseña |
|---|---|---|
| PyME | pyme@demo.com | Demo1234 |
| Instalador | instalador@demo.com | Demo1234 |
| Administrador | admin@demo.com | Demo1234 |

## Conectar la app Android

- Emulador: `http://10.0.2.2:8000/`. Teléfono físico: la IP de tu computadora en la misma red Wi-Fi.
- Con `http` (sin certificado) hay que permitir tráfico sin cifrar solo en desarrollo (`android:usesCleartextTraffic="true"` en el manifest). En la nube usa `https`.
- Después de `/auth/login`, manda el token en cada petición: `Authorization: Bearer <access_token>`.

## Endpoints

| Método y ruta | Rol | Qué hace | RF |
|---|---|---|---|
| POST /auth/registro | todos | Crea cuenta (PyME o instalador) | RF-01 |
| POST /auth/login | todos | Devuelve el token, el rol y el nombre | RF-02, RF-03 |
| GET /auth/yo | todos | Datos de la sesión | RF-03 |
| GET /tarifas | todos | Tarifas CFE de la tabla | RF-10 |
| POST /reportes | pyme | Recibe los datos leídos del recibo y la ubicación, consulta el potencial solar, calcula y guarda el reporte con su recomendación | RF-04 a RF-13 |
| GET /reportes, GET /reportes/{id} | pyme | Historial y detalle | RF-17 |
| GET /instaladores | pyme | Instaladores verificados | RF-19 |
| POST /solicitudes | pyme | Envía el reporte a uno o más instaladores (requiere autorización) | RF-19 |
| GET /solicitudes, GET /solicitudes/{id} | instalador | Bandeja y detalle (el contacto se muestra solo al aceptar) | RF-20 |
| PATCH /solicitudes/{id}/estado | instalador | Aceptar o rechazar; al aceptar registra la comisión | RF-20, RF-21 |
| GET /admin/usuarios, PATCH /admin/instaladores/{id}/verificar | admin | Gestión y verificación de instaladores | RF-22 |
| PATCH /admin/parametros/{clave}, PUT /admin/tarifas/{clave} | admin | Parámetros y tarifas | RF-23 |

Ejemplo de cuerpo de `POST /reportes` (lo que manda la app tras leer el recibo con ML Kit y confirmar el usuario):

```json
{"consumo_kwh": 1850, "tarifa": "PDBT", "importe": 7420, "periodo": "bimestral",
 "latitud": 20.45, "longitud": -101.53, "direccion": "Calle y número, ciudad", "consentimiento": true}
```

## Supuestos que debes revisar

- **Todos los parámetros de `schema.sql` son supuestos editables** (costo por kW, factor DC a AC, umbrales de la recomendación, comisión, y las tarifas de ejemplo `PDBT` y `GDBT`). Reemplaza las tarifas con las vigentes de CFE y declara los supuestos en el reporte y en la defensa.
- **Solar API:** con `SOLAR_API_KEY` llena y `MODO_DEMO=false` consulta a Google; si la ubicación no tiene datos, responde 404 con un mensaje claro (RF-13). Sin llave, o con `MODO_DEMO=true`, usa una estimación de demostración con parámetros editables; el reporte marca `fuente_solar: "demo"`. Si la usas en la defensa, dilo abiertamente.
- **Privacidad (RNF-05):** el backend no recibe ni guarda la foto del recibo, solo los valores leídos.
- **Comisión:** se registra en `comisiones` al aceptar la solicitud; no hay cobro en línea.
- No subas `.env` al repositorio.
