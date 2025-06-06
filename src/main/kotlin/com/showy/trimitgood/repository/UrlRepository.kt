package com.showy.trimitgood.repository

import com.showy.trimitgood.model.Url
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UrlRepository : JpaRepository<Url, Long> {

    fun existsByShortCode(shortCode: String): Boolean
    fun findByShortCode(shortCode: String): Url?
}
