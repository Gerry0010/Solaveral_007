-- SOLAREVAL: esquema PostgreSQL (basado en el diagrama de clases del equipo)
-- Uso:  psql -U postgres -d solareval -f schema.sql

CREATE TABLE IF NOT EXISTS usuarios (
    id             SERIAL PRIMARY KEY,
    nombre         TEXT NOT NULL,
    correo         TEXT NOT NULL UNIQUE,
    password_hash  TEXT NOT NULL,
    rol            TEXT NOT NULL CHECK (rol IN ('pyme', 'instalador', 'admin')),
    razon_social   TEXT,            -- PyME
    giro           TEXT,            -- PyME
    empresa        TEXT,            -- Instalador
    zona_cobertura TEXT,            -- Instalador
    verificado     BOOLEAN NOT NULL DEFAULT FALSE,  -- RN-12: solo instaladores verificados reciben solicitudes
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- RNF-05: no se guarda la imagen del recibo, solo los valores leídos
CREATE TABLE IF NOT EXISTS recibos (
    id          SERIAL PRIMARY KEY,
    usuario_id  INT NOT NULL REFERENCES usuarios(id),
    consumo_kwh DOUBLE PRECISION NOT NULL CHECK (consumo_kwh > 0),
    tarifa      TEXT NOT NULL,
    importe     DOUBLE PRECISION NOT NULL CHECK (importe >= 0),
    periodo     TEXT NOT NULL CHECK (periodo IN ('mensual', 'bimestral')),
    creado_en   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS ubicaciones (
    id        SERIAL PRIMARY KEY,
    latitud   DOUBLE PRECISION NOT NULL CHECK (latitud BETWEEN -90 AND 90),
    longitud  DOUBLE PRECISION NOT NULL CHECK (longitud BETWEEN -180 AND 180),
    direccion TEXT
);

CREATE TABLE IF NOT EXISTS tarifas_cfe (
    clave      TEXT PRIMARY KEY,
    precio_kwh DOUBLE PRECISION NOT NULL CHECK (precio_kwh > 0)
);

CREATE TABLE IF NOT EXISTS parametros (
    clave TEXT PRIMARY KEY,
    valor DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS reportes (
    id                    SERIAL PRIMARY KEY,
    usuario_id            INT NOT NULL REFERENCES usuarios(id),
    recibo_id             INT NOT NULL REFERENCES recibos(id),
    ubicacion_id          INT NOT NULL REFERENCES ubicaciones(id),
    paneles               INT NOT NULL,
    potencia_kw           DOUBLE PRECISION NOT NULL,
    generacion_anual_kwh  DOUBLE PRECISION NOT NULL,
    cobertura             DOUBLE PRECISION NOT NULL,   -- fracción 0 a 1
    precio_kwh            DOUBLE PRECISION NOT NULL,
    precio_fuente         TEXT NOT NULL,               -- 'tabla_cfe' o 'recibo'
    ahorro_estimado       DOUBLE PRECISION NOT NULL,   -- por año
    inversion_referencial DOUBLE PRECISION NOT NULL,
    retorno_inversion     DOUBLE PRECISION,            -- años; NULL si no hay ahorro
    recomendacion         TEXT NOT NULL CHECK (recomendacion IN
        ('conviene_ahora', 'conviene_con_condiciones', 'no_conviene_todavia')),
    fuente_solar          TEXT NOT NULL CHECK (fuente_solar IN ('solar_api', 'demo')),
    creado_en             TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS solicitudes (
    id            SERIAL PRIMARY KEY,
    reporte_id    INT NOT NULL REFERENCES reportes(id),
    pyme_id       INT NOT NULL REFERENCES usuarios(id),
    instalador_id INT NOT NULL REFERENCES usuarios(id),
    estado        TEXT NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'aceptada', 'rechazada')),
    consentimiento BOOLEAN NOT NULL,
    creado_en     TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (reporte_id, instalador_id)
);

-- RF-21: se registra la comisión; el cobro en línea queda fuera de la versión evaluable
CREATE TABLE IF NOT EXISTS comisiones (
    id            SERIAL PRIMARY KEY,
    solicitud_id  INT NOT NULL UNIQUE REFERENCES solicitudes(id),
    instalador_id INT NOT NULL REFERENCES usuarios(id),
    monto         DOUBLE PRECISION NOT NULL,
    fecha_pago    TIMESTAMP,
    creado_en     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_reportes_usuario ON reportes(usuario_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_instalador ON solicitudes(instalador_id);

-- ---------------------------------------------------------------------------
-- DATOS INICIALES. Todos son SUPUESTOS EDITABLES, no cifras oficiales.
-- Reemplaza las tarifas con las vigentes de CFE y ajusta los parámetros.
-- ---------------------------------------------------------------------------
INSERT INTO tarifas_cfe (clave, precio_kwh) VALUES
    ('PDBT', 4.00),   -- EJEMPLO: reemplazar con la tarifa vigente de CFE
    ('GDBT', 3.00)    -- EJEMPLO: reemplazar con la tarifa vigente de CFE
ON CONFLICT (clave) DO NOTHING;

INSERT INTO parametros (clave, valor) VALUES
    ('costo_por_kw', 20000),            -- MXN por kW instalado (supuesto)
    ('derate', 0.85),                   -- factor de DC a AC (supuesto)
    ('umbral_ahora', 4),                -- años de retorno para "conviene ahora"
    ('umbral_condiciones', 7),          -- años de retorno para "conviene con condiciones"
    ('cobertura_minima_ahora', 0.5),    -- fracción mínima del consumo cubierta para "conviene ahora"
    ('comision_pct', 0.04),             -- comisión del instalador sobre la inversión (RN-09: 3 a 5 %)
    ('rendimiento_demo', 1600),         -- kWh por kW al año (DC), solo modo demostración (supuesto)
    ('panel_w_demo', 400),              -- potencia por panel en modo demostración (supuesto)
    ('paneles_max_demo', 40)            -- paneles máximos en modo demostración (supuesto)
ON CONFLICT (clave) DO NOTHING;
