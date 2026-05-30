package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EmailVerificationToken
import com.quiniela.backend.entity.PasswordResetToken
import com.quiniela.backend.entity.RefreshToken
import com.quiniela.backend.entity.Usuario
import com.quiniela.backend.exception.ForbiddenException
import com.quiniela.backend.repository.EmailVerificationTokenRepository
import com.quiniela.backend.repository.PasswordResetTokenRepository
import com.quiniela.backend.repository.ParticipacionRepository
import com.quiniela.backend.repository.QuinielaRepository
import com.quiniela.backend.repository.RefreshTokenRepository
import com.quiniela.backend.repository.UsuarioRepository
import com.quiniela.backend.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val participacionRepository: ParticipacionRepository,
    private val quinielaRepository: QuinielaRepository,
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    @Value("\${app.refresh-token-expiration-days:30}") private val refreshTokenExpirationDays: Long
) {

    @Transactional
    fun register(request: RegisterRequest): RegisterResponse {
        if (request.email.isBlank() || !request.email.contains("@")) {
            throw IllegalArgumentException("Email inválido")
        }
        if (request.password.length < 6) {
            throw IllegalArgumentException("La contraseña debe tener al menos 6 caracteres")
        }
        if (request.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre es requerido")
        }
        if (usuarioRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        val usuario = Usuario(
            nombre = request.nombre,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            emailVerified = false,
            fechaRegistro = LocalDateTime.now()
        )
        val usuarioGuardado = usuarioRepository.save(usuario)

        val code = String.format("%06d", (100000..999999).random())
        val verificationToken = EmailVerificationToken(
            usuario = usuarioGuardado,
            token = code,
            expiresAt = LocalDateTime.now().plusMinutes(15)
        )
        emailVerificationTokenRepository.save(verificationToken)

        emailService.sendVerificationEmail(
            email = usuarioGuardado.email,
            nombre = usuarioGuardado.nombre,
            token = code
        )

        return RegisterResponse(message = "Revisa tu correo para activar tu cuenta con el código OTP")
    }

    @Transactional
    fun verifyRegistrationOtp(request: VerifyRegistrationOtpRequest): MessageResponse {
        val usuario = usuarioRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        if (usuario.emailVerified) {
            throw IllegalArgumentException("La cuenta ya está verificada")
        }

        val verificationToken = emailVerificationTokenRepository.findByUsuarioAndToken(usuario, request.code)
            .orElseThrow { IllegalArgumentException("Código inválido") }

        if (verificationToken.used) {
            throw IllegalArgumentException("El código ya fue utilizado")
        }

        if (verificationToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("El código ha expirado")
        }

        usuario.emailVerified = true
        verificationToken.used = true

        usuarioRepository.save(usuario)
        emailVerificationTokenRepository.save(verificationToken)

        return MessageResponse(message = "Correo verificado correctamente")
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val usuario = usuarioRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        if (!usuario.emailVerified) {
            throw ForbiddenException("Debes verificar tu correo antes de iniciar sesión")
        }

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val userDetails = userDetailsService.loadUserByUsername(request.email)
        val accessToken = jwtService.generateToken(userDetails)

        val refreshTokenValue = UUID.randomUUID().toString()
        val refreshToken = RefreshToken(
            usuario = usuario,
            token = refreshTokenValue,
            expiresAt = LocalDateTime.now().plusDays(refreshTokenExpirationDays)
        )
        refreshTokenRepository.save(refreshToken)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshTokenValue,
            usuario = UsuarioDTO(
                id = usuario.id,
                nombre = usuario.nombre,
                email = usuario.email,
                rol = usuario.rol
            )
        )
    }

    @Transactional
    fun refreshAccessToken(request: RefreshTokenRequest): RefreshTokenResponse {
        val storedToken = refreshTokenRepository.findByToken(request.refreshToken)
            .orElseThrow { IllegalArgumentException("Refresh token inválido") }

        if (storedToken.revoked) {
            throw IllegalArgumentException("Refresh token ya fue utilizado")
        }

        if (storedToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("Refresh token expirado")
        }

        val usuario = storedToken.usuario
        val userDetails = userDetailsService.loadUserByUsername(usuario.email)
        val newAccessToken = jwtService.generateToken(userDetails)

        storedToken.revoked = true
        refreshTokenRepository.save(storedToken)

        val newRefreshTokenValue = UUID.randomUUID().toString()
        val newRefreshToken = RefreshToken(
            usuario = usuario,
            token = newRefreshTokenValue,
            expiresAt = LocalDateTime.now().plusDays(refreshTokenExpirationDays)
        )
        refreshTokenRepository.save(newRefreshToken)

        return RefreshTokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshTokenValue
        )
    }

    @Transactional
    fun resendVerification(request: ResendVerificationRequest): MessageResponse {
        val usuario = usuarioRepository.findByEmail(request.email)

        if (usuario.isPresent && !usuario.get().emailVerified) {
            val user = usuario.get()
            emailVerificationTokenRepository.deleteByUsuario(user)

            val newCode = String.format("%06d", (100000..999999).random())
            val verificationToken = EmailVerificationToken(
                usuario = user,
                token = newCode,
                expiresAt = LocalDateTime.now().plusMinutes(15)
            )
            emailVerificationTokenRepository.save(verificationToken)

            emailService.sendVerificationEmail(
                email = user.email,
                nombre = user.nombre,
                token = newCode
            )
        }

        return MessageResponse(message = "Si el correo está registrado y no verificado, recibirás un código de verificación")
    }

    @Transactional
    fun forgotPassword(request: ForgotPasswordRequest): MessageResponse {
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
    fun resetPassword(request: ResetPasswordRequest): MessageResponse {
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

    fun getPerfil(email: String): UsuarioPerfilDTO {
        val usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        val puntosGlobales = usuarioRepository.puntosTotalesGlobales(usuario.id)

        val participaciones = participacionRepository.findByUsuarioIdAndQuinielaId(usuario.id, 0L)
        val quinielas = mutableListOf<QuinielaResumenDTO>()

        val misQuinielasAdmin = quinielaRepository.findByAdministradorId(usuario.id)
        misQuinielasAdmin.forEach { q ->
            val participacion = participacionRepository.findByUsuarioIdAndQuinielaId(usuario.id, q.id)
            quinielas.add(
                QuinielaResumenDTO(
                    id = q.id,
                    nombre = q.nombre,
                    codigoInvitacion = q.codigoInvitacion,
                    puntosTotales = participacion.map { it.puntosTotales }.orElse(0)
                )
            )
        }

        val misQuinielas = quinielaRepository.findByParticipanteId(usuario.id)
        misQuinielas.forEach { q ->
            if (quinielas.none { it.id == q.id }) {
                val participacion = participacionRepository.findByUsuarioIdAndQuinielaId(usuario.id, q.id)
                quinielas.add(
                    QuinielaResumenDTO(
                        id = q.id,
                        nombre = q.nombre,
                        codigoInvitacion = q.codigoInvitacion,
                        puntosTotales = participacion.map { it.puntosTotales }.orElse(0)
                    )
                )
            }
        }

        return UsuarioPerfilDTO(
            id = usuario.id,
            nombre = usuario.nombre,
            email = usuario.email,
            rol = usuario.rol,
            puntosTotalesGlobales = puntosGlobales,
            quinielas = quinielas
        )
    }
}
