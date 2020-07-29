package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.transaction.annotation.Transactional

interface HasInsert<T: DatabaseData<T>>{
    val table: TableWithMappers<T, *>

    @Transactional
    suspend fun insert(t: T, user: OAuth2User? = null): T = newSuspendedTransaction() {
        val id = table.insert{
            toRowFromT(it, t)
        } get table.id
        t.withId(id)
    }
}

interface HasUpdate<T: DatabaseData<T>, in UpdateObjectType: UpdateObject>{
    val table: TableWithMappers<T, UpdateObjectType>

    @Transactional
    suspend fun update(updateObject: UpdateObjectType, user: OAuth2User? = null): Int? {
        return table.update({table.id eq updateObject.id}){
            toRowFromUpdateObject(it, updateObject)
        }
    }
}

interface BaseRepository<T: DatabaseData<T>> {
    val table: TableWithMappers<T, *>

    suspend fun findAll() = newSuspendedTransaction() { table.selectAll().map { table.fromRow(it) }  }

    suspend fun findById(id: Long): T? = transaction {
        val row = table.select(table.id eq id).singleOrNull()
        row?.let{table.fromRow(it)}

    }
}

interface CrudRepository<T: DatabaseData<T>, in UpdateObjectType: UpdateObject>:BaseRepository<T>, HasInsert<T>, HasUpdate<T, UpdateObjectType>


