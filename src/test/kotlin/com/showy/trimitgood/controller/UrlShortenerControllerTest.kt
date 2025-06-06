package com.showy.trimitgood.controller

import com.showy.trimitgood.dto.request.UrlShortenRequest
import com.showy.trimitgood.dto.response.UrlShortenResponse
import com.showy.trimitgood.dto.response.UrlStatsResponse
import com.showy.trimitgood.service.URLShortenerService
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.net.URI
import java.time.Instant
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UrlShortenerControllerTest {

    private val urlShortenerService = mock<URLShortenerService>()
    private val httpServletRequest = mock<HttpServletRequest>()

    @InjectMocks
    lateinit var controller: URLShortenerController

    @Test
    fun `should redirect to original URL`() {
        val shortenCode = "abc123"
        val expectedUrl = "http://hollow.world"

        whenever(urlShortenerService.redirectToOriginalUrl(shortenCode, httpServletRequest))
            .thenReturn(expectedUrl)

        val response = controller.redirectToOriginalUrl(shortenCode, httpServletRequest)

        assertEquals(HttpStatus.FOUND, response.statusCode)
        assertEquals(URI.create(expectedUrl), response.headers.location)
    }

    @Test
    fun `should return URL details`() {
        val shortenCode = "abc123"
        val expectedResponse = UrlShortenResponse(
            id = 1,
            shortCode = "abc123",
            originalUrl = "http://hollow.world",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        whenever(urlShortenerService.getUrlForShortCode(shortenCode))
            .thenReturn(expectedResponse)

        val response = controller.getUrlForShortCode(shortenCode)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)
    }

    @Test
    fun `should shorten URL`() {
        val request = UrlShortenRequest(
            "http://original.com",
            customShortCode = null
        )
        val expectedResponse = UrlShortenResponse(
            id = 1,
            shortCode = "random2",
            originalUrl = "http://hollow.world",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        whenever(urlShortenerService.shortenUrl(request))
            .thenReturn(expectedResponse)

        val response = controller.shortenUrl(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)
    }

    @Test
    fun `should update URL by shortenCode`() {
        val shortenCode = "abc123"
        val request = UrlShortenRequest(
            "http://hello.world",
            customShortCode = null
        )

        val expectedResponse = UrlShortenResponse(
            id = 1,
            shortCode = "abc123",
            originalUrl = "http://hollow.world",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        whenever(urlShortenerService.updateByShortenCode(shortenCode, request))
            .thenReturn(expectedResponse)

        val response = controller.updateByShortenCode(shortenCode, request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)
    }

    @Test
    fun `should delete URL by shortenCode`() {
        val shortenCode = "abc123"

        doNothing().whenever(urlShortenerService).deleteByShortCode(shortenCode)

        val response = controller.deleteByShortCode(shortenCode)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
    }

    @Test
    fun `should return URL stats`() {
        val shortenCode = "abc123"
        val expectedStats = UrlStatsResponse(
            originalUrl = "http://hollow.world",
            shortCode = "abc123",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = null,
            accessLimit = null,
            totalVisits = 1,
            remainingVisits = null,
            details = listOf(
                UrlStatsResponse.VisitStats(
                    accessedAt = Instant.now(),
                    ipAccessFrom = "127.0.0.1",
                    userAgent = " ( ͡° ͜ʖ ͡°) "
                )
            )
        )

        whenever(urlShortenerService.getUrlStats(shortenCode))
            .thenReturn(expectedStats)

        val response = controller.getUrlStats(shortenCode)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedStats, response.body)
    }
}
