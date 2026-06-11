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
            val partidosEntity = partidoRepository.findAll()
                .filter { it.grupo.id == grupo.id }
                .sortedBy { it.fechaHora }
            val equipos = equipoRepository.findByGrupoId(grupo.id).map { it.toSeleccionDTO(partidosEntity) }
            val partidos = partidosEntity.map { it.toPartidoDTO() }

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
        val partidosEntity = partidoRepository.findAll()
            .filter { it.grupo.id == grupo.id }
            .sortedBy { it.fechaHora }
        val equipos = equipoRepository.findByGrupoId(grupo.id).map { it.toSeleccionDTO(partidosEntity) }
        val partidos = partidosEntity.map { it.toPartidoDTO() }

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

    private fun com.quiniela.backend.entity.Equipo.toSeleccionDTO(partidos: List<com.quiniela.backend.entity.Partido>): SeleccionDTO {
        var pj = 0; var pg = 0; var pe = 0; var pp = 0
        var gf = 0; var gc = 0

        for (partido in partidos) {
            val golesL = partido.golesLocalReal ?: continue
            val golesV = partido.golesVisitanteReal ?: continue
            val esLocal = partido.equipoLocal.id == id
            val esVisitante = partido.equipoVisitante.id == id
            if (!esLocal && !esVisitante) continue

            pj++
            if (esLocal) {
                gf += golesL; gc += golesV
                when {
                    golesL > golesV -> pg++
                    golesL == golesV -> pe++
                    else -> pp++
                }
            } else {
                gf += golesV; gc += golesL
                when {
                    golesV > golesL -> pg++
                    golesV == golesL -> pe++
                    else -> pp++
                }
            }
        }

        return SeleccionDTO(
            id = id,
            nombre = nombre,
            pais = nombre,
            grupo = grupo?.nombre ?: "",
            bandera = banderaUrl,
            partidosJugados = pj,
            partidosGanados = pg,
            partidosEmpatados = pe,
            partidosPerdidos = pp,
            golesAFavor = gf,
            golesEnContra = gc,
            puntos = pg * 3 + pe,
            diferenciaGoles = gf - gc
        )
    }
}
