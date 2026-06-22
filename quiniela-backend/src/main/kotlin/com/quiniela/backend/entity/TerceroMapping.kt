package com.quiniela.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "terceros_mapping")
class TerceroMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "combinacion", nullable = false, length = 12)
    val combinacion: String = "",
    @Column(name = "slot_codigo", nullable = false, length = 10)
    val slotCodigo: String = "",
    @Column(name = "grupo_origen", nullable = false, length = 1)
    val grupoOrigen: String = ""
)
