package com.quiniela.backend.repository

import com.quiniela.backend.entity.Equipo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EquipoRepository : JpaRepository<Equipo, Long> {
    fun findByGrupoId(grupoId: Long): List<Equipo>
}