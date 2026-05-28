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

    @Scheduled(fixedRate = 30000)
    fun actualizarEstados() {
        try {
            val zonaMexico = ZoneId.of("America/Mexico_City")
            val ahora = ZonedDateTime.now(zonaMexico)
            val quinceMinutosAntes = ahora.plusMinutes(15)

            val todos = partidoRepository.findAll()

            // PENDIENTE -> POR_COMENZAR (15 min antes del inicio)
            todos.filter { it.estado == EstadoPartido.PENDIENTE }
                .filter { it.fechaHora.atZone(zonaMexico).isBefore(quinceMinutosAntes) || it.fechaHora.atZone(zonaMexico).isEqual(quinceMinutosAntes) }
                .forEach { partido ->
                    partido.estado = EstadoPartido.POR_COMENZAR
                    partidoRepository.save(partido)
                    logger.info("Partido {} cambiado a POR_COMENZAR", partido.id)
                }

            // POR_COMENZAR -> EN_CURSO (a la hora del inicio)
            todos.filter { it.estado == EstadoPartido.POR_COMENZAR }
                .filter { it.fechaHora.atZone(zonaMexico).isBefore(ahora) || it.fechaHora.atZone(zonaMexico).isEqual(ahora) }
                .forEach { partido ->
                    partido.estado = EstadoPartido.EN_CURSO
                    partidoRepository.save(partido)
                    logger.info("Partido {} cambiado a EN_CURSO", partido.id)
                }
        } catch (e: Exception) {
            logger.error("Error en PartidoScheduler: {}", e.message, e)
        }
    }
}