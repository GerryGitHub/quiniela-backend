package com.quiniela.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {

    @Bean
    fun corsConfigurationSource(
        @Value("\${cors.allowed-origins:}") allowedOrigins: String,
        @Value("\${app.base-url:}") appBaseUrl: String
    ): UrlBasedCorsConfigurationSource {
        val origins = (allowedOrigins.split(",").map { it.trim() } + appBaseUrl)
            .filter { it.isNotBlank() }

        if (origins.isEmpty()) {
            throw IllegalStateException(
                "No CORS allowed origins configured. Set cors.allowed-origins (comma-separated) or app.base-url."
            )
        }

        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = origins
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
