package com.quiniela.backend.repository

import com.quiniela.backend.entity.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmail(email: String): Optional<Usuario>
    fun existsByEmail(email: String): Boolean

    @Query("SELECT COALESCE(SUM(p.puntosTotales), 0) FROM Participacion p WHERE p.usuario.id = :usuarioId")
    fun puntosTotalesGlobales(usuarioId: Long): Int
}
