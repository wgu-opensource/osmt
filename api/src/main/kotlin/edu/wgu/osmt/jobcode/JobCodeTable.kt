package edu.wgu.osmt.jobcode

import edu.wgu.osmt.db.BaseTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime

object JobCodeTable: LongIdTable("JobCode"), BaseTable {
    override val creationDate = datetime("creationDate")

    val major: Column<String?> = varchar("major", 1024).nullable()
    val minor: Column<String?> = varchar("minor", 1024).nullable()
    val broad: Column<String?> = varchar("broad", 1024).nullable()
    val detailed: Column<String?> = varchar("detailed", 1024).nullable()

    val code: Column<String> = varchar("code", 128).index()
    val name: Column<String?> = varchar("name", 1024).nullable()
    val description: Column<String?> = text("description").nullable()
    val framework: Column<String?> = varchar("framework", 1024).nullable()
    val url: Column<String?> = varchar("url", 1024).nullable()
}
