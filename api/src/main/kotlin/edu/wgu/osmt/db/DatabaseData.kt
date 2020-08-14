package edu.wgu.osmt.db

import java.time.LocalDateTime

interface HasUpdateDate {
    val updateDate: LocalDateTime
}

interface DatabaseData {
    val id: Long?
    val creationDate: LocalDateTime
}

interface OutputsModel<out T> {
    fun toModel(): T
}
