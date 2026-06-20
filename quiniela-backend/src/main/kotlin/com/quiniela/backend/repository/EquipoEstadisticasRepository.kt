package com.quiniela.backend.repository

import com.quiniela.backend.entity.EquipoEstadisticas
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EquipoEstadisticasRepository : JpaRepository<EquipoEstadisticas, Long> {
    fun findByEquipoId(equipoId: Long): EquipoEstadisticas?
}
