package com.quiniela.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "El nombre es requerido")
    val nombre: String,
    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "El email es requerido")
    val email: String,
    @field:Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    val password: String
)

data class LoginRequest(
    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "El email es requerido")
    val email: String,
    @field:NotBlank(message = "La contraseña es requerida")
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
