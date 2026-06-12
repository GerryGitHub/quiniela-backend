package com.quiniela.backend.controller

import com.quiniela.backend.domain.mapper.toCommand
import com.quiniela.backend.domain.mapper.toItem
import com.quiniela.backend.dto.*
import com.quiniela.backend.service.PronosticoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/pronosticos")
@Tag(name = "Pronósticos", description = "Endpoints para gestionar pronósticos")
class PronosticoController(
    private val pronosticoService: PronosticoService
) {

    @GetMapping("/mis-pronosticos")
    @Operation(summary = "Ver todos mis pronósticos")
    fun getMisPronosticos(
        @RequestParam(required = false) quinielaId: Long?,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<MisPronosticosDTO> {
        if (userDetails == null) {
            return ResponseEntity.status(401).build()
        }
        
        return if (quinielaId != null) {
            ResponseEntity.ok(pronosticoService.getMisPronosticos(quinielaId, userDetails.username))
        } else {
            ResponseEntity.ok(pronosticoService.getTodosMisPronosticos(userDetails.username))
        }
    }

    @GetMapping("/quiniela/{quinielaId}")
    @Operation(summary = "Ver mis pronósticos en una quiniela")
    fun getMisPronosticos(
        @PathVariable quinielaId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<MisPronosticosDTO> {
        return ResponseEntity.ok()
            .cacheControl(org.springframework.http.CacheControl.noCache())
            .body(pronosticoService.getMisPronosticos(quinielaId, userDetails.username))
    }

    @PostMapping
    @Operation(summary = "Crear o actualizar un pronóstico")
    fun crearPronostico(
        @RequestBody request: CrearPronosticoRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<PronosticoDTO> {
        return ResponseEntity.ok(pronosticoService.crearOActualizarPronostico(request.toCommand(), userDetails.username))
    }

    @PostMapping("/batch")
    @Operation(summary = "Guardar múltiples pronósticos a la vez")
    fun guardarPronosticosBatch(
        @RequestBody request: CrearPronosticosBatchRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CrearPronosticosBatchResponse> {
        return ResponseEntity.ok(pronosticoService.guardarPronosticosBatch(request.toCommand(), userDetails.username))
    }

    @GetMapping("/quiniela/{quinielaId}/partido/{partidoId}")
    @Operation(summary = "Ver todos los pronósticos de un partido", description = "Solo disponible después de que el partido comience")
    fun getPronosticosPorPartido(
        @PathVariable quinielaId: Long,
        @PathVariable partidoId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<PronosticosPorPartidoDTO> {
        return ResponseEntity.ok(
            pronosticoService.getPronosticosPorPartido(quinielaId, partidoId, userDetails.username)
        )
    }
}