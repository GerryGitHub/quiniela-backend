package com.quiniela.backend.service

import com.quiniela.backend.dto.UsuarioPerfilDTO

interface PerfilService {
    fun getPerfil(email: String): UsuarioPerfilDTO
}
