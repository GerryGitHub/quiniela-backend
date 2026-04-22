package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.service.QuinielaService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/quinielas")
@Tag(name = "Quinielas", description = "Endpoints para gestionar quinielas")
class QuinielaController(
    private val quinielaService: QuinielaService
) {

    @GetMapping
    @Operation(summary = "Listar quinielas del usuario")
    fun getQuinielas(@AuthenticationPrincipal userDetails: UserDetails?): ResponseEntity<List<QuinielaResumenDTO>> {
        if (userDetails == null) return ResponseEntity.status(401).build()
        return ResponseEntity.ok(quinielaService.getQuinielas(userDetails.username))
    }

    @PostMapping
    @Operation(summary = "Crear nueva quiniela")
    fun crearQuiniela(
        @RequestBody request: CrearQuinielaRequest,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<QuinielaDTO> {
        if (userDetails == null) return ResponseEntity.status(401).build()
        
        val codigoGenerado = if (request.codigoInvitacion.isNullOrBlank()) {
            java.util.UUID.randomUUID().toString().take(8).uppercase()
        } else {
            request.codigoInvitacion
        }
        
        val requestConCodigo = CrearQuinielaRequest(request.nombre, codigoGenerado)
        return ResponseEntity.ok(quinielaService.crearQuiniela(requestConCodigo, userDetails.username))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de una quiniela")
    fun getQuinielaDetalle(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<QuinielaDetalleDTO> {
        if (userDetails == null) return ResponseEntity.status(401).build()
        return ResponseEntity.ok(quinielaService.getQuinielaDetalle(id, userDetails.username))
    }

    @PostMapping("/join")
    @Operation(summary = "Unirse a una quiniela por código de invitación")
    fun unirseQuiniela(
        @RequestBody request: UnirseQuinielaRequest,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<QuinielaDTO> {
        if (userDetails == null) return ResponseEntity.status(401).build()
        return ResponseEntity.ok(quinielaService.unirseQuiniela(request, userDetails.username))
    }

    @GetMapping("/{id}/leaderboard")
    @Operation(summary = "Obtener tabla de posiciones de una quiniela")
    fun getLeaderboard(@PathVariable id: Long): ResponseEntity<List<LeaderboardEntryDTO>> {
        return ResponseEntity.ok(quinielaService.getLeaderboard(id))
    }
}