package com.quiniela.backend.service

import com.quiniela.backend.domain.*
import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EmailVerificationToken
import com.quiniela.backend.entity.RefreshToken
import com.quiniela.backend.entity.Usuario
import com.quiniela.backend.exception.ForbiddenException
import com.quiniela.backend.repository.EmailVerificationTokenRepository
import com.quiniela.backend.repository.RefreshTokenRepository
import com.quiniela.backend.repository.UsuarioRepository
import com.quiniela.backend.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    @Value("\${app.refresh-token-expiration-days:30}") private val refreshTokenExpirationDays: Long
) {

    @Transactional
    fun register(command: RegisterCommand): RegisterResponse {
        if (usuarioRepository.existsByEmail(command.email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }

        val usuario = Usuario(
            nombre = command.nombre,
            email = command.email,
            password = passwordEncoder.encode(command.password),
            emailVerified = false,
            fechaRegistro = LocalDateTime.now()
        )
        val usuarioGuardado = usuarioRepository.save(usuario)

        crearTokenVerificacion(usuarioGuardado)

        return RegisterResponse(message = "Revisa tu correo para activar tu cuenta con el código OTP")
    }

    @Transactional
    fun verifyRegistrationOtp(command: VerifyOtpCommand): MessageResponse {
        val usuario = usuarioRepository.findByEmail(command.email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        if (usuario.emailVerified) {
            throw IllegalArgumentException("La cuenta ya está verificada")
        }

        val verificationToken = emailVerificationTokenRepository.findByUsuarioAndToken(usuario, command.code)
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
    fun login(command: LoginCommand): AuthResponse {
        val usuario = usuarioRepository.findByEmail(command.email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        if (!usuario.emailVerified) {
            throw ForbiddenException("Debes verificar tu correo antes de iniciar sesión")
        }

        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(command.email, command.password)
        )

        val userDetails = userDetailsService.loadUserByUsername(command.email)
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
    fun refreshAccessToken(command: RefreshTokenCommand): RefreshTokenResponse {
        val storedToken = refreshTokenRepository.findByToken(command.refreshToken)
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
    fun resendVerification(command: ResendVerificationCommand): MessageResponse {
        val usuario = usuarioRepository.findByEmail(command.email)

        if (usuario.isPresent && !usuario.get().emailVerified) {
            val user = usuario.get()
            emailVerificationTokenRepository.deleteByUsuario(user)
            crearTokenVerificacion(user)
        }

        return MessageResponse(message = "Si el correo está registrado y no verificado, recibirás un código de verificación")
    }

    private fun generarCodigo(): String =
        String.format("%06d", (100000..999999).random())

    private fun crearTokenVerificacion(usuario: Usuario): String {
        val code = generarCodigo()
        val verificationToken = EmailVerificationToken(
            usuario = usuario,
            token = code,
            expiresAt = LocalDateTime.now().plusMinutes(15)
        )
        emailVerificationTokenRepository.save(verificationToken)
        emailService.sendVerificationEmail(
            email = usuario.email,
            nombre = usuario.nombre,
            token = code
        )
        return code
    }

}
