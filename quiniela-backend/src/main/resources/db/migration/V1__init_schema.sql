-- V1__init_schema.sql
-- Tabla Usuario
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL DEFAULT ''
);

-- Tabla Quiniela
CREATE TABLE quiniela (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    codigo_invitacion VARCHAR(255) NOT NULL,
    administrador_id BIGINT NOT NULL REFERENCES usuario(id)
);

-- Tabla Partido
CREATE TABLE partido (
    id BIGSERIAL PRIMARY KEY,
    equipo_local VARCHAR(255) NOT NULL,
    equipo_visitante VARCHAR(255) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL,
    goles_local_real INT,
    goles_visitante_real INT,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
);

-- Tabla Participacion
CREATE TABLE participacion (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id),
    quiniela_id BIGINT NOT NULL REFERENCES quiniela(id),
    puntos_totales INT NOT NULL DEFAULT 0,
    UNIQUE(usuario_id, quiniela_id)
);

-- Tabla Pronostico
CREATE TABLE pronostico (
    id BIGSERIAL PRIMARY KEY,
    participacion_id BIGINT NOT NULL REFERENCES participacion(id),
    partido_id BIGINT NOT NULL REFERENCES partido(id),
    goles_local_predicho INT NOT NULL,
    goles_visitante_predicho INT NOT NULL,
    puntos_obtenidos INT NOT NULL DEFAULT 0,
    UNIQUE(participacion_id, partido_id)
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_quiniela_codigo ON quiniela(codigo_invitacion);
CREATE INDEX idx_partido_fecha ON partido(fecha_hora);
CREATE INDEX idx_partido_estado ON partido(estado);
CREATE INDEX idx_pronostico_partido ON pronostico(partido_id);
