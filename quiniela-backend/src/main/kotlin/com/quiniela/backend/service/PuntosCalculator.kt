package com.quiniela.backend.service

import org.springframework.stereotype.Component

@Component
class PuntosCalculator {

    fun calcular(
        predichoLocal: Int,
        predichoVisitante: Int,
        realLocal: Int,
        realVisitante: Int
    ): Int {
        return when {
            predichoLocal == realLocal && predichoVisitante == realVisitante -> 10
            (predichoLocal - predichoVisitante) == (realLocal - realVisitante) -> 5
            (predichoLocal > predichoVisitante && realLocal > realVisitante) ||
            (predichoLocal < predichoVisitante && realLocal < realVisitante) -> 3
            else -> 0
        }
    }
}

