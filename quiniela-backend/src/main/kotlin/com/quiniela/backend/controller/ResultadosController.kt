package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.service.ResultadosService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
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
    @Operation(summary = "Actualizar resultado de partido")
    fun actualizarResultado(
        @PathVariable id: Long,
        @RequestBody request: ActualizarResultadoRequest
    ): ResponseEntity<PartidoDTO> {
        return ResponseEntity.ok(resultadosService.actualizarResultado(id, request))
    }
}