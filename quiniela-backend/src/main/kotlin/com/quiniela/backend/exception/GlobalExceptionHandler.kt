package com.quiniela.backend.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        println("ERROR IllegalArgumentException: ${e.message}")
        return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "Bad request")))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<Map<String, String>> {
        println("ERROR ForbiddenException: ${e.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to (e.message ?: "Forbidden")))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<Map<String, String>> {
        println("ERROR NotFoundException: ${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (e.message ?: "Not found")))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<Map<String, String>> {
        println("ERROR GenericException: ${e.message}")
        e.printStackTrace()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to (e.message ?: "Error interno del servidor")))
    }
}

class ForbiddenException(message: String) : RuntimeException(message)
class NotFoundException(message: String) : RuntimeException(message)
