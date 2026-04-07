package com.quiniela.backend.entity

import jakarta.persistence.*

@Entity
class Pronostico(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne
    val participacion: Participacion,
    @ManyToOne
    val partido: Partido,
    var golesLocalPredicho: Int,
    var golesVisitantePredicho: Int,
    var puntosObtenidos: Int = 0
)
