package com.showy.trimitgood.util

import jakarta.servlet.http.HttpServletRequest
import java.net.InetAddress

object RequestUtil {

    fun getClientIpSafe(request: HttpServletRequest): String? {
        val rawIp = request.getHeader("X-Forwarded-For")
            ?.split(",")
            ?.firstOrNull()
            ?.trim()
            ?: request.remoteAddr

        return runCatching {
            InetAddress.getByName(rawIp)
            rawIp
        }.getOrElse { null }
    }

    fun getUserAgent(request: HttpServletRequest): String? {
        return request.getHeader("User-Agent").take(100) ?: null
    }
}
