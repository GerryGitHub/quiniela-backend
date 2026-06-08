package com.quiniela.backend.service

import com.quiniela.backend.dto.*

interface ResultadosService {
    fun getPartidos(): List<PartidoDTO>
    fun getPartidosPendientes(): List<PartidoDTO>
    fun getPartidosConResultados(): List<PartidoDTO>
    fun getPartidosEnVivo(): List<PartidoDTO>
    fun actualizarResultado(partidoId: Long, request: ActualizarResultadoRequest, esAdmin: Boolean = false): PartidoDTO
    fun finalizarPartido(partidoId: Long): PartidoDTO
}
