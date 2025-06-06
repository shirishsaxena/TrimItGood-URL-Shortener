package com.showy.trimitgood.service

import com.showy.trimitgood.repository.UrlStatsRepository
import org.springframework.stereotype.Service

@Service
class URLStatsService(
    private val urlStatsRepository: UrlStatsRepository,
    private val urlShortenerService: URLShortenerService
)
