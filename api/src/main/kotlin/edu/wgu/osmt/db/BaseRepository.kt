package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.transaction.annotation.Transactional

interface HasInsert<T : DatabaseData<T>> {
    val table: TableWithMappers<T>

    @Transactional
    suspend fun insert(t: T, user: OAuth2User? = null): T = newSuspendedTransaction() {
        val id = table.insert {
            insertStatementApplyFromT(it, t)
        } get table.id
        t.withId(id.value)
    }
}

interface HasUpdate<T : DatabaseData<T>, in UpdateObjectType : UpdateObject> {
    val table: TableWithUpdateMapper<T, UpdateObjectType>

    @Transactional
    suspend fun update(updateObject: UpdateObjectType, user: OAuth2User? = null): Int? = newSuspendedTransaction {
        table.update({ table.id eq updateObject.id }) {
            updateBuilderApplyFromUpdateObject(it, updateObject)
        }
    }
}

interface DslCrudRepository<T : DatabaseData<T>, in UpdateObjectType : UpdateObject> : HasInsert<T>,
    HasUpdate<T, UpdateObjectType>


