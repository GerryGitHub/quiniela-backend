-- Plantilla del bracket de eliminatorias
CREATE TABLE bracket_slot (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL,
    ronda VARCHAR(10) NOT NULL,
    orden INTEGER NOT NULL,
    local_tipo VARCHAR(10) NOT NULL,
    local_grupos VARCHAR(12),
    local_partido_origen_id BIGINT REFERENCES bracket_slot(id),
    local_es_ganador BOOLEAN,
    visitante_tipo VARCHAR(10) NOT NULL,
    visitante_grupos VARCHAR(12),
    visitante_partido_origen_id BIGINT REFERENCES bracket_slot(id),
    visitante_es_ganador BOOLEAN
);

-- Asignación de 3ros lugares según tabla FIFA
CREATE TABLE terceros_mapping (
    id BIGSERIAL PRIMARY KEY,
    combinacion VARCHAR(12) NOT NULL,
    slot_codigo VARCHAR(10) NOT NULL,
    grupo_origen VARCHAR(1) NOT NULL,
    UNIQUE(combinacion, slot_codigo)
);

-- Estado de quiniela (para saber si está activa o finalizada)
ALTER TABLE quiniela ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA';
ALTER TABLE quiniela ADD COLUMN ronda VARCHAR(20);

-- Partidos de eliminatorias: codigo (P73-P104), quiniela opcional, grupo opcional
ALTER TABLE partido ADD COLUMN codigo VARCHAR(10);
ALTER TABLE partido ADD COLUMN quiniela_id BIGINT REFERENCES quiniela(id);
ALTER TABLE partido ALTER COLUMN grupo_id DROP NOT NULL;

-- ===================== BRACKET SLOTS =====================
-- Dieciseisavos (R32)
INSERT INTO bracket_slot (codigo, ronda, orden, local_tipo, local_grupos, visitante_tipo, visitante_grupos) VALUES
('P73', 'R32', 1,  'GRUPO_2', 'A', 'GRUPO_2', 'B'),
('P74', 'R32', 2,  'GRUPO_1', 'E', 'GRUPO_3', 'ABCD'),
('P75', 'R32', 3,  'GRUPO_1', 'F', 'GRUPO_2', 'C'),
('P76', 'R32', 4,  'GRUPO_1', 'C', 'GRUPO_2', 'F'),
('P77', 'R32', 5,  'GRUPO_1', 'I', 'GRUPO_3', 'CDFGH'),
('P78', 'R32', 6,  'GRUPO_2', 'E', 'GRUPO_2', 'I'),
('P79', 'R32', 7,  'GRUPO_1', 'A', 'GRUPO_3', 'CEFHI'),
('P80', 'R32', 8,  'GRUPO_1', 'L', 'GRUPO_3', 'EHIJK'),
('P81', 'R32', 9,  'GRUPO_1', 'D', 'GRUPO_3', 'BEFIJ'),
('P82', 'R32', 10, 'GRUPO_1', 'G', 'GRUPO_3', 'AEHIJ'),
('P83', 'R32', 11, 'GRUPO_2', 'K', 'GRUPO_2', 'L'),
('P84', 'R32', 12, 'GRUPO_1', 'H', 'GRUPO_2', 'J'),
('P85', 'R32', 13, 'GRUPO_1', 'B', 'GRUPO_3', 'EFGIJ'),
('P86', 'R32', 14, 'GRUPO_1', 'J', 'GRUPO_2', 'H'),
('P87', 'R32', 15, 'GRUPO_1', 'K', 'GRUPO_3', 'DEIJL'),
('P88', 'R32', 16, 'GRUPO_2', 'D', 'GRUPO_2', 'G');

-- Octavos (R16) - referencias a ganadores de R32
INSERT INTO bracket_slot (codigo, ronda, orden, local_tipo, local_partido_origen_id, local_es_ganador, visitante_tipo, visitante_partido_origen_id, visitante_es_ganador) VALUES
('P89', 'R16', 1, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P74'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P77'), TRUE),
('P90', 'R16', 2, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P73'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P75'), TRUE),
('P91', 'R16', 3, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P76'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P78'), TRUE),
('P92', 'R16', 4, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P79'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P80'), TRUE),
('P93', 'R16', 5, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P83'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P84'), TRUE),
('P94', 'R16', 6, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P81'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P82'), TRUE),
('P95', 'R16', 7, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P86'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P88'), TRUE),
('P96', 'R16', 8, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P85'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P87'), TRUE);

-- Cuartos (QF)
INSERT INTO bracket_slot (codigo, ronda, orden, local_tipo, local_partido_origen_id, local_es_ganador, visitante_tipo, visitante_partido_origen_id, visitante_es_ganador) VALUES
('P97', 'QF', 1, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P89'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P90'), TRUE),
('P98', 'QF', 2, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P93'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P94'), TRUE),
('P99', 'QF', 3, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P91'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P92'), TRUE),
('P100', 'QF', 4, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P95'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P96'), TRUE);

-- Semifinales (SF)
INSERT INTO bracket_slot (codigo, ronda, orden, local_tipo, local_partido_origen_id, local_es_ganador, visitante_tipo, visitante_partido_origen_id, visitante_es_ganador) VALUES
('P101', 'SF', 1, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P97'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P98'), TRUE),
('P102', 'SF', 2, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P99'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P100'), TRUE);

-- Tercer lugar (3RD)
INSERT INTO bracket_slot (codigo, ronda, orden, local_tipo, local_partido_origen_id, local_es_ganador, visitante_tipo, visitante_partido_origen_id, visitante_es_ganador) VALUES
('P103', '3RD', 1, 'LOSER', (SELECT id FROM bracket_slot WHERE codigo='P101'), FALSE, 'LOSER', (SELECT id FROM bracket_slot WHERE codigo='P102'), FALSE);

-- Final (FINAL)
INSERT INTO bracket_slot (codigo, ronda, orden, local_tipo, local_partido_origen_id, local_es_ganador, visitante_tipo, visitante_partido_origen_id, visitante_es_ganador) VALUES
('P104', 'FINAL', 1, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P101'), TRUE, 'WINNER', (SELECT id FROM bracket_slot WHERE codigo='P102'), TRUE);

-- ===================== TERCEROS MAPPING =====================
-- Combinación: clasifican los 8 mejores 3ros de grupos A-H
INSERT INTO terceros_mapping (combinacion, slot_codigo, grupo_origen) VALUES
('ABCDEFGH', 'P74', 'A'),
('ABCDEFGH', 'P77', 'C'),
('ABCDEFGH', 'P79', 'E'),
('ABCDEFGH', 'P80', 'H'),
('ABCDEFGH', 'P81', 'B'),
('ABCDEFGH', 'P82', 'F'),
('ABCDEFGH', 'P85', 'G'),
('ABCDEFGH', 'P87', 'D');

-- Combinación: clasifican grupos A-F, H, I (G fuera)
INSERT INTO terceros_mapping (combinacion, slot_codigo, grupo_origen) VALUES
('ABCDFHI', 'P74', 'A'),
('ABCDFHI', 'P77', 'C'),
('ABCDFHI', 'P79', 'F'),
('ABCDFHI', 'P80', 'H'),
('ABCDFHI', 'P81', 'B'),
('ABCDFHI', 'P82', 'I'),
('ABCDFHI', 'P85', 'D'),
('ABCDFHI', 'P87', 'E');
