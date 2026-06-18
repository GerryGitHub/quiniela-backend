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
    private val usuarioRepository: UsuarioRepository
) {

    fun getPreview(): BracketPreviewDTO {
        val slots = bracketSlotRepository.findAllByOrderByRondaAscOrdenAsc()
        val posiciones = getPosicionesGrupos()
        val tercerasPosiciones = getTercerasPosiciones(posiciones)
        val tercerosClasificados = tercerasPosiciones.take(8).map { it.first }
        val combinacion = tercerosClasificados.sorted().joinToString("")
        val terceroMapping = if (combinacion.length >= 8) {
            terceroMappingRepository.findByCombinacion(combinacion)
                .associate { it.slotCodigo to it.grupoOrigen }
        } else emptyMap()

        val slotsMap = slots.associateBy { it.codigo }
        val resultadosPorSlot = mutableMapOf<String, Pair<String?, String?>>()

        for (slot in slots) {
            val (local, visitante) = resolverSlot(
                slot, slotsMap, resultadosPorSlot, posiciones,
                terceroMapping, tercerasPosiciones
            )
            resultadosPorSlot[slot.codigo] = Pair(local, visitante)
        }

        val rondasMap = mutableMapOf<String, MutableList<BracketSlotPreviewDTO>>()
        for (slot in slots) {
            val (local, visitante) = resultadosPorSlot[slot.codigo] ?: Pair(null, null)
            val preview = BracketSlotPreviewDTO(
                codigo = slot.codigo,
                ronda = slot.ronda,
                orden = slot.orden,
                equipoLocal = local,
                equipoVisitante = visitante,
                localSlot = slotToOrigenDTO(slot, isLocal = true),
                visitanteSlot = slotToOrigenDTO(slot, isLocal = false),
                resuelto = local != null && visitante != null
            )
            rondasMap.getOrPut(slot.ronda) { mutableListOf() }.add(preview)
        }

        val hayGruposActivos = partidoRepository.countByEstado(EstadoPartido.PENDIENTE) > 0 ||
            partidoRepository.countByEstado(EstadoPartido.EN_CURSO) > 0

        return BracketPreviewDTO(rondas = rondasMap, gruposActivos = hayGruposActivos)
    }

    private fun resolverSlot(
        slot: BracketSlot,
        slotsMap: Map<String, BracketSlot>,
        resultadosPrevios: MutableMap<String, Pair<String?, String?>>,
        posiciones: Map<String, List<Pair<String, Int>>>,
        terceroMapping: Map<String, String>,
        tercerasPosiciones: List<Pair<String, Int>>
    ): Pair<String?, String?> {
        val local = resolverEquipo(
            slot.codigo, slot.localTipo, slot.localGrupos, slot.localPartidoOrigen,
            slot.localEsGanador, slotsMap, resultadosPrevios, posiciones,
            terceroMapping, tercerasPosiciones
        )
        val visitante = resolverEquipo(
            slot.codigo, slot.visitanteTipo, slot.visitanteGrupos, slot.visitantePartidoOrigen,
            slot.visitanteEsGanador, slotsMap, resultadosPrevios, posiciones,
            terceroMapping, tercerasPosiciones
        )
        return Pair(local, visitante)
    }

    private fun resolverEquipo(
        slotCodigo: String,
        tipo: String,
        grupos: String?,
        partidoOrigen: BracketSlot?,
        esGanador: Boolean?,
        slotsMap: Map<String, BracketSlot>,
        resultadosPrevios: MutableMap<String, Pair<String?, String?>>,
        posiciones: Map<String, List<Pair<String, Int>>>,
        terceroMapping: Map<String, String>,
        tercerasPosiciones: List<Pair<String, Int>>
    ): String? {
        return when (tipo) {
            "GRUPO_1" -> posiciones[grupos]?.firstOrNull()?.first
            "GRUPO_2" -> posiciones[grupos]?.getOrNull(1)?.first
            "GRUPO_3" -> {
                val gruposPool = grupos?.map { it.toString() } ?: return null
                val grupoAsignado = terceroMapping[slotCodigo]
                if (grupoAsignado != null && grupoAsignado in gruposPool) {
                    posiciones[grupoAsignado]?.getOrNull(2)?.first
                } else {
                    gruposPool.firstOrNull { g ->
                        tercerasPosiciones.any { it.first == g } &&
                            !equipoYaUsado(g, resultadosPrevios)
                    }?.let { posiciones[it]?.getOrNull(2)?.first }
                }
            }
            "WINNER" -> {
                val origen = partidoOrigen ?: return null
                resultadosPrevios[origen.codigo]?.first
            }
            "LOSER" -> {
                val origen = partidoOrigen ?: return null
                resultadosPrevios[origen.codigo]?.second
            }
            else -> null
        }
    }

    private fun equipoYaUsado(grupo: String, resultados: Map<String, Pair<String?, String?>>): Boolean {
        return resultados.values.any { (l, v) -> l?.startsWith("[$grupo]") == true || v?.startsWith("[$grupo]") == true }
    }

    private fun getPosicionesGrupos(): Map<String, List<Pair<String, Int>>> {
        val grupos = grupoRepository.findAll()
        val partidos = partidoRepository.findAll()
        val posiciones = mutableMapOf<String, List<Pair<String, Int>>>()

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
                Pair(equipo.nombre, pts * 10000 + gf * 100 + dg)
            }.sortedByDescending { it.second }

            posiciones[grupo.nombre] = tabla.map { Pair(it.first, it.second / 10000) }
        }
        return posiciones
    }

    private fun getTercerasPosiciones(posiciones: Map<String, List<Pair<String, Int>>>): List<Pair<String, Int>> {
        return posiciones.mapNotNull { (grupo, equipos) ->
            val tercero = equipos.getOrNull(2) ?: return@mapNotNull null
            Pair(grupo, tercero.second)
        }.sortedByDescending { it.second }
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
        if (slotsResueltos.isEmpty()) {
            throw IllegalStateException("No hay suficientes partidos resueltos para crear eliminatorias")
        }

        val nombresOcupados = quinielaRepository.findAll().map { it.nombre }.toSet()
        var nombreFinal = request.nombreQuiniela
        var suffix = 1
        while (nombreFinal in nombresOcupados) {
            suffix++; nombreFinal = "${request.nombreQuiniela} ($suffix)"
        }

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

    fun getEliminatoriasStatus(): EliminatoriasStatusDTO {
        val quinielasActivas = quinielaRepository.findAll().filter { q ->
            q.estado == "ACTIVA" && (q.ronda == null || q.ronda == "GRUPOS")
        }
        val quinielasEliminatorias = quinielaRepository.findAll()
            .filter { q -> q.ronda in listOf("R32", "R16", "QF", "SF", "3RD", "FINAL") }
            .sortedByDescending { it.createdAt }

        val faseGruposActiva = quinielasActivas.isNotEmpty()
        val rondaActual = quinielasEliminatorias.firstOrNull()?.ronda
        val quinielaGrupos = quinielasActivas.firstOrNull()?.id
            ?: quinielaRepository.findAll().filter { q -> q.ronda == null }.firstOrNull()?.id

        val bracket = if (faseGruposActiva || quinielasEliminatorias.isNotEmpty()) getPreview() else null

        return EliminatoriasStatusDTO(
            faseGruposActiva = faseGruposActiva,
            quinielaGrupos = quinielaGrupos,
            rondaActual = rondaActual,
            bracket = bracket
        )
    }
}
