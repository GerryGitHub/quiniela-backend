package com.quiniela.backend.repository

import com.quiniela.backend.entity.BracketSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BracketSlotRepository : JpaRepository<BracketSlot, Long> {
    fun findAllByOrderByRondaAscOrdenAsc(): List<BracketSlot>
}
