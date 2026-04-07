package com.quiniela.backend.repository

import com.quiniela.backend.entity.Partido
import com.quiniela.backend.entity.EstadoPartido
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PartidoRepository : JpaRepository<Partido, Long> {
    @Query("SELECT p FROM Partido p WHERE p.fechaHora >= :fechaInicio AND p.fechaHora <= :fechaFin")
    fun findByFechaBetween(fechaInicio: LocalDateTime, fechaFin: LocalDateTime): List<Partido>

    @Query("SELECT p FROM Partido p WHERE p.estado = :estado")
    fun findByEstado(estado: EstadoPartido): List<Partido>

    fun findAllByOrderByFechaHoraAsc(): List<Partido>
}
