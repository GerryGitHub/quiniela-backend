-- Add indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_partido_fecha ON partido(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_partido_estado ON partido(estado);
CREATE INDEX IF NOT EXISTS idx_partido_grupo ON partido(grupo_id);
CREATE INDEX IF NOT EXISTS idx_participacion_usuario ON participacion(usuario_id);
CREATE INDEX IF NOT EXISTS idx_participacion_quiniela ON participacion(quiniela_id);
CREATE INDEX IF NOT EXISTS idx_pronostico_partido ON pronostico(partido_id);
CREATE INDEX IF NOT EXISTS idx_pronostico_participacion ON pronostico(participacion_id);
CREATE INDEX IF NOT EXISTS idx_usuario_email ON usuario(email);