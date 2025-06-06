package com.showy.trimitgood.model

import com.showy.trimitgood.dto.response.UrlShortenResponse
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "url", schema = "sho")
data class Url(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "short_code", nullable = false, unique = true)
    val shortCode: String,

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    val originalUrl: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "expired_at")
    val expiredAt: Instant? = null,

    @Column(name = "access_limit")
    val accessLimit: Int? = null,

    @OneToMany(mappedBy = "url", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val stats: List<UrlStats> = emptyList()
) {
    fun toResponseDto(): UrlShortenResponse {
        return UrlShortenResponse(
            id = this.id,
            shortCode = this.shortCode,
            originalUrl = this.originalUrl,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            expiredAt = this.expiredAt,
            accessLimit = this.accessLimit
        )
    }
}
