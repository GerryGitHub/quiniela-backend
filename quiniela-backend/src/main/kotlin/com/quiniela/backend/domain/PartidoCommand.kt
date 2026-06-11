package com.quiniela.backend.domain

data class ActualizarPartidoCommand(
    val golesLocal: Int?,
    val golesVisitante: Int?,
    val estado: String?
)
