package com.quiniela.backend.service.impl

import com.quiniela.backend.dto.PartidoDTO
import com.quiniela.backend.service.WebSocketService
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketServiceImpl(
    private val messagingTemplate: SimpMessagingTemplate
) : WebSocketService {
    override fun notifyPartidoActualizado(partido: PartidoDTO) {
        messagingTemplate.convertAndSend("/topic/partidos", partido)
    }
}
