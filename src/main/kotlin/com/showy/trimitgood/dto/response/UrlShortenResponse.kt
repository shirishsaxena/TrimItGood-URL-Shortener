package com.showy.trimitgood.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UrlShortenResponse(
    var id: Long,
    var shortCode: String,
    var originalUrl: String,
    var createdAt: Instant,
    var updatedAt: Instant? = null,
    var expiredAt: Instant? = null,
    var accessLimit: Int? = null
)
