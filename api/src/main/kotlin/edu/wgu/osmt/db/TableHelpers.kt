package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.Column
import java.time.LocalDateTime

interface BaseTable {
    val creationDate: Column<LocalDateTime>
}

interface TableWithUpdate<UpdateObjectType : UpdateObject<*>> : BaseTable {
    val updateDate: Column<LocalDateTime>
}

