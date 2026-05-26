package com.quiniela.app.model

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val tipo: String = "Bearer",
    val usuario: UsuarioDTO
)

data class UsuarioDTO(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String = "USER",
    val puntosTotales: Int = 0
)

data class UsuarioPerfilDTO(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String = "USER",
    val puntosTotalesGlobales: Int,
    val quinielas: List<QuinielaResumenDTO>
)

data class QuinielaResumenDTO(
    val id: Long,
    val nombre: String,
    val codigoInvitacion: String,
    val puntosTotales: Int = 0
)

data class CrearQuinielaRequest(
    val nombre: String,
    val codigoInvitacion: String
)

data class UnirseQuinielaRequest(
    val codigoInvitacion: String
)

data class QuinielaDTO(
    val id: Long,
    val nombre: String,
    val codigoInvitacion: String,
    val administrador: UsuarioDTO,
    val participantes: List<UsuarioDTO>,
    val esPublica: Boolean = false
)

data class QuinielaDetalleDTO(
    val id: Long,
    val nombre: String,
    val codigoInvitacion: String,
    val administrador: UsuarioDTO,
    val participantes: List<UsuarioDTO>,
    val partidos: List<PartidoDTO>
)

data class LeaderboardEntryDTO(
    val posicion: Int,
    val usuario: UsuarioDTO,
    val puntosTotales: Int
)

data class MessageResponse(
    val message: String
)