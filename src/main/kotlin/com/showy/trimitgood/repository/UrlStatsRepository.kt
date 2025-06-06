package com.showy.trimitgood.repository

import com.showy.trimitgood.model.UrlStats
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UrlStatsRepository : JpaRepository<UrlStats, Long> {
    fun countByUrlId(urlId: Long): Long
    fun findAllByUrlId(urlId: Long): List<UrlStats>
}
