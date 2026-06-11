ALTER TABLE pronostico ADD COLUMN quiniela_id BIGINT REFERENCES quiniela(id);
CREATE INDEX idx_pronostico_quiniela ON pronostico(quiniela_id);
UPDATE pronostico p SET quiniela_id = pa.quiniela_id FROM participacion pa WHERE p.participacion_id = pa.id;
