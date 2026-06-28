package com.quiniela.backend.dto

data class AdminDashboardDTO(
    val usuarios: Long,
    val usuariosVerificados: Long,
    val quinielas: Long,
    val pronosticos: Long,
    val partidosLive: Long
)

data class AdminUserDTO(
    val id: Long,
    val nombre: String,
    val email: String
)

data class AdminQuinielaDTO(
    val id: Long,
    val nombre: String,
    val administrador: String,
    val estado: String = "ACTIVA"
)

data class AdminPartidoDTO(
    val id: Long,
    val local: String,
    val visitante: String,
    val estado: String
)

data class AdminActivityDTO(
    val usuarios: List<AdminUserDTO>,
    val quinielas: List<AdminQuinielaDTO>,
    val partidos: List<AdminPartidoDTO>
)

data class AdminUserListDTO(
    val id: Long,
    val nombre: String,
    val email: String,
    val verificado: Boolean,
    val fechaRegistro: String?,
    val quinielas: Long
)

data class AdminQuinielaListDTO(
    val id: Long,
    val nombre: String,
    val creador: String,
    val participantes: Long,
    val createdAt: String?,
    val estado: String = "ACTIVA"
)

data class AdminUserDetailDTO(
    val id: Long,
    val nombre: String,
    val email: String,
    val verificado: Boolean,
    val fechaRegistro: String?,
    val cantidadQuinielas: Long,
    val quinielas: List<AdminQuinielaDTO>
)

data class AdminSystemDTO(
    val api: String,
    val database: String,
    val ultimaActualizacion: String?
)

data class EquipoEstadisticasDTO(
    val equipoId: Long,
    val nombre: String,
    val grupo: String?,
    val rankingFifa: Int?,
    val puntosFairPlay: Int
)

data class UpdateEstadisticasRequest(
    val rankingFifa: Int?,
    val puntosFairPlay: Int?
)
