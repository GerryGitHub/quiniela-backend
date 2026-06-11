package com.quiniela.backend.service

import com.quiniela.backend.dto.*

interface PartidoService {
    fun getPartidos(fecha: String?, fase: String?): List<PartidoDTO>
    fun getPartidoDetalle(id: Long): PartidoDTO
    fun actualizarPartido(id: Long, request: ActualizarPartidoRequest): PartidoDTO
}
