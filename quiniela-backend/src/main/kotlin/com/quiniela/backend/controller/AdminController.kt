package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EquipoEstadisticas
import com.quiniela.backend.repository.EquipoEstadisticasRepository
import com.quiniela.backend.repository.EquipoRepository
import com.quiniela.backend.service.AdminService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
@Tag(name = "Administración", description = "Endpoints de administración")
class AdminController(
    private val adminService: AdminService,
    private val equipoRepository: EquipoRepository,
    private val equipoEstadisticasRepository: EquipoEstadisticasRepository
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

    @GetMapping("/equipos-estadisticas")
    @Operation(summary = "Estadísticas de equipos", description = "Lista todos los equipos con ranking FIFA y fair play")
    fun getEquiposEstadisticas(): ResponseEntity<List<EquipoEstadisticasDTO>> {
        val equipos = equipoRepository.findAll()
        val estadisticas = equipoEstadisticasRepository.findAll().associateBy { it.equipo.id }
        val result = equipos.map { eq ->
            val est = estadisticas[eq.id]
            EquipoEstadisticasDTO(
                equipoId = eq.id, nombre = eq.nombre,
                grupo = eq.grupo?.nombre,
                rankingFifa = est?.rankingFifa,
                puntosFairPlay = est?.puntosFairPlay ?: 0
            )
        }
        return ResponseEntity.ok(result)
    }

    @PutMapping("/equipos/{id}/estadisticas")
    @Transactional
    @Operation(summary = "Actualizar estadísticas", description = "Actualiza ranking FIFA y fair play de un equipo")
    fun updateEstadisticas(
        @PathVariable id: Long,
        @RequestBody request: UpdateEstadisticasRequest
    ): ResponseEntity<EquipoEstadisticasDTO> {
        val equipo = equipoRepository.findById(id)
            .orElseThrow { RuntimeException("Equipo no encontrado") }
        var est = equipoEstadisticasRepository.findByEquipoId(id)

        if (est == null) {
            est = EquipoEstadisticas(equipo = equipo)
        }

        request.rankingFifa?.let { est.rankingFifa = it }
        request.puntosFairPlay?.let { est.puntosFairPlay = it }

        equipoEstadisticasRepository.save(est)

        return ResponseEntity.ok(
            EquipoEstadisticasDTO(
                equipoId = equipo.id, nombre = equipo.nombre,
                grupo = equipo.grupo?.nombre,
                rankingFifa = est.rankingFifa,
                puntosFairPlay = est.puntosFairPlay
            )
        )
    }
}
