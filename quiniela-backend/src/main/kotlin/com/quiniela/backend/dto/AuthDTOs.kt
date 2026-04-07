package com.quiniela.backend.dto

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
    val puntosTotales: Int = 0
)

data class UsuarioPerfilDTO(
    val id: Long,
    val nombre: String,
    val email: String,
    val puntosTotalesGlobales: Int,
    val quinielas: List<QuinielaResumenDTO>
)
