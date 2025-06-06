package com.showy.trimitgood.integrationtests

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.showy.trimitgood.dto.request.UrlShortenRequest
import com.showy.trimitgood.dto.response.UrlShortenResponse
import com.showy.trimitgood.model.Url
import com.showy.trimitgood.model.UrlStats
import com.showy.trimitgood.repository.UrlRepository
import com.showy.trimitgood.repository.UrlStatsRepository
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UrlShortenerIntegrationTest @Autowired constructor(
    val mockMvc: MockMvc,
    val urlRepository: UrlRepository,
    val urlStatsRepository: UrlStatsRepository
) {

    private final val basePath = "/api/v1/shorten"
    private val objectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @BeforeAll
    fun setup() {
        urlStatsRepository.deleteAll()
        urlRepository.deleteAll()
    }

    @ParameterizedTest
    @MethodSource(
        "com.showy.trimitgood.integrationtests.helper." +
            "UrlShortenerIntegrationTestProvider#provideShortenUrlRequestsSuccess"
    )
    fun `POST shortenUrl should create and return shortened url`(request: UrlShortenRequest) {
        val expectedResponse = UrlShortenResponse(
            id = 1,
            shortCode = "random",
            originalUrl = request.url,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = request.expiry,
            accessLimit = request.accessLimit
        )

        val mvcResult = mockMvc.post(basePath) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.shortCode") { exists() }
            jsonPath("$.id") { exists() }
        }.andReturn()

        val actualResponse = objectMapper.readValue(
            mvcResult.response.contentAsString,
            UrlShortenResponse::class.java
        )

        assertThat(actualResponse)
            .usingRecursiveComparison()
            .ignoringFields("createdAt", "updatedAt", "shortCode", "id")
            .isEqualTo(expectedResponse)
    }

    @Test
    fun `GET redirectToOriginalUrl should redirect to original url`() {
        val url = urlRepository.save(
            Url(
                shortCode = "redir123",
                originalUrl = "https://redirect.test",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                expiredAt = null,
                accessLimit = null
            )
        )

        mockMvc.get("$basePath/${url.shortCode}") {
            header("X-Forwarded-For", "127.0.0.1")
            header("User-Agent", "MockMvc-Test-Agent/1.0")
        }.andExpect {
            status { isFound() }
            header { string("Location", "https://redirect.test") }
        }
    }

    @Test
    fun `GET getUrlForShortCode should return url details`() {
        val url = urlRepository.save(
            Url(
                shortCode = "details123",
                originalUrl = "https://details.test",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                expiredAt = null,
                accessLimit = 5
            )
        )

        mockMvc.get("$basePath/${url.shortCode}/details")
            .andExpect {
                status { isOk() }
                jsonPath("$.shortCode") { value("details123") }
                jsonPath("$.originalUrl") { value("https://details.test") }
                jsonPath("$.accessLimit") { value(5) }
            }
    }

    @Test
    fun `PUT updateByShortenCode should update url`() {
        val url = urlRepository.save(
            Url(
                shortCode = "update123",
                originalUrl = "https://beforeupdate.test",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                expiredAt = null,
                accessLimit = 10
            )
        )

        val updateRequest = UrlShortenRequest(
            url = "https://afterupdate.test",
            customShortCode = null,
            expiry = null,
            accessLimit = 15
        )

        mockMvc.put("$basePath/${url.shortCode}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.originalUrl") { value("https://afterupdate.test") }
                jsonPath("$.accessLimit") { value(15) }
            }
    }

    @Test
    fun `DELETE deleteByShortCode should delete url`() {
        val url = urlRepository.save(
            Url(
                shortCode = "delete123",
                originalUrl = "https://tobedeleted.test",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                expiredAt = null,
                accessLimit = null
            )
        )

        mockMvc.delete("$basePath/${url.shortCode}")
            .andExpect {
                status { isNoContent() }
            }

        assertFalse(urlRepository.existsById(url.id))
    }

    @Test
    fun `GET getUrlStats should return stats for url`() {
        val url = urlRepository.save(
            Url(
                shortCode = "stats123",
                originalUrl = "https://stats.test",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                expiredAt = null,
                accessLimit = null
            )
        )
        urlStatsRepository.saveAll(
            listOf(
                UrlStats(urlId = url.id, ipAccessFrom = "127.0.0.1", userAgent = "JUnit", accessedAt = Instant.now()),
                UrlStats(urlId = url.id, ipAccessFrom = "192.168.1.1", userAgent = "JUnit", accessedAt = Instant.now())
            )
        )

        mockMvc.get("$basePath/${url.shortCode}/stats")
            .andExpect {
                status { isOk() }
                jsonPath("$.totalVisits") { value(2) }
                jsonPath("$.details") { isArray() }
                jsonPath("$.details[0].ipAccessFrom") { exists() }
            }
    }

    @Test
    fun `GET redirectToOriginalUrl should throw when short code expired`() {
        val expiredInstant = Instant.now().minusSeconds(3600)
        val url = urlRepository.save(
            Url(
                shortCode = "expired123",
                originalUrl = "https://expired.test",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                expiredAt = expiredInstant,
                accessLimit = null
            )
        )

        mockMvc.get("$basePath/${url.shortCode}")
            .andExpect {
                status { isBadRequest() }
                content { string(containsString("ShortCode is expired")) }
            }
    }

    @Test
    fun `GET redirectToOriginalUrl should throw when access limit exceeded`() {
        val url = urlRepository.save(
            Url(
                shortCode = "limit123",
                originalUrl = "https://limit.test",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                expiredAt = null,
                accessLimit = 1
            )
        )
        // Insert 2 stats, exceeding limit 1
        urlStatsRepository.saveAll(
            listOf(
                UrlStats(urlId = url.id, ipAccessFrom = "1.1.1.1", userAgent = "JUnit", accessedAt = Instant.now()),
                UrlStats(urlId = url.id, ipAccessFrom = "2.2.2.2", userAgent = "JUnit", accessedAt = Instant.now())
            )
        )

        mockMvc.get("$basePath/${url.shortCode}")
            .andExpect {
                status { isBadRequest() }
                content { string(containsString("ShortCode has exceeded its access limit")) }
            }
    }

    @Test
    fun `GET redirectToOriginalUrl should throw on invalid short code`() {
        mockMvc.get("$basePath/invalidCode")
            .andExpect {
                status { isBadRequest() }
                content { string(containsString("Invalid shortCode")) }
            }
    }

    @Test
    fun `PUT updateByShortenCode should throw if customShortCode in request`() {
        val url = urlRepository.save(
            Url(
                shortCode = "nocustom123",
                originalUrl = "https://nocustom.test",
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                expiredAt = null,
                accessLimit = null
            )
        )
        val request = UrlShortenRequest(
            url = "https://newurl.test",
            customShortCode = "shouldfail",
            expiry = null,
            accessLimit = null
        )

        mockMvc.put("$basePath/${url.shortCode}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isBadRequest() }
                content { string(containsString("Shortcode can't be updated")) }
            }
    }
}
