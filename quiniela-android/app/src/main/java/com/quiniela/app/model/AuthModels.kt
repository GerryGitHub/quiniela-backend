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
    val codigo: String,
    val estado: String,
    val puntosTotales: Int
)