package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro, inicio de sesión, refresh token y recuperación de contraseña")
class AuthController(
    private val authService: AuthService,
    @Value("\${app.base-url:http://localhost:8080}") private val appBaseUrl: String
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

    @GetMapping(value = ["/verify-email"], produces = [MediaType.TEXT_HTML_VALUE])
    @Operation(summary = "Verificar correo electrónico")
    fun verifyEmail(@RequestParam token: String): ResponseEntity<String> {
        return try {
            authService.verifyEmail(token)
            ResponseEntity.ok(verificationSuccessHtml())
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(verificationErrorHtml(e.message ?: "Error al verificar correo"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(verificationErrorHtml("Error al verificar correo"))
        }
    }

    private fun verificationSuccessHtml(): String {
        val link = appBaseUrl
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Correo Verificado</title>
            <style>
                body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; background: #0a0a1a; color: #fff; }
                .card { background: #1a1a3e; padding: 40px; border-radius: 16px; text-align: center; max-width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.3); }
                .icon { font-size: 64px; margin-bottom: 16px; }
                h1 { font-size: 24px; margin: 0 0 8px; }
                p { color: #aaa; margin: 0; }
                .btn { display: inline-block; margin-top: 24px; padding: 12px 32px; background: #0D5BFF; color: #fff; text-decoration: none; border-radius: 8px; }
            </style>
            </head>
            <body>
            <div class="card">
                <div class="icon">✔</div>
                <h1>Correo verificado</h1>
                <p>Tu cuenta ha sido activada correctamente. Ya puedes iniciar sesi&oacute;n.</p>
                <a class="btn" href="$link">Ir a la app</a>
            </div>
            </body>
            </html>
        """.trimIndent()
    }

    private fun verificationErrorHtml(message: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Error de verificaci&oacute;n</title>
            <style>
                body { font-family: sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; background: #0a0a1a; color: #fff; }
                .card { background: #1a1a3e; padding: 40px; border-radius: 16px; text-align: center; max-width: 400px; box-shadow: 0 8px 32px rgba(0,0,0,0.3); }
                .icon { font-size: 64px; margin-bottom: 16px; }
                h1 { font-size: 24px; margin: 0 0 8px; }
                p { color: #aaa; margin: 0; }
            </style>
            </head>
            <body>
            <div class="card">
                <div class="icon">✘</div>
                <h1>Error de verificaci&oacute;n</h1>
                <p>$message</p>
            </div>
            </body>
            </html>
        """.trimIndent()
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
        return ResponseEntity.ok(authService.forgotPassword(request))
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña con token")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<MessageResponse> {
        return ResponseEntity.ok(authService.resetPassword(request))
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario actual")
    fun getPerfil(@AuthenticationPrincipal userDetails: UserDetails?): ResponseEntity<UsuarioPerfilDTO> {
        if (userDetails == null) {
            return ResponseEntity.status(401).build()
        }
        return ResponseEntity.ok(authService.getPerfil(userDetails.username))
    }
}
