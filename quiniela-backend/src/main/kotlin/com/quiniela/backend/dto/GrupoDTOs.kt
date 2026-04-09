package com.quiniela.backend.dto

data class GrupoDTO(
    val id: Long,
    val nombre: String,
    val pais: String,
    val selecciones: List<SeleccionDTO>,
    val partidos: List<PartidoDTO>
)

data class SeleccionDTO(
    val id: Long,
    val nombre: String,
    val pais: String,
    val grupo: String,
    val bandera: String? = null,
    val partidosJugados: Int = 0,
    val partidosGanados: Int = 0,
    val partidosEmpatados: Int = 0,
    val partidosPerdidos: Int = 0,
    val golesAFavor: Int = 0,
    val golesEnContra: Int = 0,
    val puntos: Int = 0,
    val diferenciaGoles: Int = 0
)

data class TablaGruposDTO(
    val grupos: List<GrupoDTO>
)

data class ActualizarResultadoRequest(
    val golesLocal: Int,
    val golesVisitante: Int
)