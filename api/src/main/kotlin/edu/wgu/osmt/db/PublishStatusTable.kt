package edu.wgu.osmt.db

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

enum class PublishStatus {
    Unpublished,
    Published,
    Archived
}

/**
 * This table should only hold values corresponding to the [[PublishStatus]] enum
 * [[PublishStatus.ordinal]] to [[PublishStatusTable.id]]
 * [[PublishStatus.name]]  to [[PublishStatusTable.name]]
 * e.g.
 * +----+-------------+
 * | id | name        |
 * +----+-------------+
 * |  0 | Unpublished |
 * |  1 | Published   |
 * |  2 | Archived    |
 * +----+-------------+
 */
object PublishStatusTable : LongIdTable("PublishStatus") {
    val name = varchar("name", 64)
}

class PublishStatusDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PublishStatusDao>(PublishStatusTable)

    var name by PublishStatusTable.name
    val statusEnum: PublishStatus
        get() = PublishStatus.values()[id.value.toInt()]
}
