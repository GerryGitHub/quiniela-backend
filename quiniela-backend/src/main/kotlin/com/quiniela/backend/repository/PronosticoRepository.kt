package com.quiniela.backend.repository

import com.quiniela.backend.entity.Pronostico
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PronosticoRepository : JpaRepository<Pronostico, Long> {
    fun findByParticipacionIdAndPartidoId(participacionId: Long, partidoId: Long): Pronostico?

    @Query("SELECT p FROM Pronostico p WHERE p.participacion.quiniela.id = :quinielaId AND p.participacion.usuario.email = :email")
    fun findByQuinielaIdAndUsuarioEmail(quinielaId: Long, email: String): List<Pronostico>

    @Query("SELECT p FROM Pronostico p WHERE p.participacion.quiniela.id = :quinielaId AND p.participacion.usuario.id = :usuarioId")
    fun findByQuinielaIdAndUsuarioId(quinielaId: Long, usuarioId: Long): List<Pronostico>

    @Query("SELECT p FROM Pronostico p WHERE p.partido.id = :partidoId")
    fun findByPartidoId(partidoId: Long): List<Pronostico>

    @Query("SELECT p FROM Pronostico p WHERE p.participacion.id = :participacionId")
    fun findByParticipacionId(participacionId: Long): List<Pronostico>

    @Query("SELECT p FROM Pronostico p JOIN p.participacion pa JOIN pa.usuario u WHERE u.email = :email")
    fun findByUsuarioEmail(email: String): List<Pronostico>

    @Query("SELECT COUNT(p) FROM Pronostico p WHERE p.participacion.id = :participacionId AND p.puntosObtenidos > 0")
    fun countAciertosByParticipacionId(participacionId: Long): Long
}
