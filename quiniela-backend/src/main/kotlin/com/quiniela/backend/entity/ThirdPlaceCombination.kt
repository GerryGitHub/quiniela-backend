package com.quiniela.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "third_place_combination")
class ThirdPlaceCombination(
    @Id
    @Column(name = "option_id")
    val option: Int,
    @Column(nullable = false) val p79: String,
    @Column(nullable = false) val p85: String,
    @Column(nullable = false) val p81: String,
    @Column(nullable = false) val p74: String,
    @Column(nullable = false) val p82: String,
    @Column(nullable = false) val p77: String,
    @Column(nullable = false) val p87: String,
    @Column(nullable = false) val p80: String
)
