package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.entity.Partido
import com.quiniela.backend.entity.Equipo
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GrupoService(
    private val grupoRepository: GrupoRepository,
    private val equipoRepository: EquipoRepository,
    private val partidoRepository: PartidoRepository
) {

    fun getAllGrupos(): TablaGruposDTO {
        val grupos = grupoRepository.findAll()
        
        val gruposDTO = grupos.map { grupo ->
            val equipos = equipoRepository.findByGrupoId(grupo.id).map { it.toDTO() }
            val partidos = partidoRepository.findAll()
                .filter { it.grupo.id == grupo.id }
                .sortedBy { it.fechaHora }
                .map { it.toDTO() }
            
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

    fun getGrupo(nombre: String): GrupoDTO {
        val grupo = grupoRepository.findByNombre(nombre) ?: throw NotFoundException("Grupo no encontrado")
        val equipos = equipoRepository.findByGrupoId(grupo.id).map { it.toDTO() }
        val partidos = partidoRepository.findAll()
            .filter { it.grupo.id == grupo.id }
            .sortedBy { it.fechaHora }
            .map { it.toDTO() }
        
        return GrupoDTO(
            id = grupo.id,
            nombre = grupo.nombre,
            pais = equipos.joinToString(", ") { it.nombre },
            selecciones = equipos,
            partidos = partidos
        )
    }

    @Transactional
    fun actualizarResultadoPartido(partidoId: Long, request: ActualizarResultadoRequest): PartidoDTO {
        val partido = partidoRepository.findById(partidoId)
            .orElseThrow { NotFoundException("Partido no encontrado") }
        
        partido.golesLocalReal = request.golesLocal
        partido.golesVisitanteReal = request.golesVisitante
        partido.estado = EstadoPartido.FINALIZADO
        
        return partidoRepository.save(partido).toDTO()
    }

    fun getPartidosGrupos(): List<PartidoDTO> {
        return partidoRepository.findAllByOrderByFechaHoraAsc().map { it.toDTO() }
    }

    private fun Equipo.toDTO() = SeleccionDTO(
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