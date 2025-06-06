package com.showy.trimitgood.repository

import com.showy.trimitgood.model.UrlSequence
import org.springframework.data.jpa.repository.JpaRepository

interface UrlSequenceRepository : JpaRepository<UrlSequence, Long>
