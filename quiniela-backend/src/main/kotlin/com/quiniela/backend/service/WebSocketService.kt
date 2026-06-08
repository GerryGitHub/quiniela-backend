package com.quiniela.backend.service

import com.quiniela.backend.dto.PartidoDTO

interface WebSocketService {
    fun notifyPartidoActualizado(partido: PartidoDTO)
}
