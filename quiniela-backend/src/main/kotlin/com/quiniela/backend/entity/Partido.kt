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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    val grupo: Grupo? = null,

    val fechaHora: LocalDateTime,
    var golesLocalReal: Int? = null,
    var golesVisitanteReal: Int? = null,
    var minutosJugados: Int? = null,
    @Enumerated(EnumType.STRING)
    var estado: EstadoPartido = EstadoPartido.PENDIENTE,
    val codigo: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiniela_id")
    val quiniela: Quiniela? = null
) {
    fun isFinished() = estado == EstadoPartido.FINALIZADO
    fun isLive() = estado == EstadoPartido.EN_CURSO
    fun isPending() = estado == EstadoPartido.PENDIENTE
    fun isAboutToStart() = estado == EstadoPartido.POR_COMENZAR
    fun hasResult() = golesLocalReal != null && golesVisitanteReal != null
    fun resultado(): Pair<Int, Int>? = if (hasResult()) Pair(golesLocalReal!!, golesVisitanteReal!!) else null
}
