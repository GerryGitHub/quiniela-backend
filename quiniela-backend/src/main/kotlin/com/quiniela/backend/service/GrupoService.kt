package com.quiniela.backend.service

import com.quiniela.backend.dto.*

interface GrupoService {
    fun getAllGrupos(): TablaGruposDTO
    fun getGrupo(nombre: String): GrupoDTO
    fun actualizarResultadoPartido(partidoId: Long, request: ActualizarResultadoRequest): PartidoDTO
    fun getPartidosGrupos(): List<PartidoDTO>
}
