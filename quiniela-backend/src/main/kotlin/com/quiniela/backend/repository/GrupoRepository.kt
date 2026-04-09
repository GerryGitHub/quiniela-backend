package com.quiniela.backend.repository

import com.quiniela.backend.entity.Grupo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GrupoRepository : JpaRepository<Grupo, Long> {
    fun findByNombre(nombre: String): Grupo?
    fun findAllByOrderByNombre(): List<Grupo>
}