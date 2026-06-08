package com.quiniela.backend.service.impl

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.mapper.toPartidoDTO
import com.quiniela.backend.mapper.toPartidoDTOConMinutos
import com.quiniela.backend.repository.PartidoRepository
import com.quiniela.backend.repository.ParticipacionRepository
import com.quiniela.backend.repository.PronosticoRepository
import com.quiniela.backend.service.PartidoService
import com.quiniela.backend.service.PuntosCalculator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PartidoServiceImpl(
    private val partidoRepository: PartidoRepository,
    private val pronosticoRepository: PronosticoRepository,
    private val participacionRepository: ParticipacionRepository,
    private val puntosCalculator: PuntosCalculator
) : PartidoService {

    override fun getPartidos(fecha: String?, fase: String?): List<PartidoDTO> {
        val partidos = when {
            !fecha.isNullOrBlank() -> {
                val fechaObj = LocalDateTime.parse(fecha)
                partidoRepository.findByFechaBetween(fechaObj, fechaObj.plusDays(1))
            }
            !fase.isNullOrBlank() -> {
                val estado = try {
                    EstadoPartido.valueOf(fase.uppercase())
                } catch (e: Exception) {
                    null
                }
                if (estado != null) partidoRepository.findByEstado(estado)
                else partidoRepository.findAll()
            }
            else -> partidoRepository.findAllByOrderByFechaHoraAsc()
        }

        return partidos.map { it.toPartidoDTOConMinutos() }
    }

    override fun getPartidoDetalle(id: Long): PartidoDTO {
        val partido = partidoRepository.findById(id)
            .orElseThrow { NotFoundException("Partido no encontrado") }
        return partido.toPartidoDTO()
    }

    @Transactional
    override fun actualizarPartido(id: Long, request: ActualizarPartidoRequest): PartidoDTO {
        val partido = partidoRepository.findById(id)
            .orElseThrow { NotFoundException("Partido no encontrado") }

        request.golesLocalReal?.let { partido.golesLocalReal = it }
        request.golesVisitanteReal?.let { partido.golesVisitanteReal = it }

        try {
            partido.estado = EstadoPartido.valueOf(request.estado.uppercase())
        } catch (e: Exception) {
            throw IllegalArgumentException("Estado inválido")
        }

        val partidoActualizado = partidoRepository.save(partido)

        if (partidoActualizado.golesLocalReal != null && partidoActualizado.golesVisitanteReal != null
            && partidoActualizado.estado == EstadoPartido.FINALIZADO) {
            calcularPuntos(partidoActualizado)
        }

        return partidoActualizado.toPartidoDTO()
    }

    @Transactional
    fun calcularPuntos(partido: com.quiniela.backend.entity.Partido) {
        val golesLocal = partido.golesLocalReal ?: return
        val golesVisitante = partido.golesVisitanteReal ?: return

        val pronosticos = pronosticoRepository.findByPartidoId(partido.id)
        pronosticos.forEach { pronostico ->
            val puntos = puntosCalculator.calcular(
                pronostico.golesLocalPredicho,
                pronostico.golesVisitantePredicho,
                golesLocal,
                golesVisitante
            )
            pronostico.puntosObtenidos = puntos
            pronosticoRepository.save(pronostico)

            val participacion = pronostico.participacion
            val pronosticosParticipacion = pronosticoRepository.findByParticipacionId(participacion.id)
            participacion.puntosTotales = pronosticosParticipacion.sumOf { it.puntosObtenidos }
            participacionRepository.save(participacion)
        }
    }
}
