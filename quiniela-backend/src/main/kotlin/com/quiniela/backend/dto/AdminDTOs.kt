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
    val administrador: String
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
    val createdAt: String?
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
