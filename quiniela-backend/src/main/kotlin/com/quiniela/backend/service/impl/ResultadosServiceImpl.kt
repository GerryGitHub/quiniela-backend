package com.quiniela.backend.service.impl

import com.quiniela.backend.dto.ActualizarResultadoRequest
import com.quiniela.backend.dto.PartidoDTO
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.mapper.toPartidoDTO
import com.quiniela.backend.mapper.toPartidoDTOConMinutos
import com.quiniela.backend.repository.ParticipacionRepository
import com.quiniela.backend.repository.PartidoRepository
import com.quiniela.backend.repository.PronosticoRepository
import com.quiniela.backend.service.PuntosCalculator
import com.quiniela.backend.service.ResultadosService
import com.quiniela.backend.service.WebSocketService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class ResultadosServiceImpl(
    private val partidoRepository: PartidoRepository,
    private val pronosticoRepository: PronosticoRepository,
    private val participacionRepository: ParticipacionRepository,
    private val puntosCalculator: PuntosCalculator,
    private val webSocketService: WebSocketService
) : ResultadosService {

    override fun getPartidos(): List<PartidoDTO> {
        return partidoRepository.findAllByOrderByFechaHoraAsc().map { it.toPartidoDTO() }
    }

    override fun getPartidosPendientes(): List<PartidoDTO> {
        return partidoRepository.findAllByOrderByFechaHoraAsc()
            .filter { it.estado == EstadoPartido.PENDIENTE }
            .map { it.toPartidoDTO() }
    }

    override fun getPartidosConResultados(): List<PartidoDTO> {
        return partidoRepository.findAllByOrderByFechaHoraAsc()
            .filter { it.golesLocalReal != null && it.golesVisitanteReal != null }
            .map { it.toPartidoDTO() }
    }

    override fun getPartidosEnVivo(): List<PartidoDTO> {
        val zonaMexico = ZoneId.of("America/Mexico_City")
        val ahora = ZonedDateTime.now(zonaMexico)
        val inicioDia = ahora.toLocalDate().atStartOfDay(zonaMexico)
        val finDia = inicioDia.plusDays(1)

        return partidoRepository.findAll()
            .filter {
                val fechaPartido = it.fechaHora.atZone(zonaMexico)
                fechaPartido >= inicioDia && fechaPartido < finDia &&
                (it.estado == EstadoPartido.POR_COMENZAR ||
                 it.estado == EstadoPartido.EN_CURSO ||
                 it.estado == EstadoPartido.FINALIZADO)
            }
            .sortedBy { it.fechaHora }
            .map { it.toPartidoDTOConMinutos(ahora) }
    }

    @Transactional
    override fun actualizarResultado(partidoId: Long, request: ActualizarResultadoRequest, esAdmin: Boolean): PartidoDTO {
        val partido = partidoRepository.findById(partidoId)
            .orElseThrow { NotFoundException("Partido no encontrado: $partidoId") }

        when (partido.estado) {
            EstadoPartido.FINALIZADO -> throw IllegalArgumentException("No se puede modificar un partido ya finalizado")
            EstadoPartido.PENDIENTE, EstadoPartido.POR_COMENZAR -> throw IllegalArgumentException("No se puede modificar un partido que no ha iniciado")
            EstadoPartido.EN_CURSO -> {
                if (!esAdmin) {
                    throw IllegalArgumentException("Solo admins pueden editar partidos en vivo")
                }
            }
        }

        partido.golesLocalReal = request.golesLocal
        partido.golesVisitanteReal = request.golesVisitante

        val saved = partidoRepository.save(partido).toPartidoDTO()
        webSocketService.notifyPartidoActualizado(saved)

        return saved
    }

    @Transactional
    override fun finalizarPartido(partidoId: Long): PartidoDTO {
        val partido = partidoRepository.findById(partidoId)
            .orElseThrow { NotFoundException("Partido no encontrado: $partidoId") }

        if (partido.golesLocalReal == null || partido.golesVisitanteReal == null) {
            throw IllegalArgumentException("El partido no tiene resultado definido")
        }

        partido.estado = EstadoPartido.FINALIZADO
        partidoRepository.save(partido)

        val golesLocal = partido.golesLocalReal!!
        val golesVisitante = partido.golesVisitanteReal!!

        val pronosticos = pronosticoRepository.findByPartidoId(partidoId)

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

        return partido.toPartidoDTO()
    }
}
