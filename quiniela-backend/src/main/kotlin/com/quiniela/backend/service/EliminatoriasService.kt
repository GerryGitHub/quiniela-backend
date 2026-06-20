package com.quiniela.backend.service

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.*
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Service
class EliminatoriasService(
    private val bracketSlotRepository: BracketSlotRepository,
    private val grupoRepository: GrupoRepository,
    private val equipoRepository: EquipoRepository,
    private val partidoRepository: PartidoRepository,
    private val quinielaRepository: QuinielaRepository,
    private val participacionRepository: ParticipacionRepository,
    private val usuarioRepository: UsuarioRepository,
    private val equipoEstadisticasRepository: EquipoEstadisticasRepository,
    private val thirdPlaceResolver: ThirdPlaceResolver
) {

    companion object {
        private val logger = LoggerFactory.getLogger(EliminatoriasService::class.java)
    }

    fun getPreview(): BracketPreviewDTO {
        val rondaOrder = Ronda.ALL
        val slots = bracketSlotRepository.findAllByOrderByRondaAscOrdenAsc()
            .sortedBy { s -> rondaOrder.indexOf(Ronda.from(s.ronda)) * 100 + s.orden }
        val posiciones = getPosicionesGrupos()

        val tercerasRankeadas = rankearTercerasPosiciones(posiciones)
        val clasificados = tercerasRankeadas.take(8)

        val asignacion3ros = asignarTercerosASlots(clasificados)
        tercerosLog(clasificados)
        asignacionLog(asignacion3ros, slots.filter { s -> s.localTipo == SlotTipo.GRUPO_3.tipo || s.visitanteTipo == SlotTipo.GRUPO_3.tipo })

        val resultadosPorSlot = mutableMapOf<String, Pair<String?, String?>>()

        for (slot in slots) {
            if (slot.ronda == Ronda.R32.ronda) {
                val local = resolverEquipo(slot, true, posiciones, resultadosPorSlot, asignacion3ros)
                val visitante = resolverEquipo(slot, false, posiciones, resultadosPorSlot, asignacion3ros)
                resultadosPorSlot[slot.codigo] = Pair(local, visitante)
            } else {
                val local = resolverReferencia(slot, true)
                val visitante = resolverReferencia(slot, false)
                resultadosPorSlot[slot.codigo] = Pair(local, visitante)
            }
        }

        val rondasMap = linkedMapOf<String, MutableList<BracketSlotPreviewDTO>>()
        for (slot in slots) {
            val (local, visitante) = resultadosPorSlot[slot.codigo] ?: Pair(null, null)
            rondasMap.getOrPut(slot.ronda) { mutableListOf() } += BracketSlotPreviewDTO(
                codigo = slot.codigo, ronda = slot.ronda, orden = slot.orden,
                equipoLocal = local, equipoVisitante = visitante,
                localSlot = slotToOrigenDTO(slot, true), visitanteSlot = slotToOrigenDTO(slot, false),
                resuelto = slot.ronda == Ronda.R32.ronda && local != null && visitante != null
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
                .thenByDescending { it.fairPlay }
                .thenBy { it.ranking })

            posiciones[grupo.nombre] = tabla
        }
        logger.info("=== POSICIONES GRUPOS ===")
        posiciones.forEach { (g, eqs) ->
            eqs.forEachIndexed { i, e ->
                logger.info("Grupo $g #${i+1}: ${e.nombre} pts=${e.puntos} dg=${e.dg} gf=${e.gf} fp=${e.fairPlay} rank=${e.ranking}")
            }
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
            .thenByDescending { it.fairPlay }
            .thenBy { it.ranking })
    }

    private fun tercerosLog(clasificados: List<TerceroRankeado>) {
        logger.info("=== TERCEROS CLASIFICADOS ===")
        clasificados.forEachIndexed { i, t ->
            logger.info("#${i+1}: Grupo ${t.grupo} ${t.nombreEquipo} pts=${t.puntos} dg=${t.dg} gf=${t.gf} fp=${t.fairPlay} rank=${t.ranking}")
        }
    }

    private fun asignacionLog(asignacion: Map<String, String>, slots3ros: List<BracketSlot>) {
        logger.info("=== ASIGNACION TERCEROS ===")
        slots3ros.forEach { slot ->
            val eq = asignacion[slot.codigo]
            val pool = if (slot.localTipo == SlotTipo.GRUPO_3.tipo) slot.localGrupos ?: "" else slot.visitanteGrupos ?: ""
            logger.info("${slot.codigo} pool=$pool -> ${eq ?: "POR DEFINIR"}")
        }
    }

    private fun asignarTercerosASlots(
        clasificados: List<TerceroRankeado>
    ): Map<String, String> {
        if (clasificados.size < 8) return emptyMap()

        val gruposSet = clasificados.map { it.grupo }.toSet()
        val option = thirdPlaceResolver.resolveOption(gruposSet)
        val combo = thirdPlaceResolver.resolveThirdPlaces(option)

        val matchToSlot = mapOf(
            Constantes.SLOT_P79 to combo.p79, Constantes.SLOT_P85 to combo.p85, Constantes.SLOT_P81 to combo.p81,
            Constantes.SLOT_P74 to combo.p74, Constantes.SLOT_P82 to combo.p82, Constantes.SLOT_P77 to combo.p77,
            Constantes.SLOT_P87 to combo.p87, Constantes.SLOT_P80 to combo.p80
        )

        val terceroPorGrupo = clasificados.associate { it.grupo to it.nombreEquipo }

        return matchToSlot.mapNotNull { (slotCodigo, grupoOrigen) ->
            val equipo = terceroPorGrupo[grupoOrigen] ?: return@mapNotNull null
            slotCodigo to equipo
        }.toMap()
    }

    private fun resolverReferencia(slot: BracketSlot, isLocal: Boolean): String? {
        val partidoOrigen = if (isLocal) slot.localPartidoOrigen else slot.visitantePartidoOrigen
        val esGanador = if (isLocal) slot.localEsGanador else slot.visitanteEsGanador
        val prefijo = if (esGanador == true) Constantes.PREFIJO_WINNER else if (esGanador == false) Constantes.PREFIJO_LOSER else null ?: return null
        val origen = partidoOrigen ?: return null
        return "$prefijo${origen.codigo}"
    }

    private fun resolverEquipo(
        slot: BracketSlot, isLocal: Boolean,
        posiciones: Map<String, List<EquipoStats>>,
        resultadosPrevios: Map<String, Pair<String?, String?>>,
        asignacion3ros: Map<String, String>
    ): String? {
        val tipo = if (isLocal) slot.localTipo else slot.visitanteTipo
        val grupos = if (isLocal) slot.localGrupos else slot.visitanteGrupos
        val partidoOrigen = if (isLocal) slot.localPartidoOrigen else slot.visitantePartidoOrigen
        val esGanador = if (isLocal) slot.localEsGanador else slot.visitanteEsGanador

        return when (tipo) {
            SlotTipo.GRUPO_1.tipo -> posiciones[grupos]?.firstOrNull()?.nombre
            SlotTipo.GRUPO_2.tipo -> posiciones[grupos]?.getOrNull(1)?.nombre
            SlotTipo.GRUPO_3.tipo -> asignacion3ros[slot.codigo]
            SlotTipo.WINNER.tipo -> {
                val origen = partidoOrigen ?: return null
                val res = resultadosPrevios[origen.codigo] ?: return null
                if (esGanador == true) res.first else res.second
            }
            SlotTipo.LOSER.tipo -> {
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
            SlotTipo.GRUPO_1.tipo, SlotTipo.GRUPO_2.tipo, SlotTipo.GRUPO_3.tipo ->
                SlotOrigenDTO(tipo = tipo, grupos = grupos)
            SlotTipo.WINNER.tipo, SlotTipo.LOSER.tipo ->
                SlotOrigenDTO(tipo = tipo, partidoOrigen = partidoOrigen?.codigo, esGanador = esGanador)
            else -> null
        }
    }

    @Transactional
    fun crearEliminatorias(request: CrearEliminatoriasRequest, email: String): CrearEliminatoriasResponse {
        val quinielaGrupos = quinielaRepository.findById(request.quinielaGruposId)
            .orElseThrow { NotFoundException("Quiniela de grupos no encontrada") }

        val admin = usuarioRepository.findByEmail(email)
            .orElseThrow { NotFoundException("Usuario no encontrado") }

        quinielaGrupos.estado = EstadoQuiniela.FINALIZADA.estado
        quinielaRepository.save(quinielaGrupos)

        val nextRondas = Ronda.ALL
        val rondaIdx = quinielaGrupos.ronda?.let { r -> nextRondas.indexOf(Ronda.from(r)) } ?: -1
        val nuevaRonda = if (rondaIdx >= 0 && rondaIdx < nextRondas.size - 1) nextRondas[rondaIdx + 1].ronda else Ronda.R32.ronda

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
            estado = EstadoQuiniela.ACTIVA.estado, ronda = nuevaRonda
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
                estado = EstadoPartido.PENDIENTE, quiniela = quinielaGuardada
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
        val quinielasActivas = quinielaRepository.findAll().filter { it.estado == EstadoQuiniela.ACTIVA.estado && (it.ronda == null || it.ronda == EstadoQuiniela.GRUPOS.estado) }
        val quinielasEliminatorias = quinielaRepository.findAll()
            .filter { Ronda.ALL.any { r -> it.ronda == r.ronda } }
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



