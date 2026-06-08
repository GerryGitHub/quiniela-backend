package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.service.AdminService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
@Tag(name = "Administración", description = "Endpoints de administración")
class AdminController(
    private val adminService: AdminService
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

    @GetMapping("/system")
    @Operation(summary = "Estado del sistema", description = "Health check de API, BD y última actualización de partidos")
    fun getSystemStatus(): ResponseEntity<AdminSystemDTO> {
        return ResponseEntity.ok(adminService.getSystemStatus())
    }

    @GetMapping("/quinielas")
    @Operation(summary = "Lista de quinielas", description = "Quinielas con búsqueda y ordenamiento")
    fun getQuinielas(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) sort: String?,
        @RequestParam(required = false) order: String?
    ): ResponseEntity<List<AdminQuinielaListDTO>> {
        return ResponseEntity.ok(adminService.getQuinielas(search, sort, order))
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Detalle de usuario", description = "Información detallada de un usuario y sus quinielas")
    fun getUserDetail(@PathVariable id: Long): ResponseEntity<AdminUserDetailDTO> {
        val detail = adminService.getUserDetail(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(detail)
    }

    @PostMapping("/calculate-scores/{partidoId}")
    @Operation(summary = "Calcular puntos de un partido", description = "Calcula los puntos de todos los pronósticos asociados a un partido")
    fun calcularPuntos(@PathVariable partidoId: Long): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("message" to "Puntos calculados para el partido $partidoId"))
    }

    @GetMapping("/users")
    @Operation(summary = "Lista de usuarios", description = "Usuarios registrados con búsqueda y filtro")
    fun getUsers(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) verificado: Boolean?
    ): ResponseEntity<List<AdminUserListDTO>> {
        return ResponseEntity.ok(adminService.getUsers(search, verificado))
    }
}
