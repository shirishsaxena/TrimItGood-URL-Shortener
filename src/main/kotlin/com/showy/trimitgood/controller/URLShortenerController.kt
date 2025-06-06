package com.showy.trimitgood.controller

import com.showy.trimitgood.dto.request.UrlShortenRequest
import com.showy.trimitgood.dto.response.UrlShortenResponse
import com.showy.trimitgood.dto.response.UrlStatsResponse
import com.showy.trimitgood.service.URLShortenerService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/shorten")
class URLShortenerController(private val urlShortenerService: URLShortenerService) {

    @GetMapping("{shortenCode}")
    fun redirectToOriginalUrl(
        @PathVariable shortenCode: String,
        request: HttpServletRequest
    ): ResponseEntity<Void> {
        val redirectUrl = urlShortenerService.redirectToOriginalUrl(shortenCode, request)
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(redirectUrl))
            .build()
    }

    @GetMapping("{shortenCode}/details")
    fun getUrlForShortCode(
        @PathVariable shortenCode: String
    ): ResponseEntity<UrlShortenResponse> {
        val response = urlShortenerService.getUrlForShortCode(shortenCode)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun shortenUrl(
        @Valid @RequestBody
        request: UrlShortenRequest
    ): ResponseEntity<UrlShortenResponse> {
        val response = urlShortenerService.shortenUrl(request)
        return ResponseEntity.ok(response)
    }

    @PutMapping("{shortenCode}")
    fun updateByShortenCode(
        @PathVariable shortenCode: String,
        @Valid @RequestBody
        request: UrlShortenRequest
    ): ResponseEntity<UrlShortenResponse> {
        val response = urlShortenerService.updateByShortenCode(shortenCode, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("{shortenCode}")
    fun deleteByShortCode(
        @PathVariable shortenCode: String
    ): ResponseEntity<Void> {
        urlShortenerService.deleteByShortCode(shortenCode)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("{shortenCode}/stats")
    fun getUrlStats(
        @PathVariable shortenCode: String
    ): ResponseEntity<UrlStatsResponse> {
        val response = urlShortenerService.getUrlStats(shortenCode)
        return ResponseEntity.ok(response)
    }
}
