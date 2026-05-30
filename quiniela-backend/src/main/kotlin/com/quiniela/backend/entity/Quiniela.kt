package com.quiniela.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Quiniela(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val nombre: String,
    val codigoInvitacion: String,
    @ManyToOne
    val administrador: Usuario,
    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null
)
