package com.quiniela.backend.repository

import com.quiniela.backend.entity.ThirdPlaceCombination
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ThirdPlaceCombinationRepository : JpaRepository<ThirdPlaceCombination, Int>
