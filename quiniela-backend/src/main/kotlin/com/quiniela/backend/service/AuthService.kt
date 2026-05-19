package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.Usuario
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

@Service
class AuthService(
    private val usuarioRepository: UsuarioRepository,
    private val participacionRepository: ParticipacionRepository,
    private val quinielaRepository: QuinielaRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
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
            password = passwordEncoder.encode(request.password)
        )
        val usuarioGuardado = usuarioRepository.save(usuario)

        val userDetails = User.builder()
            .username(usuarioGuardado.email)
            .password(usuarioGuardado.id.toString())
            .build()

        val token = jwtService.generateToken(userDetails)

        return AuthResponse(
            token = token,
            usuario = UsuarioDTO(
                id = usuarioGuardado.id,
                nombre = usuarioGuardado.nombre,
                email = usuarioGuardado.email,
                rol = usuarioGuardado.rol
            )
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )

        val userDetails = userDetailsService.loadUserByUsername(request.email)
        val usuario = usuarioRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

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
