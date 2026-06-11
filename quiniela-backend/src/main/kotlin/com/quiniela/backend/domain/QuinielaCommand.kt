package com.quiniela.backend.domain

data class CrearQuinielaCommand(
    val nombre: String,
    val codigoInvitacion: String
)

data class UnirseQuinielaCommand(
    val codigoInvitacion: String
)
