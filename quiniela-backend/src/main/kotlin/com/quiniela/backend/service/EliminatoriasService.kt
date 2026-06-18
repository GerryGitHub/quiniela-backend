package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.*
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EliminatoriasService(
    private val bracketSlotRepository: BracketSlotRepository,
    private val terceroMappingRepository: TerceroMappingRepository,
    private val grupoRepository: GrupoRepository,
    private val equipoRepository: EquipoRepository,
    private val partidoRepository: PartidoRepository,
    private val quinielaRepository: QuinielaRepository,
    private val participacionRepository: ParticipacionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val equipoEstadisticasRepository: EquipoEstadisticasRepository
) {

    fun getPreview(): BracketPreviewDTO {
        val rondaOrder = listOf("R32", "R16", "QF", "SF", "3RD", "FINAL")
        val slots = bracketSlotRepository.findAllByOrderByRondaAscOrdenAsc()
            .sortedBy { s -> rondaOrder.indexOf(s.ronda) * 100 + s.orden }
        val posiciones = getPosicionesGrupos()

        val tercerasRankeadas = rankearTercerasPosiciones(posiciones)
        val clasificados = tercerasRankeadas.take(8)
        val gruposClasificados = clasificados.map { it.grupo }.sorted().joinToString("")
        val terceroMapping = if (gruposClasificados.length >= 8) {
            terceroMappingRepository.findByCombinacion(gruposClasificados)
                .associate { it.slotCodigo to it.grupoOrigen }
        } else emptyMap()

        val asignacion3ros = asignarTercerosASlots(clasificados, slots.filter { s ->
            s.localTipo == "GRUPO_3" || s.visitanteTipo == "GRUPO_3"
        }, terceroMapping)

        val slotsMap = slots.associateBy { it.codigo }
        val resultadosPorSlot = mutableMapOf<String, Pair<String?, String?>>()
        val equiposUsados = mutableSetOf<String>()

        for (slot in slots) {
            val local = resolverEquipo(slot, true, posiciones, slotsMap, resultadosPorSlot, asignacion3ros, equiposUsados)
            val visitante = resolverEquipo(slot, false, posiciones, slotsMap, resultadosPorSlot, asignacion3ros, equiposUsados)
            resultadosPorSlot[slot.codigo] = Pair(local, visitante)
        }

        val rondasMap = linkedMapOf<String, MutableList<BracketSlotPreviewDTO>>()
        for (slot in slots) {
            val (local, visitante) = resultadosPorSlot[slot.codigo] ?: Pair(null, null)
            rondasMap.getOrPut(slot.ronda) { mutableListOf() } += BracketSlotPreviewDTO(
                codigo = slot.codigo, ronda = slot.ronda, orden = slot.orden,
                equipoLocal = local, equipoVisitante = visitante,
                localSlot = slotToOrigenDTO(slot, true), visitanteSlot = slotToOrigenDTO(slot, false),
                resuelto = local != null && visitante != null
            )
        }

        val hayGruposActivos = partidoRepository.countByEstado(EstadoPartido.PENDIENTE) > 0 ||
            partidoRepository.countByEstado(EstadoPartido.EN_CURSO) > 0

        return BracketPreviewDTO(rondas = rondasMap, gruposActivos = hayGruposActivos)
    }

    data class EquipoStats(
        val nombre: String,
        val puntos: Int,
        val dg: Int,
        val gf: Int,
        val fairPlay: Int,
        val ranking: Int
    )

    data class TerceroRankeado(
        val grupo: String,
        val nombreEquipo: String,
        val puntos: Int,
        val dg: Int,
        val gf: Int,
        val fairPlay: Int,
        val ranking: Int
    )

    private fun getPosicionesGrupos(): Map<String, List<EquipoStats>> {
        val grupos = grupoRepository.findAll()
        val partidos = partidoRepository.findAll()
        val estadisticas = equipoEstadisticasRepository.findAll().associateBy { it.equipo.id }

        val posiciones = mutableMapOf<String, List<EquipoStats>>()

        for (grupo in grupos) {
            val equipos = equipoRepository.findByGrupoId(grupo.id)
            val partidosGrupo = partidos.filter { it.grupo?.id == grupo.id }

            val tabla = equipos.mapNotNull { equipo ->
                var pts = 0; var dg = 0; var gf = 0; var pj = 0
                for (p in partidosGrupo) {
                    val golesL = p.golesLocalReal ?: continue
                    val golesV = p.golesVisitanteReal ?: continue
                    val esLocal = p.equipoLocal.id == equipo.id
                    val esVisitante = p.equipoVisitante.id == equipo.id
                    if (!esLocal && !esVisitante) continue
                    pj++
                    if (esLocal) { gf += golesL; dg += golesL - golesV; pts += if (golesL > golesV) 3 else if (golesL == golesV) 1 else 0 }
                    else { gf += golesV; dg += golesV - golesL; pts += if (golesV > golesL) 3 else if (golesV == golesL) 1 else 0 }
                }
                if (pj == 0) return@mapNotNull null
                val est = estadisticas[equipo.id]
                EquipoStats(equipo.nombre, pts, dg, gf, est?.puntosFairPlay ?: 0, est?.rankingFifa ?: 999)
            }.sortedWith(compareByDescending<EquipoStats> { it.puntos }
                .thenByDescending { it.dg }
                .thenByDescending { it.gf }
                .thenBy { it.fairPlay }
                .thenBy { it.ranking })

            posiciones[grupo.nombre] = tabla
        }
        return posiciones
    }

    private fun rankearTercerasPosiciones(posiciones: Map<String, List<EquipoStats>>): List<TerceroRankeado> {
        return posiciones.mapNotNull { (grupo, equipos) ->
            val tercero = equipos.getOrNull(2) ?: return@mapNotNull null
            TerceroRankeado(grupo, tercero.nombre, tercero.puntos, tercero.dg, tercero.gf, tercero.fairPlay, tercero.ranking)
        }.sortedWith(compareByDescending<TerceroRankeado> { it.puntos }
            .thenByDescending { it.dg }
            .thenByDescending { it.gf }
            .thenBy { it.fairPlay }
            .thenBy { it.ranking })
    }

    private fun asignarTercerosASlots(
        clasificados: List<TerceroRankeado>,
        slots3ros: List<BracketSlot>,
        mapping: Map<String, String>
    ): Map<String, String> {
        val asignacion = mutableMapOf<String, String>()
        val disponibles = clasificados.toMutableList()

        val entryPoints = mutableListOf<Pair<String, String>>()

        for (slot in slots3ros) {
            val gruposPool = if (slot.localTipo == "GRUPO_3") slot.localGrupos ?: "" else slot.visitanteGrupos ?: ""
            val slotCodigo = slot.codigo

            val grupoMapping = mapping[slotCodigo]
            if (grupoMapping != null && grupoMapping.length == 1 && grupoMapping[0] in gruposPool) {
                val tercero = disponibles.find { it.grupo == grupoMapping }
                if (tercero != null) {
                    entryPoints.add(Pair(slotCodigo, tercero.nombreEquipo))
                    disponibles.remove(tercero)
                    continue
                }
            }
            entryPoints.add(Pair(slotCodigo, gruposPool))
        }

        val unresolved = entryPoints.filter { it.second.all { c -> c.isUpperCase() } }
        val resolved = entryPoints.filter { it.second.any { c -> !c.isUpperCase() || c in 'á'..'ú' } }

        val slotsPendientes = unresolved.map { (slot, pool) ->
            val grupos = pool.map { it.toString() }
            SlotPendiente(slot, grupos.toSet())
        }.sortedBy { it.pool.size }

        for (sp in slotsPendientes) {
            val elegido = disponibles.filter { it.grupo in sp.pool }.maxByOrNull { idxOf(clasificados, it) }
            if (elegido != null) {
                asignacion[sp.codigo] = elegido.nombreEquipo
                disponibles.remove(elegido)
            }
        }

        for ((slot, nombre) in entryPoints) {
            if (nombre.any { c -> !c.isUpperCase() || c in 'á'..'ú' }) {
                asignacion[slot] = nombre
            }
        }

        return asignacion
    }

    data class SlotPendiente(val codigo: String, val pool: Set<String>)

    private fun idxOf(lista: List<TerceroRankeado>, item: TerceroRankeado): Int {
        return lista.indexOf(item)
    }

    private fun resolverEquipo(
        slot: BracketSlot, isLocal: Boolean,
        posiciones: Map<String, List<EquipoStats>>,
        slotsMap: Map<String, BracketSlot>,
        resultadosPrevios: Map<String, Pair<String?, String?>>,
        asignacion3ros: Map<String, String>,
        equiposUsados: MutableSet<String>
    ): String? {
        val tipo = if (isLocal) slot.localTipo else slot.visitanteTipo
        val grupos = if (isLocal) slot.localGrupos else slot.visitanteGrupos
        val partidoOrigen = if (isLocal) slot.localPartidoOrigen else slot.visitantePartidoOrigen
        val esGanador = if (isLocal) slot.localEsGanador else slot.visitanteEsGanador

        return when (tipo) {
            "GRUPO_1" -> posiciones[grupos]?.firstOrNull()?.nombre
            "GRUPO_2" -> posiciones[grupos]?.getOrNull(1)?.nombre
            "GRUPO_3" -> asignacion3ros[slot.codigo]
            "WINNER" -> {
                val origen = partidoOrigen ?: return null
                val res = resultadosPrevios[origen.codigo] ?: return null
                if (esGanador == true) res.first else res.second
            }
            "LOSER" -> {
                val origen = partidoOrigen ?: return null
                val res = resultadosPrevios[origen.codigo] ?: return null
                if (esGanador == false) res.second else res.first
            }
            else -> null
        }
    }

    private fun slotToOrigenDTO(slot: BracketSlot, isLocal: Boolean): SlotOrigenDTO? {
        val tipo = if (isLocal) slot.localTipo else slot.visitanteTipo
        val grupos = if (isLocal) slot.localGrupos else slot.visitanteGrupos
        val partidoOrigen = if (isLocal) slot.localPartidoOrigen else slot.visitantePartidoOrigen
        val esGanador = if (isLocal) slot.localEsGanador else slot.visitanteEsGanador
        return when (tipo) {
            "GRUPO_1", "GRUPO_2", "GRUPO_3" -> SlotOrigenDTO(tipo = tipo, grupos = grupos)
            "WINNER", "LOSER" -> SlotOrigenDTO(tipo = tipo, partidoOrigen = partidoOrigen?.codigo, esGanador = esGanador)
            else -> null
        }
    }

    @Transactional
    fun crearEliminatorias(request: CrearEliminatoriasRequest, email: String): CrearEliminatoriasResponse {
        val quinielaGrupos = quinielaRepository.findById(request.quinielaGruposId)
            .orElseThrow { NotFoundException("Quiniela de grupos no encontrada") }

        val admin = usuarioRepository.findByEmail(email)
            .orElseThrow { NotFoundException("Usuario no encontrado") }

        quinielaGrupos.estado = "FINALIZADA"
        quinielaRepository.save(quinielaGrupos)

        val nextRondas = listOf("R32", "R16", "QF", "SF", "3RD", "FINAL")
        val rondaIdx = nextRondas.indexOf(quinielaGrupos.ronda)
        val nuevaRonda = if (rondaIdx >= 0 && rondaIdx < nextRondas.size - 1) nextRondas[rondaIdx + 1] else "R32"
        if (nuevaRonda !in nextRondas) throw IllegalStateException("No hay más rondas disponibles")

        val preview = getPreview()
        val slotsRonda = preview.rondas[nuevaRonda]?.sortedBy { it.orden } ?: emptyList()
        val slotsResueltos = slotsRonda.filter { it.resuelto }
        if (slotsResueltos.isEmpty()) throw IllegalStateException("No hay suficientes partidos resueltos")

        val nombresOcupados = quinielaRepository.findAll().map { it.nombre }.toSet()
        var nombreFinal = request.nombreQuiniela
        var suffix = 1
        while (nombreFinal in nombresOcupados) { suffix++; nombreFinal = "${request.nombreQuiniela} ($suffix)" }

        val quiniela = Quiniela(
            nombre = nombreFinal, codigoInvitacion = generarCodigo(),
            administrador = admin, createdAt = LocalDateTime.now(),
            estado = "ACTIVA", ronda = nuevaRonda
        )
        val quinielaGuardada = quinielaRepository.save(quiniela)

        val participacionAdmin = Participacion(usuario = admin, quiniela = quinielaGuardada, puntosTotales = 0)
        participacionRepository.save(participacionAdmin)

        val partidos = slotsResueltos.mapNotNull { slot ->
            val eqLocal = slot.equipoLocal ?: return@mapNotNull null
            val eqVisitante = slot.equipoVisitante ?: return@mapNotNull null
            val equipoLocal = encontrarOCrearEquipo(eqLocal)
            val equipoVisitante = encontrarOCrearEquipo(eqVisitante)
            Partido(
                equipoLocal = equipoLocal, equipoVisitante = equipoVisitante,
                grupo = null, fechaHora = LocalDateTime.now().plusDays(7 + slot.orden.toLong()),
                estado = EstadoPartido.PENDIENTE, codigo = slot.codigo, quiniela = quinielaGuardada
            )
        }

        val partidosGuardados = partidoRepository.saveAll(partidos)

        return CrearEliminatoriasResponse(
            quinielaId = quinielaGuardada.id, nombre = nombreFinal,
            ronda = nuevaRonda, partidosCreados = partidosGuardados.size
        )
    }

    private fun encontrarOCrearEquipo(nombre: String): Equipo {
        return equipoRepository.findAll().find { it.nombre == nombre }
            ?: equipoRepository.save(Equipo(nombre = nombre))
    }

    private fun generarCodigo(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val codigo = (1..8).map { chars.random() }.joinToString("")
        return if (quinielaRepository.findByCodigoInvitacion(codigo).isPresent) generarCodigo() else codigo
    }

    fun getStatus(): EliminatoriasStatusDTO {
        val quinielasActivas = quinielaRepository.findAll().filter { it.estado == "ACTIVA" && (it.ronda == null || it.ronda == "GRUPOS") }
        val quinielasEliminatorias = quinielaRepository.findAll()
            .filter { it.ronda in listOf("R32", "R16", "QF", "SF", "3RD", "FINAL") }
            .sortedByDescending { it.createdAt }

        val faseGruposActiva = quinielasActivas.isNotEmpty()
        val rondaActual = quinielasEliminatorias.firstOrNull()?.ronda
        val quinielaGrupos = quinielasActivas.firstOrNull()?.id
            ?: quinielaRepository.findAll().filter { it.ronda == null }.firstOrNull()?.id

        val bracket = if (faseGruposActiva || quinielasEliminatorias.isNotEmpty()) getPreview() else null

        return EliminatoriasStatusDTO(
            faseGruposActiva = faseGruposActiva,
            quinielaGrupos = quinielaGrupos,
            rondaActual = rondaActual,
            bracket = bracket
        )
    }
}



