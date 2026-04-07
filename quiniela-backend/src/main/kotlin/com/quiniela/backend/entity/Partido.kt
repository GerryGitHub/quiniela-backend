package com.quiniela.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Partido(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val equipoLocal: String,
    val equipoVisitante: String,
    val fechaHora: LocalDateTime,
    var golesLocalReal: Int? = null,
    var golesVisitanteReal: Int? = null,
    @Enumerated(EnumType.STRING)
    var estado: EstadoPartido = EstadoPartido.PENDIENTE
)
