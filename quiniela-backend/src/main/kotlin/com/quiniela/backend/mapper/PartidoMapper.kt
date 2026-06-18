package com.quiniela.backend.mapper

import com.quiniela.backend.dto.PartidoDTO
import com.quiniela.backend.entity.Partido
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

fun Partido.toPartidoDTO(): PartidoDTO {
    return PartidoDTO(
        id = id,
        equipoLocal = equipoLocal.nombre,
        equipoVisitante = equipoVisitante.nombre,
        fechaHora = fechaHora.toString(),
        grupo = grupo?.nombre,
        grupoId = grupo?.id,
        equipoLocalId = equipoLocal.id,
        equipoVisitanteId = equipoVisitante.id,
        golesLocalReal = golesLocalReal,
        golesVisitanteReal = golesVisitanteReal,
        estado = estado.name,
        minutosJugados = minutosJugados
    )
}

fun Partido.toPartidoDTOConMinutos(ahora: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Mexico_City"))): PartidoDTO {
    val zonaMexico = ZoneId.of("America/Mexico_City")
    val fechaPartido = fechaHora.atZone(zonaMexico)
    val minutosParaInicio = if (estado.name == "POR_COMENZAR" && fechaPartido.isAfter(ahora)) {
        Duration.between(ahora, fechaPartido).toMinutes().toInt()
    } else {
        null
    }

    return PartidoDTO(
        id = id,
        equipoLocal = equipoLocal.nombre,
        equipoVisitante = equipoVisitante.nombre,
        fechaHora = fechaHora.toString(),
        grupo = grupo?.nombre,
        grupoId = grupo?.id,
        equipoLocalId = equipoLocal.id,
        equipoVisitanteId = equipoVisitante.id,
        golesLocalReal = golesLocalReal,
        golesVisitanteReal = golesVisitanteReal,
        estado = estado.name,
        minutosParaInicio = minutosParaInicio,
        minutosJugados = minutosJugados
    )
}
