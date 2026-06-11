package com.quiniela.backend.domain.mapper

import com.quiniela.backend.domain.*
import com.quiniela.backend.dto.*

fun RegisterRequest.toCommand() = RegisterCommand(
    nombre = nombre,
    email = email,
    password = password
)

fun LoginRequest.toCommand() = LoginCommand(
    email = email,
    password = password
)

fun VerifyRegistrationOtpRequest.toCommand() = VerifyOtpCommand(
    email = email,
    code = code
)

fun ResendVerificationRequest.toCommand() = ResendVerificationCommand(
    email = email
)

fun ForgotPasswordRequest.toCommand() = ForgotPasswordCommand(
    email = email
)

fun ResetPasswordRequest.toCommand() = ResetPasswordCommand(
    email = email,
    code = code,
    newPassword = newPassword
)

fun RefreshTokenRequest.toCommand() = RefreshTokenCommand(
    refreshToken = refreshToken
)
