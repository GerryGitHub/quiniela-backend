package com.quiniela.backend.repository

import com.quiniela.backend.entity.RefreshToken
import com.quiniela.backend.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByToken(token: String): Optional<RefreshToken>
    fun findByUsuarioAndRevokedFalseAndExpiresAtAfter(usuario: Usuario, now: java.time.LocalDateTime): List<RefreshToken>
    fun deleteByUsuario(usuario: Usuario)
}
