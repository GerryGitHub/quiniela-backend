package com.quiniela.backend.service.impl

import com.quiniela.backend.dto.*
import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.entity.HealthStatus
import com.quiniela.backend.entity.Quiniela
import com.quiniela.backend.entity.SortDirection
import com.quiniela.backend.entity.SortField
import com.quiniela.backend.repository.ParticipacionRepository
import com.quiniela.backend.repository.PartidoRepository
import com.quiniela.backend.repository.PronosticoRepository
import com.quiniela.backend.repository.QuinielaRepository
import com.quiniela.backend.repository.UsuarioRepository
import com.quiniela.backend.service.AdminService
import org.springframework.stereotype.Service

@Service
class AdminServiceImpl(
    private val usuarioRepository: UsuarioRepository,
    private val quinielaRepository: QuinielaRepository,
    private val pronosticoRepository: PronosticoRepository,
    private val partidoRepository: PartidoRepository,
    private val participacionRepository: ParticipacionRepository
) : AdminService {

    override fun getDashboard(): AdminDashboardDTO {
        return AdminDashboardDTO(
            usuarios = usuarioRepository.count(),
            usuariosVerificados = usuarioRepository.countByEmailVerifiedTrue(),
            quinielas = quinielaRepository.count(),
            pronosticos = pronosticoRepository.count(),
            partidosLive = partidoRepository.countByEstado(EstadoPartido.EN_CURSO)
        )
    }

    override fun getSystemStatus(): AdminSystemDTO {
        val dbOnline = try {
            usuarioRepository.count()
            true
        } catch (e: Exception) {
            false
        }
        val ultimaActualizacion = try {
            partidoRepository.findUltimaActualizacion()?.toString()?.replace("T", " ")
        } catch (_: Exception) {
            null
        }
        return AdminSystemDTO(
            api = HealthStatus.ONLINE.status,
            database = if (dbOnline) HealthStatus.ONLINE.status else HealthStatus.OFFLINE.status,
            ultimaActualizacion = ultimaActualizacion
        )
    }

    override fun getActivity(): AdminActivityDTO {
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

    override fun getQuinielas(
        search: String?,
        sort: String?,
        order: String?
    ): List<AdminQuinielaListDTO> {
        var quinielas = quinielaRepository.findAll()

        if (!search.isNullOrBlank()) {
            val q = search.lowercase()
            quinielas = quinielas.filter {
                it.nombre.lowercase().contains(q) || it.administrador.nombre.lowercase().contains(q)
            }
        }

        val comparator: Comparator<Quiniela> = when (SortField.from(sort?.lowercase())) {
            SortField.NOMBRE -> compareBy { it.nombre.lowercase() }
            SortField.CREADOR -> compareBy { it.administrador.nombre.lowercase() }
            SortField.FECHA -> compareBy { it.createdAt }
            null -> compareByDescending { it.id }
        }

        quinielas = if (SortDirection.from(order?.lowercase()) == SortDirection.ASC) quinielas.sortedWith(comparator)
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

    override fun getUserDetail(id: Long): AdminUserDetailDTO? {
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

    override fun getUsers(search: String?, verificado: Boolean?): List<AdminUserListDTO> {
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
