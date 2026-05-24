package com.quiniela.backend.entity

import jakarta.persistence.*

@Entity
class Grupo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val nombre: String
)

@Entity
class Equipo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val nombre: String,
    val banderaUrl: String? = null,
    
    @ManyToOne
    @JoinColumn(name = "grupo_id")
    val grupo: Grupo? = null
)