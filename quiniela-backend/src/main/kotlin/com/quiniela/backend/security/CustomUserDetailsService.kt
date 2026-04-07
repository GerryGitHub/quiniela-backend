package com.quiniela.backend.security

import com.quiniela.backend.repository.UsuarioRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val usuarioRepository: UsuarioRepository
) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("Usuario no encontrado: $email") }

        return User.builder()
            .username(usuario.email)
            .password(usuario.password)
            .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
            .build()
    }
}
