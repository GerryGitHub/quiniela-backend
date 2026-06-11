package com.quiniela.backend.domain

data class RegisterCommand(
    val nombre: String,
    val email: String,
    val password: String
)

data class LoginCommand(
    val email: String,
    val password: String
)

data class VerifyOtpCommand(
    val email: String,
    val code: String
)

data class ResendVerificationCommand(
    val email: String
)

data class ForgotPasswordCommand(
    val email: String
)

data class ResetPasswordCommand(
    val email: String,
    val code: String,
    val newPassword: String
)

data class RefreshTokenCommand(
    val refreshToken: String
)
