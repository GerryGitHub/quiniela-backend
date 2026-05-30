ALTER TABLE quiniela ADD COLUMN created_at TIMESTAMP;
UPDATE quiniela SET created_at = NOW() WHERE created_at IS NULL;
