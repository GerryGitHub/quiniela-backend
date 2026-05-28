package com.quiniela.app.model

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
) {
    companion object {
        const val ESTADO_PENDIENTE = "PENDIENTE"
        const val ESTADO_POR_COMENZAR = "POR_COMENZAR"
        const val ESTADO_EN_CURSO = "EN_CURSO"
        const val ESTADO_FINALIZADO = "FINALIZADO"
    }
}

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
    val puntosObtenidos: Int
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
    val pronosticos: List<PronosticoItemRequest>
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