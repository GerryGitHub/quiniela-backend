package com.quiniela.backend.service.impl

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.entity.Pronostico
import com.quiniela.backend.exception.ForbiddenException
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.mapper.toPartidoDTO
import com.quiniela.backend.repository.*
import com.quiniela.backend.service.PronosticoService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PronosticoServiceImpl(
    private val pronosticoRepository: PronosticoRepository,
    private val partidoRepository: PartidoRepository,
    private val participacionRepository: ParticipacionRepository,
    private val usuarioRepository: UsuarioRepository
) : PronosticoService {

    override fun getMisPronosticos(quinielaId: Long, email: String): MisPronosticosDTO {
        val pronosticos = pronosticoRepository.findByQuinielaIdAndUsuarioEmail(quinielaId, email)
        return MisPronosticosDTO(pronosticos = pronosticos.map { it.toDTO() })
    }

    override fun getTodosMisPronosticos(email: String): MisPronosticosDTO {
        val pronosticos = pronosticoRepository.findByUsuarioEmail(email)
        return MisPronosticosDTO(pronosticos = pronosticos.map { it.toDTO() })
    }

    @Transactional
    override fun crearOActualizarPronostico(request: CrearPronosticoRequest, email: String): PronosticoDTO {
        val usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        val partido = partidoRepository.findById(request.idPartido)
            .orElseThrow { NotFoundException("Partido no encontrado") }

        if (partido.fechaHora.isBefore(LocalDateTime.now())) {
            throw ForbiddenException("No puedes modificar el pronóstico, el partido ya ha comenzado")
        }

        if (partido.estado != EstadoPartido.PENDIENTE) {
            throw ForbiddenException("Solo puedes prognosticar partidos pendientes")
        }

        val participacion = participacionRepository.findById(request.idParticipacion)
            .orElseThrow { NotFoundException("Participación no encontrada") }

        if (participacion.usuario.id != usuario.id) {
            throw ForbiddenException("No puedes crear pronósticos para otra participación")
        }

        val pronosticoExistente = pronosticoRepository.findByParticipacionIdAndPartidoId(
            request.idParticipacion,
            request.idPartido
        )

        val pronostico = if (pronosticoExistente != null) {
            pronosticoExistente.golesLocalPredicho = request.golesLocalPredicho
            pronosticoExistente.golesVisitantePredicho = request.golesVisitantePredicho
            pronosticoExistente
        } else {
            Pronostico(
                participacion = participacion,
                partido = partido,
                golesLocalPredicho = request.golesLocalPredicho,
                golesVisitantePredicho = request.golesVisitantePredicho,
                puntosObtenidos = 0,
                quiniela = participacion.quiniela
            )
        }

        val pronosticoGuardado = pronosticoRepository.save(pronostico)
        return pronosticoGuardado.toDTO()
    }

    @Transactional
    override fun guardarPronosticosBatch(request: CrearPronosticosBatchRequest, email: String): CrearPronosticosBatchResponse {
        val usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        val participacion = participacionRepository.findByUsuarioIdAndQuinielaId(usuario.id, request.idQuiniela)
            .orElseThrow { NotFoundException("No participas en esta quiniela") }

        val pronosticosGuardados = mutableListOf<PronosticoDTO>()

        for (item in request.pronosticos) {
            val partido = partidoRepository.findById(item.idPartido)
                .orElseThrow { NotFoundException("Partido no encontrado: ${item.idPartido}") }

            if (partido.fechaHora.isBefore(LocalDateTime.now())) {
                continue
            }

            if (partido.estado != EstadoPartido.PENDIENTE) {
                continue
            }

            val pronosticoExistente = pronosticoRepository.findByParticipacionIdAndPartidoId(
                participacion.id,
                item.idPartido
            )

            val pronostico = if (pronosticoExistente != null) {
                pronosticoExistente.golesLocalPredicho = item.golesLocalPredicho
                pronosticoExistente.golesVisitantePredicho = item.golesVisitantePredicho
                pronosticoExistente
            } else {
                Pronostico(
                    participacion = participacion,
                    partido = partido,
                    golesLocalPredicho = item.golesLocalPredicho,
                    golesVisitantePredicho = item.golesVisitantePredicho,
                    puntosObtenidos = 0,
                    quiniela = participacion.quiniela
                )
            }

            val guardado = pronosticoRepository.save(pronostico)
            pronosticosGuardados.add(guardado.toDTO())
        }

        return CrearPronosticosBatchResponse(
            pronosticosGuardados = pronosticosGuardados.size,
            pronosticos = pronosticosGuardados
        )
    }

    override fun getPronosticosPorPartido(quinielaId: Long, partidoId: Long, email: String): PronosticosPorPartidoDTO {
        val partido = partidoRepository.findById(partidoId)
            .orElseThrow { NotFoundException("Partido no encontrado") }

        if (partido.fechaHora.isAfter(LocalDateTime.now()) && (partido.estado == EstadoPartido.PENDIENTE || partido.estado == EstadoPartido.POR_COMENZAR)) {
            throw ForbiddenException("Los pronósticos solo se pueden ver después de que comience el partido")
        }

        val pronosticos = pronosticoRepository.findByPartidoId(partidoId)

        val pronosticosDTO = pronosticos.map { p ->
            PronosticoResumenDTO(
                usuario = UsuarioDTO(
                    id = p.participacion.usuario.id,
                    nombre = p.participacion.usuario.nombre,
                    email = p.participacion.usuario.email
                ),
                golesLocalPredicho = p.golesLocalPredicho,
                golesVisitantePredicho = p.golesVisitantePredicho,
                puntosObtenidos = p.puntosObtenidos
            )
        }

        return PronosticosPorPartidoDTO(
            partido = partido.toPartidoDTO(),
            pronosticos = pronosticosDTO
        )
    }

    private fun Pronostico.toDTO(): PronosticoDTO {
        val partido = this.partido
        return PronosticoDTO(
            id = id,
            usuario = UsuarioDTO(
                id = participacion.usuario.id,
                nombre = participacion.usuario.nombre,
                email = participacion.usuario.email
            ),
            partido = partido.toPartidoDTO(),
            golesLocalPredicho = golesLocalPredicho,
            golesVisitantePredicho = golesVisitantePredicho,
            puntosObtenidos = puntosObtenidos,
            quinielaId = quiniela?.id
        )
    }
}
