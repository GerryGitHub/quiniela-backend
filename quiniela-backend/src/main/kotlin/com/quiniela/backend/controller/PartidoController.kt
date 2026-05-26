package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.service.PartidoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/partidos")
@Tag(name = "Partidos", description = "Endpoints para gestionar partidos")
class PartidoController(
    private val partidoService: PartidoService
) {

    @GetMapping
    @Operation(summary = "Listar todos los partidos", description = "Filtrar por ?fecha= o ?fase=grupos")
    fun getPartidos(
        @RequestParam(required = false) fecha: String?,
        @RequestParam(required = false) fase: String?
    ): ResponseEntity<List<PartidoDTO>> {
        return ResponseEntity.ok(partidoService.getPartidos(fecha, fase))
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de un partido")
    fun getPartidoDetalle(@PathVariable id: Long): ResponseEntity<PartidoDTO> {
        return ResponseEntity.ok(partidoService.getPartidoDetalle(id))
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar marcador de un partido (Admin)")
    fun actualizarPartido(
        @PathVariable id: Long,
        @RequestBody request: ActualizarPartidoRequest
    ): ResponseEntity<PartidoDTO> {
        return ResponseEntity.ok(partidoService.actualizarPartido(id, request))
    }
}
