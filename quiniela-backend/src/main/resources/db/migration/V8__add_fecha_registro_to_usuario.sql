ALTER TABLE usuario ADD COLUMN fecha_registro TIMESTAMP;
UPDATE usuario SET fecha_registro = NOW() WHERE fecha_registro IS NULL;
