-- Add email verification column to usuario
ALTER TABLE usuario ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Create email verification token table
CREATE TABLE email_verification_token (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_token_token ON email_verification_token(token);
CREATE INDEX idx_email_token_usuario ON email_verification_token(usuario_id);
