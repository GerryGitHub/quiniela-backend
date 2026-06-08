package com.quiniela.backend.service

import com.quiniela.backend.dto.*

interface QuinielaService {
    fun getQuinielas(email: String): List<QuinielaResumenDTO>
    fun crearQuiniela(request: CrearQuinielaRequest, email: String): QuinielaDTO
    fun getQuinielaDetalle(id: Long, email: String): QuinielaDetalleDTO
    fun unirseQuiniela(request: UnirseQuinielaRequest, email: String): QuinielaDTO
    fun getLeaderboard(quinielaId: Long): List<LeaderboardEntryDTO>
}
