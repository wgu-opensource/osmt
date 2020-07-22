package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.transaction.annotation.Transactional

interface CrudRepository<T> where T : DatabaseData<T> {
    val table: TableWithMappers<T>

    suspend fun findAll() = newSuspendedTransaction() { table.selectAll().map { table.fromRow(it) }  }

    suspend fun findById(id: Long): T? = transaction {
        val row = table.select(table.id eq id).singleOrNull()
        row?.let{table.fromRow(it)}

    }

    @Transactional
    suspend fun insert(t: T): T = newSuspendedTransaction() {
        val id = table.insert{
            toRow(it, t)
        } get table.id
        t.withId(id)
    }
}


