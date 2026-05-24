package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.service.GrupoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/grupos")
@Tag(name = "Grupos FIFA", description = "Endpoints para gestionar grupos y selecciones del mundial")
class GrupoController(
    private val grupoService: GrupoService
) {

    @GetMapping
    @Operation(summary = "Obtener todos los grupos con tablas de posiciones")
    fun getAllGrupos(): ResponseEntity<TablaGruposDTO> {
        return ResponseEntity.ok(grupoService.getAllGrupos())
    }

    @GetMapping("/{nombre}")
    @Operation(summary = "Obtener un grupo específico")
    fun getGrupo(@PathVariable nombre: String): ResponseEntity<GrupoDTO> {
        return ResponseEntity.ok(grupoService.getGrupo(nombre))
    }

    @GetMapping("/partidos")
    @Operation(summary = "Obtener todos los partidos")
    fun getPartidos(): ResponseEntity<List<PartidoDTO>> {
        return ResponseEntity.ok(grupoService.getPartidosGrupos())
    }

    @PatchMapping("/partidos/{id}/resultado")
    @Operation(summary = "Actualizar resultado de un partido")
    fun actualizarResultado(
        @PathVariable id: Long,
        @RequestBody request: ActualizarResultadoRequest
    ): ResponseEntity<PartidoDTO> {
        return ResponseEntity.ok(grupoService.actualizarResultadoPartido(id, request))
    }
}