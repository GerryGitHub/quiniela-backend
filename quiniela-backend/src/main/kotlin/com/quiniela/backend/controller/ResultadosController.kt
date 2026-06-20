package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.Constantes
import com.quiniela.backend.entity.RolUsuario
import com.quiniela.backend.service.ResultadosService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/resultados")
@Tag(name = "Resultados", description = "Endpoints para gestionar resultados de partidos")
class ResultadosController(
    private val resultadosService: ResultadosService
) {

    @GetMapping("/partidos")
    @Operation(summary = "Obtener todos los partidos")
    fun getPartidos(): ResponseEntity<List<PartidoDTO>> {
        return ResponseEntity.ok(resultadosService.getPartidos())
    }

    @GetMapping("/debug")
    @Operation(summary = "Debug endpoint")
    fun debug(): ResponseEntity<*> {
        return try {
            val count = resultadosService.getPartidos().size
            ResponseEntity.ok(mapOf(Constantes.RESPONSE_KEY_PARTIDOS_COUNT to count))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.status(500).body(mapOf(Constantes.RESPONSE_KEY_ERROR to e.message))
        }
    }

    @GetMapping("/en-vivo")
    @Operation(summary = "Obtener partidos en vivo")
    fun getPartidosEnVivo(): ResponseEntity<*> {
        return try {
            ResponseEntity.ok(resultadosService.getPartidosEnVivo())
        } catch (e: Exception) {
            ResponseEntity.status(500).body(mapOf(Constantes.RESPONSE_KEY_ERROR to e.message))
        }
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Obtener partidos pendientes")
    fun getPartidosPendientes(): ResponseEntity<List<PartidoDTO>> {
        return ResponseEntity.ok(resultadosService.getPartidosPendientes())
    }

    @GetMapping("/completados")
    @Operation(summary = "Obtener partidos con resultados")
    fun getPartidosConResultados(): ResponseEntity<List<PartidoDTO>> {
        return ResponseEntity.ok(resultadosService.getPartidosConResultados())
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar resultado de partido (Admin)")
    fun actualizarResultado(
        @PathVariable id: Long,
        @RequestBody request: ActualizarResultadoRequest,
        @AuthenticationPrincipal userDetails: UserDetails?
    ): ResponseEntity<PartidoDTO> {
        val esAdmin = userDetails?.authorities?.any { it.authority == RolUsuario.ADMIN.authority } == true
        return ResponseEntity.ok(resultadosService.actualizarResultado(id, request, esAdmin))
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Finalizar partido y calcular puntos (Admin)")
    fun finalizarPartido(@PathVariable id: Long): ResponseEntity<PartidoDTO> {
        return ResponseEntity.ok(resultadosService.finalizarPartido(id))
    }
}