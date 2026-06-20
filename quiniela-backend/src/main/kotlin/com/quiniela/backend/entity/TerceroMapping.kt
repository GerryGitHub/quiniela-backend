package com.quiniela.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "terceros_mapping")
class TerceroMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
