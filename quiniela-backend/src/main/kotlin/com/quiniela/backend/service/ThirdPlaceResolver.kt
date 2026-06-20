package com.quiniela.backend.service

import com.quiniela.backend.entity.ThirdPlaceCombination
import com.quiniela.backend.exception.NotFoundException
import com.quiniela.backend.repository.ThirdPlaceCombinationRepository
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct

@Service
class ThirdPlaceResolver(
    private val repository: ThirdPlaceCombinationRepository
) {
    private val optionByGroups = mutableMapOf<String, Int>()

    @PostConstruct
    fun buildLookup() {
        val all = repository.findAll()
        for (combo in all) {
            val groups = setOf(combo.p79, combo.p85, combo.p81, combo.p74, combo.p82, combo.p77, combo.p87, combo.p80)
            val key = groups.sorted().joinToString("")
            optionByGroups[key] = combo.option
        }
    }

    fun resolveOption(gruposClasificados: Set<String>): Int {
        val key = gruposClasificados.sorted().joinToString("")
        return optionByGroups[key]
            ?: throw IllegalArgumentException("No FIFA option found for groups: $key")
    }

    fun resolveThirdPlaces(option: Int): ThirdPlaceCombination {
        return repository.findById(option)
            .orElseThrow { NotFoundException("Third place combination not found for option: $option") }
    }
}
