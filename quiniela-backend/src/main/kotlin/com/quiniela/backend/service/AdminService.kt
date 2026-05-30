package com.quiniela.backend.service

import com.quiniela.backend.dto.AdminActivityDTO
import com.quiniela.backend.dto.AdminDashboardDTO
import com.quiniela.backend.dto.AdminPartidoDTO
import com.quiniela.backend.dto.AdminQuinielaDTO
import com.quiniela.backend.dto.AdminUserDTO
import com.quiniela.backend.dto.AdminQuinielaListDTO
import com.quiniela.backend.dto.AdminSystemDTO
import com.quiniela.backend.dto.AdminUserDetailDTO
import com.quiniela.backend.dto.AdminUserListDTO
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.entity.Quiniela
import com.quiniela.backend.repository.ParticipacionRepository
import com.quiniela.backend.repository.PartidoRepository
import com.quiniela.backend.repository.PronosticoRepository
import com.quiniela.backend.repository.QuinielaRepository
import com.quiniela.backend.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class AdminService(
    private val usuarioRepository: UsuarioRepository,
    private val quinielaRepository: QuinielaRepository,
    private val pronosticoRepository: PronosticoRepository,
    private val partidoRepository: PartidoRepository,
    private val participacionRepository: ParticipacionRepository
) {

    fun getDashboard(): AdminDashboardDTO {
        return AdminDashboardDTO(
            usuarios = usuarioRepository.count(),
            usuariosVerificados = usuarioRepository.countByEmailVerifiedTrue(),
            quinielas = quinielaRepository.count(),
            pronosticos = pronosticoRepository.count(),
            partidosLive = partidoRepository.countByEstado(EstadoPartido.EN_CURSO)
        )
    }

    fun getSystemStatus(): AdminSystemDTO {
        val dbOnline = try {
            usuarioRepository.count()
            true
        } catch (e: Exception) {
            false
        }
        val ultimaActualizacion = try {
            partidoRepository.findUltimaActualizacion()?.toString()?.replace("T", " ")
        } catch (e: Exception) {
            null
        }
        return AdminSystemDTO(
            api = "ONLINE",
            database = if (dbOnline) "ONLINE" else "OFFLINE",
            ultimaActualizacion = ultimaActualizacion
        )
    }

    fun getActivity(): AdminActivityDTO {
        val usuarios = usuarioRepository.findTop10ByOrderByIdDesc().map {
            AdminUserDTO(id = it.id, nombre = it.nombre, email = it.email)
        }
        val quinielas = quinielaRepository.findTop10ByOrderByIdDesc().map {
            AdminQuinielaDTO(id = it.id, nombre = it.nombre, administrador = it.administrador.nombre)
        }
        val partidos = partidoRepository.findTop10ByOrderByIdDesc().map {
            AdminPartidoDTO(
                id = it.id,
                local = it.equipoLocal.nombre,
                visitante = it.equipoVisitante.nombre,
                estado = it.estado.name
            )
        }
        return AdminActivityDTO(usuarios = usuarios, quinielas = quinielas, partidos = partidos)
    }

    fun getQuinielas(
        search: String? = null,
        sort: String? = null,
        order: String? = null
    ): List<AdminQuinielaListDTO> {
        var quinielas = quinielaRepository.findAll()

        if (!search.isNullOrBlank()) {
            val q = search.lowercase()
            quinielas = quinielas.filter {
                it.nombre.lowercase().contains(q) || it.administrador.nombre.lowercase().contains(q)
            }
        }

        val comparator: Comparator<Quiniela> = when (sort?.lowercase()) {
            "nombre" -> compareBy { it.nombre.lowercase() }
            "creador" -> compareBy { it.administrador.nombre.lowercase() }
            "fecha" -> compareBy { it.createdAt }
            else -> compareByDescending { it.id }
        }

        quinielas = if (order?.lowercase() == "asc") quinielas.sortedWith(comparator)
        else quinielas.sortedWith(comparator.reversed())

        return quinielas.map {
            AdminQuinielaListDTO(
                id = it.id,
                nombre = it.nombre,
                creador = it.administrador.nombre,
                participantes = participacionRepository.countByQuinielaId(it.id),
                createdAt = it.createdAt?.toString()?.replace("T", " ")
            )
        }
    }

    fun getUserDetail(id: Long): AdminUserDetailDTO? {
        val user = usuarioRepository.findById(id).orElse(null) ?: return null
        val quinielas = quinielaRepository.findByParticipanteId(id).map {
            AdminQuinielaDTO(id = it.id, nombre = it.nombre, administrador = it.administrador.nombre)
        }
        return AdminUserDetailDTO(
            id = user.id,
            nombre = user.nombre,
            email = user.email,
            verificado = user.emailVerified,
            fechaRegistro = user.fechaRegistro?.toString()?.replace("T", " "),
            cantidadQuinielas = participacionRepository.countByUsuarioId(user.id),
            quinielas = quinielas
        )
    }

    fun getUsers(search: String? = null, verificado: Boolean? = null): List<AdminUserListDTO> {
        var usuarios = usuarioRepository.findAll()

        if (!search.isNullOrBlank()) {
            val q = search.lowercase()
            usuarios = usuarios.filter {
                it.nombre.lowercase().contains(q) || it.email.lowercase().contains(q)
            }
        }

        if (verificado != null) {
            usuarios = usuarios.filter { it.emailVerified == verificado }
        }

        return usuarios.sortedByDescending { it.id }.map {
            AdminUserListDTO(
                id = it.id,
                nombre = it.nombre,
                email = it.email,
                verificado = it.emailVerified,
                fechaRegistro = it.fechaRegistro?.toString()?.replace("T", " "),
                quinielas = participacionRepository.countByUsuarioId(it.id)
            )
        }
    }
}
