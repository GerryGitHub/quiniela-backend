package com.quiniela.backend.entity

import jakarta.persistence.*

@Entity
class Participacion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne
    val usuario: Usuario,
    @ManyToOne
    val quiniela: Quiniela,
    var puntosTotales: Int = 0
)
