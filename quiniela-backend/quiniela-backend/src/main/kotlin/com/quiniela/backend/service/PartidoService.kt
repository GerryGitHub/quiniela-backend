package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.entity.Partido
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.repository.PartidoRepository
import com.quiniela.backend.repository.ParticipacionRepository
import com.quiniela.backend.repository.PronosticoRepository
import com.quiniela.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PartidoService(
    private val partidoRepository: PartidoRepository,
    private val pronosticoRepository: PronosticoRepository,
    private val participacionRepository: ParticipacionRepository,
    private val usuarioRepository: UsuarioRepository
) {

    fun getPartidos(fecha: String?, fase: String?): List<PartidoDTO> {
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

        return partidos.map { it.toDTO() }
    }

    fun getPartidoDetalle(id: Long): PartidoDTO {
        val partido = partidoRepository.findById(id)
            .orElseThrow { NotFoundException("Partido no encontrado") }
        return partido.toDTO()
    }

    @Transactional
    fun actualizarPartido(id: Long, request: ActualizarPartidoRequest): PartidoDTO {
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

        if (partidoActualizado.golesLocalReal != null && partidoActualizado.golesVisitanteReal != null) {
            calcularPuntos(partidoActualizado)
        }

        return partidoActualizado.toDTO()
    }

    @Transactional
    fun calcularPuntos(partido: Partido) {
        val pronosticos = pronosticoRepository.findByPartidoId(partido.id)

        pronosticos.forEach { pronostico ->
            val puntos = calcularPuntosPronostico(
                pronostico.golesLocalPredicho,
                pronostico.golesVisitantePredicho,
                partido.golesLocalReal ?: 0,
                partido.golesVisitanteReal ?: 0
            )
            pronostico.puntosObtenidos = puntos
            pronosticoRepository.save(pronostico)

            val participacion = pronostico.participacion
            val pronosticosParticipacion = pronosticoRepository.findByParticipacionId(participacion.id)
            val puntosTotales = pronosticosParticipacion.sumOf { it.puntosObtenidos }
            participacion.puntosTotales = puntosTotales
            participacionRepository.save(participacion)
        }
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
            (predichoLocal < predichoVisitante && realLocal < realVisitante) -> 3
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
        estado = estado.name
    )
}
