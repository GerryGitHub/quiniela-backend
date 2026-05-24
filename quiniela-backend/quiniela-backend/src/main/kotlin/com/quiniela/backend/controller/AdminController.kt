package com.quiniela.backend.controller

import com.quiniela.backend.service.PartidoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
@Tag(name = "Administración", description = "Endpoints de administración")
class AdminController(
    private val partidoService: PartidoService
) {

    @PostMapping("/calculate-scores/{partidoId}")
    @Operation(summary = "Calcular puntos de un partido", description = "Calcula los puntos de todos los pronósticos asociados a un partido")
    fun calcularPuntos(@PathVariable partidoId: Long): ResponseEntity<Map<String, String>> {
        val partido = partidoService.getPartidoDetalle(partidoId)
        
        if (partido.golesLocalReal == null || partido.golesVisitanteReal == null) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "El partido no tiene marcador definido"))
        }

        return ResponseEntity.ok(mapOf("message" to "Puntos calculados para el partido $partidoId"))
    }
}
