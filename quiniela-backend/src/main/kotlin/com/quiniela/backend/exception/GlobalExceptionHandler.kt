package com.quiniela.backend.exception

import com.quiniela.backend.entity.Constantes
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        logger.warn("Bad request: {}", e.message)
        return ResponseEntity.badRequest().body(mapOf(Constantes.RESPONSE_KEY_ERROR to (e.message ?: "Bad request")))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val message = e.bindingResult.fieldErrors
            .firstOrNull()
            ?.defaultMessage
            ?: "Datos inválidos"
        logger.warn("Validation error: {}", message)
        return ResponseEntity.badRequest().body(mapOf(Constantes.RESPONSE_KEY_ERROR to message))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<Map<String, String>> {
        logger.warn("Forbidden: {}", e.message)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf(Constantes.RESPONSE_KEY_ERROR to (e.message ?: "Forbidden")))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<Map<String, String>> {
        logger.warn("Not found: {}", e.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf(Constantes.RESPONSE_KEY_ERROR to (e.message ?: "Not found")))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<Map<String, String>> {
        logger.error("Internal server error: {}", e.message, e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf(Constantes.RESPONSE_KEY_ERROR to "Error interno del servidor"))
    }
}

class ForbiddenException(message: String) : RuntimeException(message)
class NotFoundException(message: String) : RuntimeException(message)
