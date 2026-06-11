package com.quiniela.backend.domain.mapper

import com.quiniela.backend.domain.ActualizarPartidoCommand
import com.quiniela.backend.dto.ActualizarPartidoRequest

fun ActualizarPartidoRequest.toCommand() = ActualizarPartidoCommand(
    golesLocal = golesLocalReal,
    golesVisitante = golesVisitanteReal,
    estado = estado
)
