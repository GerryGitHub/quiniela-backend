package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.service.AuthService
import com.quiniela.backend.service.PasswordResetService
import com.quiniela.backend.service.PerfilService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro, inicio de sesión, refresh token y recuperación de contraseña")
class AuthController(
    private val authService: AuthService,
    private val passwordResetService: PasswordResetService,
    private val perfilService: PerfilService
) {

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token mediante refresh token")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        return ResponseEntity.ok(authService.refreshAccessToken(request))
    }

    @PostMapping("/verify-registration-otp")
    @Operation(summary = "Verificar cuenta con código OTP")
    fun verifyRegistrationOtp(@Valid @RequestBody request: VerifyRegistrationOtpRequest): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(authService.verifyRegistrationOtp(request))
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Reenviar correo de verificación")
    fun resendVerification(@Valid @RequestBody request: ResendVerificationRequest): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(authService.resendVerification(request))
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar restablecimiento de contraseña")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(passwordResetService.forgotPassword(request))
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña con token")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(passwordResetService.resetPassword(request))
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario actual")
    fun getPerfil(@AuthenticationPrincipal userDetails: UserDetails?): ResponseEntity<UsuarioPerfilDTO> {
        if (userDetails == null) {
            return ResponseEntity.status(401).build()
        }
        return ResponseEntity.ok(perfilService.getPerfil(userDetails.username))
    }
}
