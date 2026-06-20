package com.quiniela.backend.repository

import com.quiniela.backend.entity.Partido
import com.quiniela.backend.entity.EstadoPartido
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PartidoRepository : JpaRepository<Partido, Long> {
    @Query("SELECT p FROM Partido p WHERE p.fechaHora >= :fechaInicio AND p.fechaHora <= :fechaFin")
    fun findByFechaBetween(fechaInicio: LocalDateTime, fechaFin: LocalDateTime): List<Partido>

    @Query("SELECT p FROM Partido p WHERE p.estado = :estado")
    fun findByEstado(estado: EstadoPartido): List<Partido>

    fun countByEstado(estado: EstadoPartido): Long

    fun findAllByOrderByFechaHoraAsc(): List<Partido>
    fun findTop10ByOrderByIdDesc(): List<Partido>

    @Query("SELECT MAX(p.fechaHora) FROM Partido p WHERE p.estado = :estado1 OR p.estado = :estado2")
    fun findUltimaActualizacion(
        @Param("estado1") estado1: EstadoPartido = EstadoPartido.FINALIZADO,
        @Param("estado2") estado2: EstadoPartido = EstadoPartido.EN_CURSO
    ): LocalDateTime?
}
