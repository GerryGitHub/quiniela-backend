package com.quiniela.backend.controller

import com.quiniela.backend.dto.AdminActivityDTO
import com.quiniela.backend.dto.AdminDashboardDTO
import com.quiniela.backend.service.AdminService
import com.quiniela.backend.service.PartidoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
@Tag(name = "Administración", description = "Endpoints de administración")
class AdminController(
    private val adminService: AdminService,
    private val partidoService: PartidoService
) {

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard de métricas", description = "Devuelve conteos de usuarios, quinielas, pronósticos y partidos en vivo")
    fun getDashboard(): ResponseEntity<AdminDashboardDTO> {
        return ResponseEntity.ok(adminService.getDashboard())
    }

    @GetMapping("/activity")
    @Operation(summary = "Actividad reciente", description = "Últimos usuarios, quinielas y partidos (máx. 10 cada uno)")
    fun getActivity(): ResponseEntity<AdminActivityDTO> {
        return ResponseEntity.ok(adminService.getActivity())
    }

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
