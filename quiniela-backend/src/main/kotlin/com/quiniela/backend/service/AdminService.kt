package com.quiniela.backend.service

import com.quiniela.backend.dto.AdminActivityDTO
import com.quiniela.backend.dto.AdminDashboardDTO
import com.quiniela.backend.dto.AdminPartidoDTO
import com.quiniela.backend.dto.AdminQuinielaDTO
import com.quiniela.backend.dto.AdminUserDTO
import com.quiniela.backend.entity.EstadoPartido
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
    private val partidoRepository: PartidoRepository
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
}
