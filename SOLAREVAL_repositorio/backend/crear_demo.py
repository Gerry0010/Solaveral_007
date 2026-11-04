"""Crea las cuentas de prueba (ficticias). Uso: python crear_demo.py"""
from db import pool
from tokens import hash_password

PASSWORD = "Demo1234"
CUENTAS = [
    ("PyME Demo", "pyme@demo.com", "pyme", dict(razon_social="Papelería Demo", giro="Comercio"), True),
    ("Instalador Demo", "instalador@demo.com", "instalador", dict(empresa="Solar Demo", zona_cobertura="Guanajuato"), True),
    ("Administrador Demo", "admin@demo.com", "admin", {}, True),
]

conn = pool.connection()
cur = conn.cursor()
for nombre, correo, rol, extra, verificado in CUENTAS:
    cur.execute("SELECT 1 FROM usuarios WHERE correo = %s", (correo,))
    if cur.fetchone():
        print("Ya existe:", correo)
        continue
    cur.execute(
        """INSERT INTO usuarios (nombre, correo, password_hash, rol, razon_social, giro,
                                 empresa, zona_cobertura, verificado)
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)""",
        (nombre, correo, hash_password(PASSWORD), rol, extra.get("razon_social"), extra.get("giro"),
         extra.get("empresa"), extra.get("zona_cobertura"), verificado))
    print("Creada:", correo)
conn.commit()
conn.close()
print(f"Contraseña de las cuentas de prueba: {PASSWORD}")
