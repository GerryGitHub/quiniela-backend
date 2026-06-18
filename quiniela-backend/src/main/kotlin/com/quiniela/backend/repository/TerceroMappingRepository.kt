package com.quiniela.backend.repository

import com.quiniela.backend.entity.TerceroMapping
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TerceroMappingRepository : JpaRepository<TerceroMapping, Long> {
    fun findByCombinacion(combinacion: String): List<TerceroMapping>
}
