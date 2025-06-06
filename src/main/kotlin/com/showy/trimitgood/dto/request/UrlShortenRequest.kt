package com.showy.trimitgood.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import java.time.Instant

data class UrlShortenRequest(
    @field:NotBlank(message = "URL is required to shortened it")
    @field:URL(message = "Provided URL is not valid")
    val url: String,

    @field:Size(min = 8, max = 20, message = "Custom short code must be (8, 20) characters")
    val customShortCode: String?,

    @field:Min(value = 1, message = "Access limit must be at least 1")
    @field:Max(value = 10000, message = "DDo you really need accessLimit for this THICC value?")
    val accessLimit: Int? = null,

    val expiry: Instant? = null
)
