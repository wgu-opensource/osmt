package edu.wgu.osmt.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.springframework.data.domain.Sort
import org.springframework.data.util.Streamable

interface PaginationHelpers<T> where T : LongIdTable, T : PublishStatusUpdate<*> {
    val table: T

    fun publishStatusSetToQuery(publishStatusSet: Set<PublishStatus>): Query {
        return when (publishStatusSet) {
            setOf(PublishStatus.Published) -> {
                table.select { table.publishDate.isNotNull() and (table.publishDate greater table.archiveDate) }
            }
            setOf(PublishStatus.Archived) -> {
                table.select { table.archiveDate.isNotNull() and (table.archiveDate greater table.publishDate) }
            }
            setOf(PublishStatus.Unarchived) -> {
                table.select { table.archiveDate.isNull() and (table.publishDate.isNull()) }
            }
            setOf(PublishStatus.Unarchived, PublishStatus.Archived) -> {
                table.select { table.publishDate.isNull() or (table.archiveDate greater table.publishDate) }
            }
            setOf(PublishStatus.Unarchived, PublishStatus.Published) -> {
                table.select { table.publishDate.isNull() or (table.publishDate greater table.archiveDate) }
            }
            setOf(PublishStatus.Archived, PublishStatus.Published) -> {
                table.select { table.archiveDate.isNotNull() or (table.publishDate.isNotNull()) }
            }
            else -> {
                table.selectAll()
            }
        }
    }

    fun sortToQueryOrder(sort: Sort, query: Query): Streamable<Pair<Column<*>, SortOrder>> {
        return sort.map { order ->
            val column = table.columns.firstOrNull() { it.name == order.property } ?: throw Error("invalid order param")
            val sortOrder = SortOrder.valueOf(order.direction.name)
            val statement = column to sortOrder
            statement
        }
    }
}

data class PaginatedResults<T>(val total: Int, val results: List<T>)
