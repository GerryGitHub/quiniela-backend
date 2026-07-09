package com.quiniela.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath
        // No skip - always process to authenticate
        return false
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                val jwt = authHeader.substring(7)
                val userEmail = jwtService.extractUsername(jwt)

                if (SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(userEmail)

                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        val authToken = UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.authorities
                        )
                        authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authToken
                    }
                }
            } catch (e: Exception) {
                // Log error but continue - let controller handle auth
                logger.debug("JWT validation failed: $e")
            }
        }

        filterChain.doFilter(request, response)
    }
}
