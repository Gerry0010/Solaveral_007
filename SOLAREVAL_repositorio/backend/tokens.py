"""Contraseñas y tokens (sin dependencias de FastAPI, para poder probarlo solo)."""
import hashlib
import hmac
import os
import secrets
import time
import jwt
from dotenv import load_dotenv

load_dotenv()
SECRET_KEY = os.getenv("SECRET_KEY", "cambia-esto-en-produccion")
ALGORITMO = "HS256"
HORAS_SESION = 12
ITERACIONES = 200_000


def hash_password(password: str) -> str:
    salt = secrets.token_bytes(16)
    h = hashlib.pbkdf2_hmac("sha256", password.encode(), salt, ITERACIONES)
    return f"{salt.hex()}${h.hex()}"


def verificar_password(password: str, almacenado: str) -> bool:
    try:
        salt_hex, h_hex = almacenado.split("$")
        h = hashlib.pbkdf2_hmac("sha256", password.encode(), bytes.fromhex(salt_hex), ITERACIONES)
        return hmac.compare_digest(h.hex(), h_hex)
    except ValueError:
        return False


def crear_token(usuario_id: int, rol: str) -> str:
    payload = {"sub": str(usuario_id), "rol": rol, "exp": int(time.time()) + HORAS_SESION * 3600}
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITMO)


def leer_token(token: str) -> dict:
    """Devuelve {'id', 'rol'} o lanza jwt.PyJWTError si es inválido o expiró."""
    p = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITMO])
    return {"id": int(p["sub"]), "rol": p["rol"]}
