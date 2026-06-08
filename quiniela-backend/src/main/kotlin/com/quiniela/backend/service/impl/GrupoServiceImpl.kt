package com.quiniela.backend.service.impl

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.mapper.toPartidoDTO
import com.quiniela.backend.repository.EquipoRepository
import com.quiniela.backend.repository.GrupoRepository
import com.quiniela.backend.repository.PartidoRepository
import com.quiniela.backend.service.GrupoService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GrupoServiceImpl(
    private val grupoRepository: GrupoRepository,
    private val equipoRepository: EquipoRepository,
    private val partidoRepository: PartidoRepository
) : GrupoService {

    override fun getAllGrupos(): TablaGruposDTO {
        val grupos = grupoRepository.findAll()

        val gruposDTO = grupos.map { grupo ->
            val equipos = equipoRepository.findByGrupoId(grupo.id).map { it.toSeleccionDTO() }
            val partidos = partidoRepository.findAll()
                .filter { it.grupo.id == grupo.id }
                .sortedBy { it.fechaHora }
                .map { it.toPartidoDTO() }

            GrupoDTO(
                id = grupo.id,
                nombre = grupo.nombre,
                pais = equipos.joinToString(", ") { it.nombre },
                selecciones = equipos,
                partidos = partidos
            )
        }

        return TablaGruposDTO(grupos = gruposDTO)
    }

    override fun getGrupo(nombre: String): GrupoDTO {
        val grupo = grupoRepository.findByNombre(nombre) ?: throw NotFoundException("Grupo no encontrado")
        val equipos = equipoRepository.findByGrupoId(grupo.id).map { it.toSeleccionDTO() }
        val partidos = partidoRepository.findAll()
            .filter { it.grupo.id == grupo.id }
            .sortedBy { it.fechaHora }
            .map { it.toPartidoDTO() }

        return GrupoDTO(
            id = grupo.id,
            nombre = grupo.nombre,
            pais = equipos.joinToString(", ") { it.nombre },
            selecciones = equipos,
            partidos = partidos
        )
    }

    @Transactional
    override fun actualizarResultadoPartido(partidoId: Long, request: ActualizarResultadoRequest): PartidoDTO {
        val partido = partidoRepository.findById(partidoId)
            .orElseThrow { NotFoundException("Partido no encontrado") }

        partido.golesLocalReal = request.golesLocal
        partido.golesVisitanteReal = request.golesVisitante
        partido.estado = EstadoPartido.FINALIZADO

        return partidoRepository.save(partido).toPartidoDTO()
    }

    override fun getPartidosGrupos(): List<PartidoDTO> {
        return partidoRepository.findAllByOrderByFechaHoraAsc().map { it.toPartidoDTO() }
    }

    private fun com.quiniela.backend.entity.Equipo.toSeleccionDTO() = SeleccionDTO(
        id = id,
        nombre = nombre,
        pais = nombre,
        grupo = grupo?.nombre ?: "",
        bandera = banderaUrl,
        partidosJugados = 0,
        partidosGanados = 0,
        partidosEmpatados = 0,
        partidosPerdidos = 0,
        golesAFavor = 0,
        golesEnContra = 0,
        puntos = 0,
        diferenciaGoles = 0
    )
}
