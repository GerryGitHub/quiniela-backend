package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.entity.Partido
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class ResultadosService(
    private val partidoRepository: PartidoRepository,
    private val pronosticoRepository: PronosticoRepository,
    private val participacionRepository: ParticipacionRepository
) {
    private var webSocketService: WebSocketService? = null

    fun setWebSocketService(service: WebSocketService) {
        this.webSocketService = service
    }

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

    fun getPartidosEnVivo(): List<PartidoDTO> {
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
            .map { it.toDTOConMinutos(ahora) }
    }

    @Transactional
    fun actualizarResultado(partidoId: Long, request: ActualizarResultadoRequest, esAdmin: Boolean = false): PartidoDTO {
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
        
        if (partido.estado == EstadoPartido.EN_CURSO && request.golesLocal != null && request.golesVisitante != null) {
            // mantener EN_CURSO
        }

        val saved = partidoRepository.save(partido).toDTO()
        webSocketService?.notifyPartidoActualizado(saved)
        
        return saved
    }

    @Transactional
    fun finalizarPartido(partidoId: Long): PartidoDTO {
        val partido = partidoRepository.findById(partidoId)
            .orElseThrow { NotFoundException("Partido no encontrado: $partidoId") }

        if (partido.golesLocalReal == null || partido.golesVisitanteReal == null) {
            throw IllegalArgumentException("El partido no tiene resultado definido")
        }

        partido.estado = EstadoPartido.FINALIZADO
        partidoRepository.save(partido)

        // Calcular puntos de los pronósticos
        val pronosticos = pronosticoRepository.findByPartidoId(partidoId)
        
        pronosticos.forEach { pronostico ->
            val puntos = calcularPuntosPronostico(
                pronostico.golesLocalPredicho,
                pronostico.golesVisitantePredicho,
                partido.golesLocalReal!!,
                partido.golesVisitanteReal!!
            )
            pronostico.puntosObtenidos = puntos
            pronosticoRepository.save(pronostico)

            // Actualizar puntos totales de la participación
            val participacion = pronostico.participacion
            val pronosticosParticipacion = pronosticoRepository.findByParticipacionId(participacion.id)
            val puntosTotales = pronosticosParticipacion.sumOf { it.puntosObtenidos }
            participacion.puntosTotales = puntosTotales
            participacionRepository.save(participacion)
        }

        return partido.toDTO()
    }

    private fun calcularPuntosPronostico(
        predichoLocal: Int,
        predichoVisitante: Int,
        realLocal: Int,
        realVisitante: Int
    ): Int {
        return when {
            predichoLocal == realLocal && predichoVisitante == realVisitante -> 10
            (predichoLocal - predichoVisitante) == (realLocal - realVisitante) -> 5
            (predichoLocal > predichoVisitante && realLocal > realVisitante) ||
            (predichoLocal < predichoVisitante && realLocal < realVisitante) ||
            (predichoLocal == realLocal && predichoVisitante == realVisitante) -> 3
            else -> 0
        }
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
        estado = estado.name,
        minutosJugados = minutosJugados
    )

    private fun Partido.toDTOConMinutos(ahora: ZonedDateTime): PartidoDTO {
        val zonaMexico = ZoneId.of("America/Mexico_City")
        val fechaPartido = fechaHora.atZone(zonaMexico)
        val minutosParaInicio = if (fechaPartido.isAfter(ahora)) {
            Duration.between(ahora, fechaPartido).toMinutes().toInt()
        } else {
            null
        }

        return PartidoDTO(
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
            estado = estado.name,
            minutosParaInicio = minutosParaInicio,
            minutosJugados = minutosJugados
        )
    }
}