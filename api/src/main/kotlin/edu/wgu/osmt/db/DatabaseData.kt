package edu.wgu.osmt.db

import java.time.LocalDateTime

interface HasUpdateDate{
    val updateDate: LocalDateTime
}

abstract class DatabaseData<T> {
    abstract val id: Long?
    abstract val creationDate: LocalDateTime

    abstract fun withId(id: Long): T
}
