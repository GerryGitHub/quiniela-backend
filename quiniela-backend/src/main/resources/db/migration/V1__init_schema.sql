-- 1. Tabla Usuario
CREATE TABLE usuario (
                         id BIGSERIAL PRIMARY KEY,
                         nombre VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         password VARCHAR(255) NOT NULL DEFAULT '',
                         rol VARCHAR(20) NOT NULL DEFAULT 'USER'
);

-- 2. Tabla Grupo (Catálogo de Grupos/Fases)
CREATE TABLE grupo (
                       id BIGSERIAL PRIMARY KEY,
                       nombre VARCHAR(100) NOT NULL
);

-- 3. Tabla Equipo (Catálogo de Equipos)
CREATE TABLE equipo (
                        id BIGSERIAL PRIMARY KEY,
                        nombre VARCHAR(255) NOT NULL,
                        bandera_url VARCHAR(500), -- Opcional: para escudos o banderas
                        grupo_id BIGINT REFERENCES grupo(id)
);

-- 4. Tabla Quiniela
CREATE TABLE quiniela (
                          id BIGSERIAL PRIMARY KEY,
                          nombre VARCHAR(255) NOT NULL,
                          codigo_invitacion VARCHAR(255) NOT NULL,
                          administrador_id BIGINT NOT NULL REFERENCES usuario(id)
);

-- 5. Tabla Partido (RELACIONADA POR IDs)
CREATE TABLE partido (
                         id BIGSERIAL PRIMARY KEY,
                         equipo_local_id BIGINT NOT NULL REFERENCES equipo(id),
                         equipo_visitante_id BIGINT NOT NULL REFERENCES equipo(id),
                         grupo_id BIGINT NOT NULL REFERENCES grupo(id), -- Relación formal al grupo
                         fecha_hora TIMESTAMP NOT NULL,
                         goles_local_real INT,
                         goles_visitante_real INT,
                         estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
);

-- 6. Tabla Participacion
CREATE TABLE participacion (
                               id BIGSERIAL PRIMARY KEY,
                               usuario_id BIGINT NOT NULL REFERENCES usuario(id),
                               quiniela_id BIGINT NOT NULL REFERENCES quiniela(id),
                               puntos_totales INT NOT NULL DEFAULT 0,
                               UNIQUE(usuario_id, quiniela_id)
);

-- 7. Tabla Pronostico
CREATE TABLE pronostico (
                            id BIGSERIAL PRIMARY KEY,
                            participacion_id BIGINT NOT NULL REFERENCES participacion(id),
                            partido_id BIGINT NOT NULL REFERENCES partido(id),
                            goles_local_predicho INT NOT NULL,
                            goles_visitante_predicho INT NOT NULL,
                            puntos_obtenidos INT NOT NULL DEFAULT 0,
                            UNIQUE(participacion_id, partido_id)
);

-- Índices optimizados
CREATE INDEX idx_quiniela_codigo ON quiniela(codigo_invitacion);
CREATE INDEX idx_partido_fecha ON partido(fecha_hora);
CREATE INDEX idx_partido_estado ON partido(estado);
CREATE INDEX idx_partido_equipo_local ON partido(equipo_local_id);
CREATE INDEX idx_partido_equipo_visitante ON partido(equipo_visitante_id);
CREATE INDEX idx_pronostico_partido ON pronostico(partido_id);