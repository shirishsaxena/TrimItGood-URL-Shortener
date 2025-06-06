package com.showy.trimitgood.integrationtests.helper

import com.showy.trimitgood.dto.request.UrlShortenRequest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class UrlShortenerIntegrationTestProvider {
    companion object {
        @JvmStatic
        fun provideShortenUrlRequestsSuccess(): Stream<UrlShortenRequest> = Stream.of(
            UrlShortenRequest(url = "https://example.com", customShortCode = null, expiry = null, accessLimit = null),
            UrlShortenRequest(
                url = "https://example.com",
                customShortCode = "balidCode",
                expiry = null,
                accessLimit = null
            ),
            UrlShortenRequest(
                url = "https://example.com",
                customShortCode = null,
                expiry = Instant.now().plus(1, ChronoUnit.DAYS),
                accessLimit = null
            ),
            UrlShortenRequest(url = "https://example.com", customShortCode = null, expiry = null, accessLimit = 10),
            UrlShortenRequest(
                url = "https://example.com",
                customShortCode = "balidCode2",
                expiry = Instant.now().plus(1, ChronoUnit.DAYS),
                accessLimit = 5
            )
        )
    }
}
