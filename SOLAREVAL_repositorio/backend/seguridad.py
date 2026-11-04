import jwt
from fastapi import Depends, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from tokens import leer_token

esquema = HTTPBearer()


def usuario_actual(cred: HTTPAuthorizationCredentials = Depends(esquema)) -> dict:
    try:
        return leer_token(cred.credentials)
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Sesión inválida o expirada")


def requiere_rol(*roles: str):
    def dependencia(u: dict = Depends(usuario_actual)) -> dict:
        if u["rol"] not in roles:
            raise HTTPException(status_code=403, detail="No tienes permiso para esta acción")
        return u
    return dependencia
