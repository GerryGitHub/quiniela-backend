CREATE TABLE equipo_estadisticas (
    id BIGSERIAL PRIMARY KEY,
    equipo_id BIGINT NOT NULL REFERENCES equipo(id) UNIQUE,
    ranking_fifa INT,
    puntos_fair_play INT NOT NULL DEFAULT 0
);
