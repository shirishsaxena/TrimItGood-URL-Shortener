package com.showy.trimitgood.util

import com.showy.trimitgood.repository.UrlRepository
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class UrlShortenerUtil(
    private val urlRepository: UrlRepository
) {
    private final val MAX_ATTEMPT = 5
    private final val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz123456789"
    private final val charToIndex = alphabet.withIndex().associate { it.value to it.index }
    private final val CODE_LENGTH = 6
    private val random = SecureRandom()

    fun generateUniqueShortCode(length: Int = CODE_LENGTH, maxAttempts: Int = MAX_ATTEMPT): String {
        repeat(maxAttempts) {
            val shortCode = (1..length)
                .map { alphabet[random.nextInt(alphabet.length)] }
                .joinToString("")
            if (!urlRepository.existsByShortCode(shortCode)) {
                return shortCode
            }
        }
        throw RuntimeException("Failed to generate unique short code after $maxAttempts attempts")
    }

    fun generateUniqueShortCode(sequence: Long, maxAttempts: Int = MAX_ATTEMPT): String {
        repeat(maxAttempts) {
            val shortCode = base10ToBase62(sequence)
            if (!urlRepository.existsByShortCode(shortCode)) {
                return shortCode
            }
        }
        throw RuntimeException("Failed to generate unique short code after $maxAttempts attempts")
    }

    private fun base10ToBase62(num: Long): String {
        if (num == 0L) return "0"
        var n = num
        val result = StringBuilder()
        while (n > 0) {
            result.append(alphabet[(n % 62).toInt()])
            n /= 62
        }
        return result.reverse().toString()
    }

    private fun base62ToBase10(url: String): Long {
        var result = 0L
        for (char in url) {
            val value = charToIndex[char]
                ?: throw IllegalArgumentException("Invalid character in Base62 string: $char")
            result = result * 62 + value
        }
        return result
    }
}
