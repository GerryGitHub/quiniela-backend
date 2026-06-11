package com.quiniela.backend.service.impl

import com.quiniela.backend.dto.*
import com.quiniela.backend.repository.ParticipacionRepository
import com.quiniela.backend.repository.QuinielaRepository
import com.quiniela.backend.repository.UsuarioRepository
import com.quiniela.backend.service.PerfilService
import org.springframework.stereotype.Service

@Service
class PerfilServiceImpl(
    private val usuarioRepository: UsuarioRepository,
    private val participacionRepository: ParticipacionRepository,
    private val quinielaRepository: QuinielaRepository
) : PerfilService {

    override fun getPerfil(email: String): UsuarioPerfilDTO {
        val usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        val puntosGlobales = usuarioRepository.puntosTotalesGlobales(usuario.id)

        val quinielas = mutableListOf<QuinielaResumenDTO>()

        val misQuinielasAdmin = quinielaRepository.findByAdministradorId(usuario.id)
        misQuinielasAdmin.forEach { q ->
            val participacion = participacionRepository.findByUsuario_IdAndQuiniela_Id(usuario.id, q.id)
            quinielas.add(
                QuinielaResumenDTO(
                    id = q.id,
                    nombre = q.nombre,
                    codigoInvitacion = q.codigoInvitacion,
                    puntosTotales = participacion.map { it.puntosTotales }.orElse(0)
                )
            )
        }

        val misQuinielas = quinielaRepository.findByParticipanteId(usuario.id)
        misQuinielas.forEach { q ->
            if (quinielas.none { it.id == q.id }) {
                val participacion = participacionRepository.findByUsuario_IdAndQuiniela_Id(usuario.id, q.id)
                quinielas.add(
                    QuinielaResumenDTO(
                        id = q.id,
                        nombre = q.nombre,
                        codigoInvitacion = q.codigoInvitacion,
                        puntosTotales = participacion.map { it.puntosTotales }.orElse(0)
                    )
                )
            }
        }

        return UsuarioPerfilDTO(
            id = usuario.id,
            nombre = usuario.nombre,
            email = usuario.email,
            rol = usuario.rol,
            puntosTotalesGlobales = puntosGlobales,
            quinielas = quinielas
        )
    }
}
