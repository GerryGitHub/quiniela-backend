package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.entity.Partido
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ResultadosService(
    private val partidoRepository: PartidoRepository
) {

    fun getPartidos(): List<PartidoDTO> {
        return partidoRepository.findAllByOrderByFechaHoraAsc().map { it.toDTO() }
    }

    fun getPartidosPendientes(): List<PartidoDTO> {
        return partidoRepository.findAllByOrderByFechaHoraAsc()
            .filter { it.estado == EstadoPartido.PENDIENTE }
            .map { it.toDTO() }
    }

    fun getPartidosConResultados(): List<PartidoDTO> {
        return partidoRepository.findAllByOrderByFechaHoraAsc()
            .filter { it.golesLocalReal != null && it.golesVisitanteReal != null }
            .map { it.toDTO() }
    }

    @Transactional
    fun actualizarResultado(partidoId: Long, request: ActualizarResultadoRequest): PartidoDTO {
        val partido = partidoRepository.findById(partidoId)
            .orElseThrow { NotFoundException("Partido no encontrado: $partidoId") }

        partido.golesLocalReal = request.golesLocal
        partido.golesVisitanteReal = request.golesVisitante
        partido.estado = EstadoPartido.FINALIZADO

        return partidoRepository.save(partido).toDTO()
    }

    private fun Partido.toDTO() = PartidoDTO(
        id = id,
        equipoLocal = equipoLocal.nombre,
        equipoVisitante = equipoVisitante.nombre,
        fechaHora = fechaHora.toString(),
        grupo = grupo.nombre,
        grupoId = grupo.id,
        equipoLocalId = equipoLocal.id,
        equipoVisitanteId = equipoVisitante.id,
        golesLocalReal = golesLocalReal,
        golesVisitanteReal = golesVisitanteReal,
        estado = estado.name
    )
}