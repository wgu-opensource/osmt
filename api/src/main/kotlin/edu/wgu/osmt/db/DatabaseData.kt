package edu.wgu.osmt.db

abstract class DatabaseData<T> {
    abstract val id: Long?
    abstract fun withId(id: Long): T
}
