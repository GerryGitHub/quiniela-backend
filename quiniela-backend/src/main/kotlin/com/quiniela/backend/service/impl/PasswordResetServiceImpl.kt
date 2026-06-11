package com.quiniela.backend.service.impl

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.PasswordResetToken
import com.quiniela.backend.repository.PasswordResetTokenRepository
import com.quiniela.backend.repository.UsuarioRepository
import com.quiniela.backend.service.EmailService
import com.quiniela.backend.service.PasswordResetService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PasswordResetServiceImpl(
    private val usuarioRepository: UsuarioRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder
) : PasswordResetService {

    @Transactional
    override fun forgotPassword(request: ForgotPasswordRequest): MessageResponse {
        val usuario = usuarioRepository.findByEmail(request.email)

        if (usuario.isPresent && usuario.get().emailVerified) {
            val user = usuario.get()
            passwordResetTokenRepository.deleteByUsuario(user)

            val code = String.format("%06d", (100000..999999).random())
            val resetToken = PasswordResetToken(
                usuario = user,
                token = code,
                expiresAt = LocalDateTime.now().plusMinutes(15)
            )
            passwordResetTokenRepository.save(resetToken)

            emailService.sendPasswordResetEmail(
                email = user.email,
                nombre = user.nombre,
                token = code
            )
        }

        return MessageResponse(message = "Si existe una cuenta, enviamos instrucciones.")
    }

    @Transactional
    override fun resetPassword(request: ResetPasswordRequest): MessageResponse {
        val usuario = usuarioRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        val resetToken = passwordResetTokenRepository.findByUsuarioAndToken(usuario, request.code)
            .orElseThrow { IllegalArgumentException("Código inválido") }

        if (resetToken.used) {
            throw IllegalArgumentException("El código ya fue utilizado")
        }

        if (resetToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("El código ha expirado")
        }

        usuario.password = passwordEncoder.encode(request.newPassword)
        resetToken.used = true

        usuarioRepository.save(usuario)
        passwordResetTokenRepository.save(resetToken)

        return MessageResponse(message = "Contraseña actualizada correctamente.")
    }
}
