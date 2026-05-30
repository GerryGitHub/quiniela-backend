package com.quiniela.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val nombre: String,
    @Column(unique = true)
    val email: String,
    var password: String = "",
    var rol: String = "USER",  // USER o ADMIN
    @Column(name = "email_verified")
    var emailVerified: Boolean = false,
    @Column(name = "fecha_registro")
    var fechaRegistro: LocalDateTime? = null
)
