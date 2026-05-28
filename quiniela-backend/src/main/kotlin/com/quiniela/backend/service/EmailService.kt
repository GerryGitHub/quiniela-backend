package com.quiniela.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class EmailService(
    @Value("\${resend.api-key:}") private val resendApiKey: String,
    @Value("\${email.from:onboarding@resend.dev}") private val emailFrom: String
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper()

    fun sendPasswordResetEmail(email: String, nombre: String, token: String) {
        if (resendApiKey.isBlank()) {
            throw IllegalStateException("Servicio de correo no configurado")
        }

        val htmlBody = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="font-family: sans-serif; padding: 24px; text-align: center;">
                <h2>Recupera tu contrase&ntilde;a de QGol</h2>
                <p>Hola, $nombre.</p>
                <p>Tu c&oacute;digo de recuperaci&oacute;n es:</p>
                <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #0D5BFF; padding: 20px; background: #F0F4FF; border-radius: 12px; margin: 20px 0;">$token</div>
                <p style="color: #666;">Este c&oacute;digo expira en 15 minutos.</p>
                <p style="color: #999; font-size: 12px;">Si no solicitaste esto, ignora este mensaje.</p>
            </body>
            </html>
        """.trimIndent()

        val payload = mapOf(
            "from" to emailFrom,
            "to" to listOf(email),
            "subject" to "Tu código de recuperación - QGol",
            "html" to htmlBody
        )

        try {
            val jsonBody = objectMapper.writeValueAsString(payload)
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer $resendApiKey")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() in 200..299) {
                logger.info("Password reset email sent to $email")
            } else {
                logger.error("Resend API error for $email: ${response.statusCode()} ${response.body()}")
                throw RuntimeException("Error al enviar correo de recuperación")
            }
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error sending password reset email to $email", e)
            throw RuntimeException("Error al enviar correo de recuperación")
        }
    }

    fun sendVerificationEmail(email: String, nombre: String, token: String) {
        if (resendApiKey.isBlank()) {
            throw IllegalStateException("Servicio de correo no configurado")
        }

        val htmlBody = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="font-family: sans-serif; padding: 24px;">
                <h2>¡Bienvenido, $nombre!</h2>
                <p>Gracias por registrarte en QGol.</p>
                <p>Tu c&oacute;digo de verificaci&oacute;n es:</p>
                <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #0D5BFF; padding: 20px; background: #F0F4FF; border-radius: 12px; margin: 20px 0;">$token</div>
                <p style="color: #666;">Este c&oacute;digo expira en 15 minutos.</p>
                <p style="color: #999; font-size: 12px;">Si no solicitaste esto, ignora este mensaje.</p>
            </body>
            </html>
        """.trimIndent()

        val payload = mapOf(
            "from" to emailFrom,
            "to" to listOf(email),
            "subject" to "Tu código de verificación - QGol",
            "html" to htmlBody
        )

        try {
            val jsonBody = objectMapper.writeValueAsString(payload)
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer $resendApiKey")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() in 200..299) {
                logger.info("Verification email sent to $email")
            } else {
                logger.error("Resend API error for $email: ${response.statusCode()} ${response.body()}")
                throw RuntimeException("Error al enviar correo de verificación")
            }
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error sending verification email to $email", e)
            throw RuntimeException("Error al enviar correo de verificación")
        }
    }
}
