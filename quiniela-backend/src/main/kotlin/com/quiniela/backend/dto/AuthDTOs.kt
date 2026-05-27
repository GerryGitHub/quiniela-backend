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
    val accessToken: String,
    val refreshToken: String? = null,
    val tipo: String = "Bearer",
    val usuario: UsuarioDTO
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "El refresh token es requerido")
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String? = null
)

data class UsuarioDTO(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String = "USER",
    val puntosTotales: Int = 0
)

data class RegisterResponse(
    val message: String
)

data class MessageResponse(
    val message: String
)

data class ResendVerificationRequest(
    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "El email es requerido")
    val email: String
)

data class ForgotPasswordRequest(
    @field:Email(message = "Email inválido")
    @field:NotBlank(message = "El email es requerido")
    val email: String
)

data class ResetPasswordRequest(
    @field:NotBlank(message = "El token es requerido")
    val token: String,
    @field:Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    val newPassword: String
)

data class UsuarioPerfilDTO(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: String = "USER",
    val puntosTotalesGlobales: Int,
    val quinielas: List<QuinielaResumenDTO>
)
