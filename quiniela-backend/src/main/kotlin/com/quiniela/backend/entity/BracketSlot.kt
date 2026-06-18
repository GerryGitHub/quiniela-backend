package com.quiniela.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "bracket_slot")
class BracketSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val codigo: String,
    val ronda: String,
    val orden: Int,
    val localTipo: String,
    val localGrupos: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_partido_origen_id")
    val localPartidoOrigen: BracketSlot? = null,
    val localEsGanador: Boolean? = null,
    val visitanteTipo: String,
    val visitanteGrupos: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitante_partido_origen_id")
    val visitantePartidoOrigen: BracketSlot? = null,
    val visitanteEsGanador: Boolean? = null
)
