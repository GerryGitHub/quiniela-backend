package com.quiniela.backend.dto

data class AdminDashboardDTO(
    val usuarios: Long,
    val usuariosVerificados: Long,
    val quinielas: Long,
    val pronosticos: Long,
    val partidosLive: Long
)
