package com.quiniela.backend.service

import com.quiniela.backend.dto.AdminDashboardDTO
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
}
