package com.quiniela.app.model

data class Partido(
    val id: Long = 0,
    val equipoLocal: String = "",
    val equipoVisitante: String = "",
    val fechaHora: String = "",
    val grupo: String? = null,
    val grupoId: Long? = null,
    val equipoLocalId: Long? = null,
    val equipoVisitanteId: Long? = null,
    val golesLocalReal: Int? = null,
    val golesVisitanteReal: Int? = null,
    val estado: String = "PENDIENTE"
)