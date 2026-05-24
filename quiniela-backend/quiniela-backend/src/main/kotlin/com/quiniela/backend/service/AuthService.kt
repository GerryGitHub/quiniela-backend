package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EmailVerificationToken
import com.quiniela.backend.entity.Usuario
import com.quiniela.backend.exception.ForbiddenException
import com.quiniela.backend.repository.EmailVerificationTokenRepository
import com.quiniela.backend.repository.ParticipacionRepository
import com.quiniela.backend.repository.QuinielaRepository
import com.quiniela.backend.repository.UsuarioRepository
import com.quiniela.backend.security.JwtService
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
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
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService
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
            emailVerified = false
        )
        val usuarioGuardado = usuarioRepository.save(usuario)

        val token = UUID.randomUUID().toString()
        val verificationToken = EmailVerificationToken(
            usuario = usuarioGuardado,
            token = token,
            expiresAt = LocalDateTime.now().plusHours(24)
        )
        emailVerificationTokenRepository.save(verificationToken)

        emailService.sendVerificationEmail(
            email = usuarioGuardado.email,
            nombre = usuarioGuardado.nombre,
            token = token
        )

        return RegisterResponse(message = "Revisa tu correo para activar tu cuenta")
    }

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
        val token = jwtService.generateToken(userDetails)

        return AuthResponse(
            token = token,
            usuario = UsuarioDTO(
                id = usuario.id,
                nombre = usuario.nombre,
                email = usuario.email,
                rol = usuario.rol
            )
        )
    }

    @Transactional
    fun verifyEmail(token: String): MessageResponse {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            .orElseThrow { IllegalArgumentException("Token inválido") }

        if (verificationToken.used) {
            throw IllegalArgumentException("El token ya fue utilizado")
        }

        if (verificationToken.expiresAt.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("El token ha expirado")
        }

        val usuario = verificationToken.usuario
        usuario.emailVerified = true
        verificationToken.used = true

        usuarioRepository.save(usuario)
        emailVerificationTokenRepository.save(verificationToken)

        return MessageResponse(message = "Correo verificado correctamente")
    }

    @Transactional
    fun resendVerification(request: ResendVerificationRequest): MessageResponse {
        val usuario = usuarioRepository.findByEmail(request.email)

        if (usuario.isPresent && !usuario.get().emailVerified) {
            val user = usuario.get()
            emailVerificationTokenRepository.deleteByUsuario(user)

            val newToken = UUID.randomUUID().toString()
            val verificationToken = EmailVerificationToken(
                usuario = user,
                token = newToken,
                expiresAt = LocalDateTime.now().plusHours(24)
            )
            emailVerificationTokenRepository.save(verificationToken)

            emailService.sendVerificationEmail(
                email = user.email,
                nombre = user.nombre,
                token = newToken
            )
        }

        return MessageResponse(message = "Si el correo está registrado y no verificado, recibirás un enlace de verificación")
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
