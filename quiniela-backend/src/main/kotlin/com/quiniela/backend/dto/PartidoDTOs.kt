package com.quiniela.backend.dto

import java.time.LocalDateTime

data class PartidoDTO(
    val id: Long,
    val equipoLocal: String,
    val equipoVisitante: String,
    val fechaHora: String,
    val grupo: String? = null,
    val grupoId: Long? = null,
    val equipoLocalId: Long? = null,
    val equipoVisitanteId: Long? = null,
    val golesLocalReal: Int?,
    val golesVisitanteReal: Int?,
    val estado: String,
    val minutosParaInicio: Int? = null,
    val minutosJugados: Int? = null
)

data class ActualizarPartidoRequest(
    val golesLocalReal: Int?,
    val golesVisitanteReal: Int?,
    val estado: String
)

data class PronosticoDTO(
    val id: Long,
    val usuario: UsuarioDTO,
    val partido: PartidoDTO,
    val golesLocalPredicho: Int,
    val golesVisitantePredicho: Int,
    val puntosObtenidos: Int,
    val quinielaId: Long? = null
)

data class CrearPronosticoRequest(
    val idParticipacion: Long,
    val idPartido: Long,
    val golesLocalPredicho: Int,
    val golesVisitantePredicho: Int
)

data class PronosticoItemRequest(
    val idPartido: Long,
    val golesLocalPredicho: Int,
    val golesVisitantePredicho: Int
)

data class CrearPronosticosBatchRequest(
    val idQuiniela: Long,
    val pronosticos: List<PronosticoItemRequest>,
    val idParticipacion: Long? = null
)

data class CrearPronosticosBatchResponse(
    val pronosticosGuardados: Int,
    val pronosticos: List<PronosticoDTO>
)

data class MisPronosticosDTO(
    val pronosticos: List<PronosticoDTO>
)

data class PronosticosPorPartidoDTO(
    val partido: PartidoDTO,
    val pronosticos: List<PronosticoResumenDTO>
)

data class PronosticoResumenDTO(
    val usuario: UsuarioDTO,
    val golesLocalPredicho: Int,
    val golesVisitantePredicho: Int,
    val puntosObtenidos: Int
)
