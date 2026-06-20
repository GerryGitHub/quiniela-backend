package com.quiniela.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "equipo_estadisticas")
class EquipoEstadisticas(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipo_id", nullable = false, unique = true)
    val equipo: Equipo,

    var rankingFifa: Int? = null,

    var puntosFairPlay: Int = 0
)
