package com.showy.trimitgood.exception

class RequestException(
    override val message: String,
    val statusCode: Int = 400,
    val errorDetails: String? = null
) : RuntimeException(message)
