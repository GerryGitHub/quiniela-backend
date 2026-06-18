package com.quiniela.backend.controller

import com.quiniela.backend.dto.*
import com.quiniela.backend.service.EliminatoriasService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/eliminatorias")
class EliminatoriasController(
    private val eliminatoriasService: EliminatoriasService
) {

    @GetMapping("/preview")
    fun getPreview(): ResponseEntity<BracketPreviewDTO> {
        return ResponseEntity.ok(eliminatoriasService.getPreview())
    }

    @GetMapping("/status")
    fun getStatus(): ResponseEntity<EliminatoriasStatusDTO> {
        return ResponseEntity.ok(eliminatoriasService.getEliminatoriasStatus())
    }

    @PostMapping("/crear")
    @PreAuthorize("hasRole('ADMIN')")
    fun crearEliminatorias(
        @RequestBody request: CrearEliminatoriasRequest,
        authentication: Authentication
    ): ResponseEntity<CrearEliminatoriasResponse> {
        val email = authentication.name
        val result = eliminatoriasService.crearEliminatorias(request, email)
        return ResponseEntity.ok(result)
    }
}
