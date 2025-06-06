package com.showy.trimitgood.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "url_sequence", schema = "sho")
data class UrlSequence(
    @Id
    val id: Long = 1L,

    @Column(name = "curr_no", nullable = false)
    var currNo: Long = 0L
) {
    fun getAndIncrement(): Long {
        val curr = this.currNo
        this.currNo += 1
        return curr
    }
}
