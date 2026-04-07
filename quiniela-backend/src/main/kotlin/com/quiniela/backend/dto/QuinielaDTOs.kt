package com.quiniela.backend.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CrearQuinielaRequest(
    val nombre: String,
    val codigoInvitacion: String
)

data class UnirseQuinielaRequest @JsonCreator constructor(
    @JsonProperty("codigoInvitacion") val codigoInvitacion: String
)

data class QuinielaDTO(
    val id: Long,
    val nombre: String,
    val codigoInvitacion: String,
    val administrador: UsuarioDTO,
    val participantes: List<UsuarioDTO>,
    val esPublica: Boolean = false
)

data class QuinielaResumenDTO(
    val id: Long,
    val nombre: String,
    val codigoInvitacion: String,
    val puntosTotales: Int = 0
)

data class LeaderboardEntryDTO(
    val posicion: Int,
    val usuario: UsuarioDTO,
    val puntosTotales: Int
)

data class QuinielaDetalleDTO(
    val id: Long,
    val nombre: String,
    val codigoInvitacion: String,
    val administrador: UsuarioDTO,
    val participantes: List<UsuarioDTO>,
    val partidos: List<PartidoDTO>
)
