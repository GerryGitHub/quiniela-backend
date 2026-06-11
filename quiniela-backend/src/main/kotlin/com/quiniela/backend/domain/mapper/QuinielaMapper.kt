package com.quiniela.backend.domain.mapper

import com.quiniela.backend.domain.CrearQuinielaCommand
import com.quiniela.backend.domain.UnirseQuinielaCommand
import com.quiniela.backend.dto.CrearQuinielaRequest
import com.quiniela.backend.dto.UnirseQuinielaRequest

fun CrearQuinielaRequest.toCommand() = CrearQuinielaCommand(
    nombre = nombre,
    codigoInvitacion = codigoInvitacion
)

fun UnirseQuinielaRequest.toCommand() = UnirseQuinielaCommand(
    codigoInvitacion = codigoInvitacion
)
