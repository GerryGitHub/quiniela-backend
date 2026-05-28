package com.quiniela.backend.repository

import com.quiniela.backend.entity.EmailVerificationToken
import com.quiniela.backend.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    fun findByToken(token: String): Optional<EmailVerificationToken>
    fun findByUsuario(usuario: Usuario): List<EmailVerificationToken>
    fun findByUsuarioAndToken(usuario: Usuario, token: String): Optional<EmailVerificationToken>
    fun deleteByUsuario(usuario: Usuario)
}
