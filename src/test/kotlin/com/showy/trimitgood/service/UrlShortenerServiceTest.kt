package com.showy.trimitgood.service

import com.showy.trimitgood.dto.request.UrlShortenRequest
import com.showy.trimitgood.exception.RequestException
import com.showy.trimitgood.model.Url
import com.showy.trimitgood.model.UrlStats
import com.showy.trimitgood.repository.UrlRepository
import com.showy.trimitgood.repository.UrlStatsRepository
import com.showy.trimitgood.util.UrlShortenerUtil
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UrlShortenerServiceTest {

    private val urlRepository = mock<UrlRepository>()
    private val sequenceService = mock<SequenceService>()
    private val urlShortenerUtil = mock<UrlShortenerUtil>()
    private val urlStatsRepository = mock<UrlStatsRepository>()

    @InjectMocks
    lateinit var service: URLShortenerService

    @Test
    fun `shortenUrl should save and return UrlShortenResponse`() {
        val request = UrlShortenRequest(
            "http://original.com",
            customShortCode = null,
            accessLimit = 420,
            expiry = Instant.now().plus(69, ChronoUnit.DAYS)
        )

        val generatedCode = "abc123"
        val savedEntity = Url(
            id = 1L,
            shortCode = generatedCode,
            originalUrl = request.url,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = request.expiry,
            accessLimit = request.accessLimit
        )

        whenever(sequenceService.getNext()).thenReturn(1L)
        whenever(urlShortenerUtil.generateUniqueShortCode(1L)).thenReturn(generatedCode)
        whenever(urlRepository.save(Mockito.any())).thenReturn(savedEntity)

        val response = service.shortenUrl(request)

        assertEquals(generatedCode, response.shortCode)
        assertEquals(request.url, response.originalUrl)
    }

    @Test
    fun `shortenUrl should throw when custom short code is blank`() {
        val request = UrlShortenRequest(
            url = "https://example.com",
            customShortCode = "   ",
            expiry = null,
            accessLimit = null
        )

        val ex = assertThrows<RequestException> {
            service.shortenUrl(request)
        }
        assertEquals("Custom short code cannot be blank", ex.message)
    }

    @Test
    fun `shortenUrl should throw when custom short code already exists`() {
        val request = UrlShortenRequest(
            url = "https://example.com",
            customShortCode = "existingCode",
            expiry = null,
            accessLimit = null
        )
        whenever(urlRepository.existsByShortCode("existingCode")).thenReturn(true)

        val ex = assertThrows<RequestException> {
            service.shortenUrl(request)
        }
        assertEquals("ShortCode already exists", ex.message)
    }

    @Test
    fun `shortenUrl should throw when expiry date is in the past`() {
        val pastInstant = Instant.now().minusSeconds(60)
        val request = UrlShortenRequest(
            url = "https://example.com",
            customShortCode = null,
            expiry = pastInstant,
            accessLimit = null
        )

        val ex = assertThrows<RequestException> {
            service.shortenUrl(request)
        }
        assertEquals("Invalid expiry dateTime provided", ex.message)
    }

    @Test
    fun `redirectToOriginalUrl should return originalUrl and save stats`() {
        val shortenCode = "abc123"
        val fakeRequest = mock(HttpServletRequest::class.java)
        val url = Url(
            id = 1L,
            shortCode = shortenCode,
            originalUrl = "https://original.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = null,
            accessLimit = null
        )

        whenever(fakeRequest.getHeader("User-Agent")).thenReturn("JUnit-Test-Agent")
        whenever(fakeRequest.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1")
        whenever(urlRepository.findByShortCode(shortenCode)).thenReturn(url)
        whenever(urlStatsRepository.save(Mockito.any())).thenAnswer { it.arguments[0] }

        val result = service.redirectToOriginalUrl(shortenCode, fakeRequest)

        assertEquals("https://original.com", result)
    }

    @Test
    fun `redirectToOriginalUrl should throw when shortCode is invalid`() {
        whenever(urlRepository.findByShortCode("invalidCode")).thenReturn(null)

        val ex = assertThrows<RequestException> {
            service.redirectToOriginalUrl("invalidCode", mock())
        }
        assertEquals("Invalid shortCode", ex.message)
    }

    @Test
    fun `redirectToOriginalUrl should throw when shortCode is expired`() {
        val expiredUrl = Url(
            id = 1,
            shortCode = "code",
            originalUrl = "https://example.com",
            createdAt = Instant.now().minus(1, ChronoUnit.DAYS),
            updatedAt = Instant.now().minus(12, ChronoUnit.HOURS),
            expiredAt = Instant.now().minus(1, ChronoUnit.MINUTES),
            accessLimit = null
        )

        whenever(urlRepository.findByShortCode("code")).thenReturn(expiredUrl)

        val ex = assertThrows<RequestException> {
            service.redirectToOriginalUrl("code", mock())
        }
        assertEquals("ShortCode is expired", ex.message)
    }

    @Test
    fun `redirectToOriginalUrl should throw when access limit exceeded`() {
        val url = Url(
            id = 1,
            shortCode = "code",
            originalUrl = "https://example.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = null,
            accessLimit = 2
        )
        whenever(urlRepository.findByShortCode("code")).thenReturn(url)
        whenever(urlStatsRepository.countByUrlId(1)).thenReturn(3L)

        val ex = assertThrows<RequestException> {
            service.redirectToOriginalUrl("code", mock())
        }
        assertEquals("ShortCode has exceeded its access limit", ex.message)
    }

    @Test
    fun `getUrlForShortCode should return UrlShortenResponse`() {
        val shortenCode = "abc123"
        val url = Url(
            id = 1L,
            shortCode = shortenCode,
            originalUrl = "https://example.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = null,
            accessLimit = null
        )

        whenever(urlRepository.findByShortCode(shortenCode)).thenReturn(url)

        val result = service.getUrlForShortCode(shortenCode)

        assertEquals("https://example.com", result.originalUrl)
    }

    @Test
    fun `deleteByShortCode should call repository delete`() {
        val code = "abc123"
        val url = Url(
            id = 1L,
            shortCode = code,
            originalUrl = "https://delete.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = null,
            accessLimit = null
        )

        whenever(urlRepository.findByShortCode(code)).thenReturn(url)

        service.deleteByShortCode(code)

        Mockito.verify(urlRepository).deleteById(1L)
    }

    @Test
    fun `updateByShortenCode should update and return UrlShortenResponse`() {
        val code = "abc123"
        val existing = Url(
            id = 1L,
            shortCode = code,
            originalUrl = "https://old.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = null,
            accessLimit = null
        )

        val req = UrlShortenRequest(
            url = "https://new.com",
            customShortCode = null,
            expiry = Instant.now().plus(1, ChronoUnit.DAYS),
            accessLimit = 20
        )

        val updated = existing.copy(
            originalUrl = req.url,
            expiredAt = req.expiry,
            accessLimit = req.accessLimit,
            updatedAt = Instant.now()
        )

        whenever(urlRepository.findByShortCode(code)).thenReturn(existing)
        whenever(urlRepository.save(Mockito.any())).thenReturn(updated)

        val result = service.updateByShortenCode(code, req)

        assertEquals("https://new.com", result.originalUrl)
    }

    @Test
    fun `updateByShortenCode should throw when trying to update shortcode`() {
        val request = UrlShortenRequest(
            url = "https://example.com",
            customShortCode = "newCode",
            expiry = null,
            accessLimit = null
        )

        val ex = assertThrows<RequestException> {
            service.updateByShortenCode("existingCode", request)
        }
        assertEquals("Shortcode can't be updated", ex.message)
    }

    @Test
    fun `updateByShortenCode should throw when shortCode is invalid`() {
        val request = UrlShortenRequest(
            url = "https://example.com",
            customShortCode = null,
            expiry = null,
            accessLimit = null
        )
        whenever(urlRepository.findByShortCode("invalidCode")).thenReturn(null)

        val ex = assertThrows<RequestException> {
            service.updateByShortenCode("invalidCode", request)
        }
        assertEquals("Invalid shortCode", ex.message)
    }

    @Test
    fun `updateByShortenCode should throw when expiry date is in the past`() {
        val url = Url(
            id = 1,
            shortCode = "code",
            originalUrl = "https://old.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = null,
            accessLimit = null
        )
        whenever(urlRepository.findByShortCode("code")).thenReturn(url)

        val pastInstant = Instant.now().minusSeconds(3600)
        val request = UrlShortenRequest(
            url = "https://example.com",
            customShortCode = null,
            expiry = pastInstant,
            accessLimit = null
        )

        val ex = assertThrows<RequestException> {
            service.updateByShortenCode("code", request)
        }
        assertEquals("Invalid expiry dateTime provided", ex.message)
    }

    @Test
    fun `getUrlStats should return visit stats`() {
        val code = "abc123"
        val url = Url(
            id = 1L,
            shortCode = code,
            originalUrl = "https://stats.com",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = null,
            accessLimit = 10
        )

        val visits = listOf(
            UrlStats(1L, 1L, "1.1.1.1", "UA", Instant.now()),
            UrlStats(2L, 1L, "2.2.2.2", "UA2", Instant.now())
        )

        whenever(urlRepository.findByShortCode(code)).thenReturn(url)
        whenever(urlStatsRepository.findAllByUrlId(1L)).thenReturn(visits)

        val result = service.getUrlStats(code)

        assertEquals(2, result.totalVisits)
        assertEquals("https://stats.com", result.originalUrl)
    }
}
