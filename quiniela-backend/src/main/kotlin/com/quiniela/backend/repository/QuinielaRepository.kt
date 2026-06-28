package com.quiniela.backend.repository

import com.quiniela.backend.entity.Quiniela
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface QuinielaRepository : JpaRepository<Quiniela, Long> {
    fun findByCodigoInvitacion(codigo: String): Optional<Quiniela>

    @Query("SELECT q FROM Quiniela q JOIN q.administrador a WHERE a.id = :usuarioId")
    fun findByAdministradorId(usuarioId: Long): List<Quiniela>

    @Query("SELECT q FROM Quiniela q JOIN Participacion p ON p.quiniela = q WHERE p.usuario.id = :usuarioId")
    fun findByParticipanteId(usuarioId: Long): List<Quiniela>

    fun existsByNombre(nombre: String): Boolean
    fun findTop10ByOrderByIdDesc(): List<Quiniela>
    fun findByEstado(estado: String): List<Quiniela>
}
