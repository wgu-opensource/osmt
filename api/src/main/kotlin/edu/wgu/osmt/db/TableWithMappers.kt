package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder

abstract class TableWithMappers<T>(name: String): Table(name){
    val id = long("id").autoIncrement().primaryKey()

    abstract fun fromRow(t: ResultRow): T
    abstract fun toRow(it: UpdateBuilder<Number>, t: T)
}
