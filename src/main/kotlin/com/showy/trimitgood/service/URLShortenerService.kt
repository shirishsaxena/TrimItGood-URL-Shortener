package com.showy.trimitgood.service

import com.showy.trimitgood.dto.request.UrlShortenRequest
import com.showy.trimitgood.dto.response.UrlShortenResponse
import com.showy.trimitgood.dto.response.UrlStatsResponse
import com.showy.trimitgood.exception.RequestException
import com.showy.trimitgood.model.Url
import com.showy.trimitgood.model.UrlStats
import com.showy.trimitgood.repository.UrlRepository
import com.showy.trimitgood.repository.UrlStatsRepository
import com.showy.trimitgood.util.RequestUtil
import com.showy.trimitgood.util.UrlShortenerUtil
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class URLShortenerService(
    private val sequenceService: SequenceService,
    private val urlRepository: UrlRepository,
    private val urlStatsRepository: UrlStatsRepository,
    private val urlShortenerUtil: UrlShortenerUtil
) {
    fun shortenUrl(request: UrlShortenRequest): UrlShortenResponse {
        val shortCode = validateAndGenerateShortCode(request.customShortCode)
        val expiredAt = request.expiry?.also { time ->
            if (time.isBefore(Instant.now())) {
                throw RequestException("Invalid expiry dateTime provided")
            }
        }

        val url = Url(
            shortCode = shortCode,
            originalUrl = request.url,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = expiredAt,
            accessLimit = request.accessLimit
        )

        return urlRepository.save(url).toResponseDto()
    }

    private fun validateAndGenerateShortCode(customShortCode: String?): String {
        customShortCode?.let { shortCode ->
            val trimmedCode = shortCode.trim()

            if (trimmedCode.isEmpty()) {
                throw RequestException("Custom short code cannot be blank")
            }

            if (urlRepository.existsByShortCode(trimmedCode)) {
                throw RequestException("ShortCode already exists")
            }
            return trimmedCode
        }

        val nextSequenceValue = sequenceService.getNext()
        return urlShortenerUtil.generateUniqueShortCode(nextSequenceValue)
    }

    fun redirectToOriginalUrl(
        shortenCode: String,
        request: HttpServletRequest
    ): String {
        val url = urlRepository.findByShortCode(shortenCode)
            ?: throw RequestException("Invalid shortCode")

        url.expiredAt?.let { expiry ->
            if (expiry.isBefore(Instant.now())) {
                throw RequestException("ShortCode is expired")
            }
        }

        url.accessLimit?.let { limit ->
            val accessCount = urlStatsRepository.countByUrlId(url.id)
            if (accessCount >= limit) {
                throw RequestException("ShortCode has exceeded its access limit")
            }
        }

        val urlStats = UrlStats(
            urlId = url.id,
            ipAccessFrom = RequestUtil.getClientIpSafe(request),
            userAgent = RequestUtil.getUserAgent(request),
            accessedAt = Instant.now()
        )

        urlStatsRepository.save(urlStats)

        return url.originalUrl
    }

    fun getUrlForShortCode(shortenCode: String): UrlShortenResponse {
        val url = urlRepository.findByShortCode(shortenCode)
            ?: throw RequestException("Invalid shortCode")
        return url.toResponseDto()
    }

    fun deleteByShortCode(shortenCode: String) {
        val url = urlRepository.findByShortCode(shortenCode)
            ?: throw RequestException("Invalid shortCode")
        urlRepository.deleteById(url.id)
    }

    fun updateByShortenCode(shortenCode: String, request: UrlShortenRequest): UrlShortenResponse {
        request.customShortCode
            ?.let { throw RequestException("Shortcode can't be updated") }

        val url = urlRepository.findByShortCode(shortenCode)
            ?: throw RequestException("Invalid shortCode")

        val expiredAt = request.expiry?.also { time ->
            if (time.isBefore(Instant.now())) {
                throw RequestException("Invalid expiry dateTime provided")
            }
        }

        val updatedUrl = Url(
            id = url.id,
            shortCode = url.shortCode,
            originalUrl = request.url,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            expiredAt = expiredAt,
            accessLimit = request.accessLimit
        )

        return urlRepository.save(updatedUrl).toResponseDto()
    }

    fun getUrlStats(shortenCode: String): UrlStatsResponse {
        val url = urlRepository.findByShortCode(shortenCode)
            ?: throw RequestException("Invalid shortCode")

        val urlStatsList = urlStatsRepository.findAllByUrlId(url.id)

        val visitStatsList = urlStatsList.map { item ->
            UrlStatsResponse.VisitStats(
                accessedAt = item.accessedAt,
                ipAccessFrom = item.ipAccessFrom,
                userAgent = item.userAgent
            )
        }.toList()

        return UrlStatsResponse(
            shortCode = url.shortCode,
            originalUrl = url.originalUrl,
            createdAt = url.createdAt,
            updatedAt = url.updatedAt,
            expiredAt = url.expiredAt,
            accessLimit = url.accessLimit,
            totalVisits = visitStatsList.size,
            details = visitStatsList,
            remainingVisits = url.accessLimit?.let { limit -> limit - visitStatsList.size }
        )
    }
}
