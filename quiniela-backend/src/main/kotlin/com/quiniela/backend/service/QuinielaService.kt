package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.Participacion
import com.quiniela.backend.entity.Quiniela
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QuinielaService(
    private val quinielaRepository: QuinielaRepository,
    private val usuarioRepository: UsuarioRepository,
    private val participacionRepository: ParticipacionRepository,
    private val partidoRepository: PartidoRepository
) {

    fun getQuinielas(email: String): List<QuinielaResumenDTO> {
        val usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        val resultado = mutableListOf<QuinielaResumenDTO>()

        val comoAdmin = quinielaRepository.findByAdministradorId(usuario.id)
        comoAdmin.forEach { q ->
            val participacion = participacionRepository.findByUsuarioIdAndQuinielaId(usuario.id, q.id)
            resultado.add(
                QuinielaResumenDTO(
                    id = q.id,
                    nombre = q.nombre,
                    codigoInvitacion = q.codigoInvitacion,
                    puntosTotales = participacion.map { it.puntosTotales }.orElse(0)
                )
            )
        }

        val comoParticipante = quinielaRepository.findByParticipanteId(usuario.id)
        comoParticipante.forEach { q ->
            if (resultado.none { it.id == q.id }) {
                val participacion = participacionRepository.findByUsuarioIdAndQuinielaId(usuario.id, q.id)
                resultado.add(
                    QuinielaResumenDTO(
                        id = q.id,
                        nombre = q.nombre,
                        codigoInvitacion = q.codigoInvitacion,
                        puntosTotales = participacion.map { it.puntosTotales }.orElse(0)
                    )
                )
            }
        }

        return resultado
    }

    @Transactional
    fun crearQuiniela(request: CrearQuinielaRequest, email: String): QuinielaDTO {
        if (request.nombre.isBlank()) {
            throw IllegalArgumentException("El nombre de la quiniela es requerido")
        }
        if (quinielaRepository.existsByNombre(request.nombre)) {
            throw IllegalArgumentException("Ya existe una quiniela con ese nombre")
        }

        val usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        val quiniela = Quiniela(
            nombre = request.nombre,
            codigoInvitacion = request.codigoInvitacion,
            administrador = usuario
        )
        val quinielaGuardada = quinielaRepository.save(quiniela)

        val participacion = Participacion(
            usuario = usuario,
            quiniela = quinielaGuardada,
            puntosTotales = 0
        )
        participacionRepository.save(participacion)

        return QuinielaDTO(
            id = quinielaGuardada.id,
            nombre = quinielaGuardada.nombre,
            codigoInvitacion = quinielaGuardada.codigoInvitacion,
            administrador = UsuarioDTO(usuario.id, usuario.nombre, usuario.email),
            participantes = listOf(UsuarioDTO(usuario.id, usuario.nombre, usuario.email))
        )
    }

    fun getQuinielaDetalle(id: Long, email: String): QuinielaDetalleDTO {
        val quiniela = quinielaRepository.findById(id)
            .orElseThrow { NotFoundException("Quiniela no encontrada") }

        val participantes = participacionRepository.findByQuinielaIdOrderByPuntosDesc(id)
            .map { p ->
                UsuarioDTO(
                    id = p.usuario.id,
                    nombre = p.usuario.nombre,
                    email = p.usuario.email,
                    puntosTotales = p.puntosTotales
                )
            }

        val partidos = partidoRepository.findAll()
            .map { p ->
                PartidoDTO(
                    id = p.id,
                    equipoLocal = p.equipoLocal.nombre,
                    equipoVisitante = p.equipoVisitante.nombre,
                    fechaHora = p.fechaHora.toString(),
                    grupo = p.grupo.nombre,
                    grupoId = p.grupo.id,
                    equipoLocalId = p.equipoLocal.id,
                    equipoVisitanteId = p.equipoVisitante.id,
                    golesLocalReal = p.golesLocalReal,
                    golesVisitanteReal = p.golesVisitanteReal,
                    estado = p.estado.name
                )
            }

        return QuinielaDetalleDTO(
            id = quiniela.id,
            nombre = quiniela.nombre,
            codigoInvitacion = quiniela.codigoInvitacion,
            administrador = UsuarioDTO(
                quiniela.administrador.id,
                quiniela.administrador.nombre,
                quiniela.administrador.email
            ),
            participantes = participantes,
            partidos = partidos
        )
    }

    @Transactional
    fun unirseQuiniela(request: UnirseQuinielaRequest, email: String): QuinielaDTO {
        println("UnirseQuiniela - email: $email, codigo: ${request.codigoInvitacion}")
        
        val usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        val quiniela = quinielaRepository.findByCodigoInvitacion(request.codigoInvitacion)
            .orElseThrow { NotFoundException("Quiniela no encontrada con ese código") }

        println("UnirseQuiniela - usuario: ${usuario.id}, quiniela: ${quiniela.id}")

        if (participacionRepository.existsByUsuarioIdAndQuinielaId(usuario.id, quiniela.id)) {
            throw IllegalArgumentException("Ya estás participando en esta quiniela")
        }

        val participacion = Participacion(
            usuario = usuario,
            quiniela = quiniela,
            puntosTotales = 0
        )
        participacionRepository.save(participacion)

        val participantes = participacionRepository.findByQuinielaIdOrderByPuntosDesc(quiniela.id)
            .map { p ->
                UsuarioDTO(
                    id = p.usuario.id,
                    nombre = p.usuario.nombre,
                    email = p.usuario.email,
                    puntosTotales = p.puntosTotales
                )
            }

        return QuinielaDTO(
            id = quiniela.id,
            nombre = quiniela.nombre,
            codigoInvitacion = quiniela.codigoInvitacion,
            administrador = UsuarioDTO(
                quiniela.administrador.id,
                quiniela.administrador.nombre,
                quiniela.administrador.email
            ),
            participantes = participantes
        )
    }

    fun getLeaderboard(quinielaId: Long): List<LeaderboardEntryDTO> {
      quinielaRepository.findById(quinielaId)
            .orElseThrow { NotFoundException("Quiniela no encontrada") }

        val participantes = participacionRepository.findByQuinielaIdOrderByPuntosDesc(quinielaId)

        return participantes.mapIndexed { index, p ->
            LeaderboardEntryDTO(
                posicion = index + 1,
                usuario = UsuarioDTO(
                    id = p.usuario.id,
                    nombre = p.usuario.nombre,
                    email = p.usuario.email
                ),
                puntosTotales = p.puntosTotales
            )
        }
    }
}
