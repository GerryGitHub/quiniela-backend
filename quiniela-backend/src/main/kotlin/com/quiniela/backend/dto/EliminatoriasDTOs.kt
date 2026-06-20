package com.quiniela.backend.dto

data class BracketSlotPreviewDTO(
    val codigo: String,
    val ronda: String,
    val orden: Int,
    val equipoLocal: String?,
    val equipoVisitante: String?,
    val localSlot: SlotOrigenDTO?,
    val visitanteSlot: SlotOrigenDTO?,
    val resuelto: Boolean
)

data class SlotOrigenDTO(
    val tipo: String,
    val grupos: String? = null,
    val partidoOrigen: String? = null,
    val esGanador: Boolean? = null
)

data class BracketPreviewDTO(
    val rondas: Map<String, List<BracketSlotPreviewDTO>>,
    val gruposActivos: Boolean
)

data class EliminatoriasStatusDTO(
    val faseGruposActiva: Boolean,
    val quinielaGrupos: Long?,
    val rondaActual: String?,
    val bracket: BracketPreviewDTO?
)

data class CrearEliminatoriasRequest(
    val nombreQuiniela: String,
    val quinielaGruposId: Long
)

data class CrearEliminatoriasResponse(
    val quinielaId: Long,
    val nombre: String,
    val ronda: String,
    val partidosCreados: Int
)
