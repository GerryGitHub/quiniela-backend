package com.quiniela.backend.service

import com.quiniela.backend.dto.PartidoDTO
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    fun notifyPartidoActualizado(partido: PartidoDTO) {
        messagingTemplate.convertAndSend("/topic/partidos", partido)
    }
}