import os
from contextlib import contextmanager
import psycopg2
from psycopg2.extras import RealDictCursor
from dbutils.pooled_db import PooledDB
from dotenv import load_dotenv
from fastapi import HTTPException

load_dotenv()

# Mismo patrón del backend de ejemplo del profe: un pool de conexiones a PostgreSQL.
# Las variables se llaman DB_* (y no USER) para que no choquen con variables del sistema.
pool = PooledDB(
    creator=psycopg2,
    maxconnections=10,
    mincached=2,
    maxcached=5,
    blocking=True,
    host=os.getenv("DB_HOST", "localhost"),
    port=int(os.getenv("DB_PORT", 5432)),
    user=os.getenv("DB_USER"),
    password=os.getenv("DB_PASSWORD"),
    database=os.getenv("DB_NAME"),
    sslmode=os.getenv("DB_SSLMODE", "prefer"),
    cursor_factory=RealDictCursor,
)


def get_db():
    """Dependencia de FastAPI: presta una conexión del pool y la devuelve al terminar."""
    conn = pool.connection()
    try:
        yield conn
    finally:
        conn.close()


@contextmanager
def transaccion(conn):
    """Abre un cursor, hace commit si todo sale bien y rollback si algo falla."""
    try:
        with conn.cursor() as cur:
            yield cur
        conn.commit()
    except HTTPException:
        conn.rollback()
        raise
    except Exception as e:
        conn.rollback()
        raise HTTPException(status_code=500, detail=f"Error en la base de datos: {e}")
