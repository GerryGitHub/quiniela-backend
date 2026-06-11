package com.quiniela.backend.domain

data class PronosticoItem(
    val idPartido: Long,
    val golesLocal: Int,
    val golesVisitante: Int
)

data class GuardarPronosticosCommand(
    val quinielaId: Long,
    val participacionId: Long?,
    val pronosticos: List<PronosticoItem>
)

data class CrearPronosticoCommand(
    val idParticipacion: Long,
    val idPartido: Long,
    val golesLocal: Int,
    val golesVisitante: Int
)
