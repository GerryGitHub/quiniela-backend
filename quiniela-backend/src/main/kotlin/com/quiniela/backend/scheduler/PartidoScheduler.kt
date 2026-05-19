package com.quiniela.backend.scheduler

import com.quiniela.backend.entity.EstadoPartido
import com.quiniela.backend.repository.PartidoRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.ZoneId
import java.time.ZonedDateTime

@Component
class PartidoScheduler(
    private val partidoRepository: PartidoRepository
) {
    private val logger = LoggerFactory.getLogger(PartidoScheduler::class.java)

    @Scheduled(fixedRate = 60000)
    fun actualizarEstados() {
        try {
            val zonaMexico = ZoneId.of("America/Mexico_City")
            val ahora = ZonedDateTime.now(zonaMexico)
            val quinceMinutosAntes = ahora.plusMinutes(15)

            val partidosPendientes = partidoRepository.findAll()
                .filter { it.estado == EstadoPartido.PENDIENTE }

            val actualizados = partidosPendientes.filter { partido ->
                val fechaPartido = partido.fechaHora.atZone(zonaMexico)
                fechaPartido.isBefore(quinceMinutosAntes) || fechaPartido.isEqual(quinceMinutosAntes)
            }

            actualizados.forEach { partido ->
                partido.estado = EstadoPartido.EN_CURSO
                partidoRepository.save(partido)
            }

            if (actualizados.isNotEmpty()) {
                logger.info("Partidos cambiados a EN_CURSO: {}", actualizados.map { it.id })
            }
        } catch (e: Exception) {
            logger.error("Error en PartidoScheduler: {}", e.message, e)
        }
    }
}