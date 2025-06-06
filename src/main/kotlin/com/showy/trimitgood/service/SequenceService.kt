package com.showy.trimitgood.service

import com.showy.trimitgood.repository.UrlSequenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SequenceService(
    private val urlSequenceRepository: UrlSequenceRepository
) {
    @Transactional
    fun getNext(): Long {
        val seq = urlSequenceRepository.findById(1L)
            .orElseThrow { IllegalArgumentException("Url Sequence entry not found") }
        val curr = seq.getAndIncrement()
        urlSequenceRepository.save(seq)
        return curr
    }
}
