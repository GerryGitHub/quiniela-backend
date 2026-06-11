package com.quiniela.backend.domain.mapper

import com.quiniela.backend.domain.CrearPronosticoCommand
import com.quiniela.backend.domain.GuardarPronosticosCommand
import com.quiniela.backend.domain.PronosticoItem
import com.quiniela.backend.dto.CrearPronosticosBatchRequest
import com.quiniela.backend.dto.CrearPronosticoRequest
import com.quiniela.backend.dto.PronosticoItemRequest

fun CrearPronosticoRequest.toCommand() = CrearPronosticoCommand(
    idParticipacion = idParticipacion,
    idPartido = idPartido,
    golesLocal = golesLocalPredicho,
    golesVisitante = golesVisitantePredicho
)

fun PronosticoItemRequest.toItem() = PronosticoItem(
    idPartido = idPartido,
    golesLocal = golesLocalPredicho,
    golesVisitante = golesVisitantePredicho
)

fun CrearPronosticosBatchRequest.toCommand() = GuardarPronosticosCommand(
    quinielaId = idQuiniela,
    participacionId = idParticipacion,
    pronosticos = pronosticos.map { it.toItem() }
)
