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
    @Value("\${app.base-url:http://localhost:8080}") private val appBaseUrl: String,
    @Value("\${email.from:onboarding@resend.dev}") private val emailFrom: String
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper()

    fun sendVerificationEmail(email: String, nombre: String, token: String) {
        if (resendApiKey.isBlank()) {
            throw IllegalStateException("Servicio de correo no configurado")
        }

        val link = "$appBaseUrl/auth/verify-email?token=$token"
        val htmlBody = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="font-family: sans-serif; padding: 24px;">
                <h2>¡Bienvenido, $nombre!</h2>
                <p>Gracias por registrarte en Quiniela.</p>
                <p>Para activar tu cuenta, haz clic en el siguiente enlace:</p>
                <p><a href="$link" style="display: inline-block; padding: 12px 24px; background: #0D5BFF; color: white; text-decoration: none; border-radius: 8px;">Verificar mi correo</a></p>
                <p>O copia este enlace en tu navegador:</p>
                <p style="color: #0D5BFF;">$link</p>
                <p style="color: #666;">Este enlace expira en 24 horas.</p>
            </body>
            </html>
        """.trimIndent()

        val payload = mapOf(
            "from" to emailFrom,
            "to" to listOf(email),
            "subject" to "Verifica tu correo electrónico - Quiniela",
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
