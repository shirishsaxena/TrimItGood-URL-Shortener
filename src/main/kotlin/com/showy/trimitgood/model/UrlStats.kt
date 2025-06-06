package com.showy.trimitgood.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "url_stats", schema = "sho")
data class UrlStats(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "url_id", nullable = false)
    val urlId: Long,

    @Column(name = "ip_access_from", nullable = true)
    val ipAccessFrom: String? = null,

    @Column(name = "user_agent", nullable = true)
    val userAgent: String? = null,

    @Column(name = "accessed_at", nullable = false)
    val accessedAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id", nullable = false, insertable = false, updatable = false)
    val url: Url? = null
)
