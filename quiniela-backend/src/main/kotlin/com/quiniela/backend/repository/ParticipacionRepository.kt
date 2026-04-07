package com.quiniela.backend.repository

import com.quiniela.backend.entity.Participacion
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ParticipacionRepository : JpaRepository<Participacion, Long> {
    fun findByUsuarioIdAndQuinielaId(usuarioId: Long, quinielaId: Long): Optional<Participacion>

    fun existsByUsuarioIdAndQuinielaId(usuarioId: Long, quinielaId: Long): Boolean

    @Query("SELECT p FROM Participacion p WHERE p.quiniela.id = :quinielaId ORDER BY p.puntosTotales DESC")
    fun findByQuinielaIdOrderByPuntosDesc(quinielaId: Long): List<Participacion>

    @Query("SELECT p FROM Participacion p JOIN p.usuario u WHERE u.email = :email AND p.quiniela.id = :quinielaId")
    fun findByEmailAndQuinielaId(email: String, quinielaId: Long): Optional<Participacion>
}
