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
    var puntosObtenidos: Int = 0,
    @ManyToOne
    val quiniela: Quiniela? = null
) {
    fun calcularPuntos(): Int {
        val realLocal = partido.golesLocalReal ?: return 0
        val realVisitante = partido.golesVisitanteReal ?: return 0
        return when {
            golesLocalPredicho == realLocal && golesVisitantePredicho == realVisitante -> 10
            (golesLocalPredicho - golesVisitantePredicho) == (realLocal - realVisitante) -> 5
            (golesLocalPredicho > golesVisitantePredicho && realLocal > realVisitante) ||
            (golesLocalPredicho < golesVisitantePredicho && realLocal < realVisitante) -> 3
            else -> 0
        }
    }

    fun esExacto() = calcularPuntos() == 10
    fun esDiferencia() = calcularPuntos() == 5
    fun esResultado() = calcularPuntos() == 3
}
