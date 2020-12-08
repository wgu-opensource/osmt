package edu.wgu.osmt.db

import edu.wgu.osmt.auditlog.AuditLogTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SortOrder
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface BaseTable {
    val creationDate: Column<LocalDateTime>
}

interface TableWithUpdate<UpdateObjectType : UpdateObject<*>> : BaseTable {
    val updateDate: Column<LocalDateTime>
}

fun LongIdTable.sortAdapter(pageable: Pageable?): Array<Pair<Column<*>, SortOrder>> = if (pageable != null){
    pageable.sort.mapNotNull { order ->
        val column = this.columns.firstOrNull() {it.name == order.property} ?: throw Error("invalid order parameter")
        val sortOrder = SortOrder.valueOf(order.direction.name)
        val statement = column to sortOrder
        statement
    }.toTypedArray()
} else {
    arrayOf()
}
