package edu.wgu.osmt.db

import java.time.LocalDateTime

interface HasUpdateDate {
    val updateDate: LocalDateTime
}

interface DatabaseData<T> {
    val id: Long?
    val creationDate: LocalDateTime

    fun withId(id: Long): T
}

interface OutputsModel<T> {
    fun toModel(): T
}
