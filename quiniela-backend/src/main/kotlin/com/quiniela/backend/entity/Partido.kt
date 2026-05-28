package com.quiniela.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Partido(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne
    @JoinColumn(name = "equipo_local_id")
    val equipoLocal: Equipo,
    
    @ManyToOne
    @JoinColumn(name = "equipo_visitante_id")
    val equipoVisitante: Equipo,
    
    @ManyToOne
    @JoinColumn(name = "grupo_id")
    val grupo: Grupo,
    
    val fechaHora: LocalDateTime,
    var golesLocalReal: Int? = null,
    var golesVisitanteReal: Int? = null,
    var minutosJugados: Int? = null,
    @Enumerated(EnumType.STRING)
    var estado: EstadoPartido = EstadoPartido.PENDIENTE
)