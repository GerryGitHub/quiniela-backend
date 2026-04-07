package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro e inicio de sesión")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.register(request))
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario actual")
    fun getPerfil(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<UsuarioPerfilDTO> {
        return ResponseEntity.ok(authService.getPerfil(userDetails.username))
    }
}
