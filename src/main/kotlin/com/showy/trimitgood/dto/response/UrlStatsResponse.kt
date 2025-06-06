package com.showy.trimitgood.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UrlStatsResponse(
    var shortCode: String,
    var originalUrl: String,
    var createdAt: Instant,
    var updatedAt: Instant?,
    var expiredAt: Instant?,
    var accessLimit: Int?,

    var totalVisits: Int?,
    var remainingVisits: Int?,
    var details: List<VisitStats>? = emptyList()
) {
    data class VisitStats(
        var accessedAt: Instant,
        var ipAccessFrom: String?,
        var userAgent: String?
    )
}
