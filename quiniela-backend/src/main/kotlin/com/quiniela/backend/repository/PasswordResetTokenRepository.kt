package com.quiniela.backend.repository

import com.quiniela.backend.entity.PasswordResetToken
import com.quiniela.backend.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: String): Optional<PasswordResetToken>
    fun findByUsuario(usuario: Usuario): List<PasswordResetToken>
    fun findByUsuarioAndToken(usuario: Usuario, token: String): Optional<PasswordResetToken>
    fun deleteByUsuario(usuario: Usuario)
}
