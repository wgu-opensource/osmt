package edu.wgu.osmt

import edu.wgu.osmt.auditlog.AuditLogTable
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.DbConfig
import edu.wgu.osmt.db.PublishStatusTable
import edu.wgu.osmt.elasticsearch.EsConfig
import edu.wgu.osmt.jobcode.JobCodeTable
import edu.wgu.osmt.keyword.KeywordTable
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import edu.wgu.osmt.richskill.RichSkillJobCodes
import edu.wgu.osmt.richskill.RichSkillKeywords
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.api.FlywayException
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.currentDialect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import java.util.ArrayList

@SpringBootApplication(exclude = arrayOf(DataSourceAutoConfiguration::class, FlywayAutoConfiguration::class))
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@EnableConfigurationProperties(DbConfig::class, EsConfig::class)
@EnableWebMvc
class Application {

    private val tableList: List<Table> = listOf(
        AuditLogTable,
        RichSkillDescriptorTable,
        JobCodeTable,
        RichSkillJobCodes,
        KeywordTable,
        RichSkillKeywords,
        PublishStatusTable
    )

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var flywayManager: FlywayManager

    @Bean
    fun commandLineRunner(): CommandLineRunner {
        return CommandLineRunner {
            // TODO this works for happy path migrations, additional logic may be necessary for other flows
            try {
                flywayManager.flyway.info()
                flywayManager.flyway.migrate()
            } catch (e: FlywayException) {
                println("Migration exception occurred: ${e.message.toString()}")
            }
            printMissingTableAndColumnStatements()
        }
    }

    fun printMissingTableAndColumnStatements() {
        runBlocking {
            if (appConfig.dbConfig.showMissingCreateTableStatements) {
                var missingStatements = 0
                tableList.forEach { table ->
                    transaction {
                        val statements = SchemaUtils.createStatements(table)
                        val missingColumnStatements = SchemaUtils.addMissingColumnsStatementsPublic(table)
                        if (statements.isNotEmpty()) {
                            missingStatements += 1
                            println("${table.tableName} create statement:")
                            println(statements)
                        }
                        if (missingColumnStatements.isNotEmpty()) {
                            missingStatements += 1
                            println("${table.tableName} missing column statements:")
                            println(missingColumnStatements)
                        }
                    }
                }
                if (missingStatements == 0) {
                    println("Tables ${tableList.map { it.tableName }} are in sync with application!")
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

// TODO copy/paste to get around private visibility
fun SchemaUtils.addMissingColumnsStatementsPublic(vararg tables: Table): List<String> {
    with(TransactionManager.current()) {
        val statements = ArrayList<String>()
        if (tables.isEmpty())
            return statements

        val existingTableColumns = currentDialect.tableColumns(*tables)

        for (table in tables) {
            //create columns
            val thisTableExistingColumns = existingTableColumns[table].orEmpty()
            val missingTableColumns =
                table.columns.filterNot { c -> thisTableExistingColumns.any { it.name.equals(c.name, true) } }
            missingTableColumns.flatMapTo(statements) { it.ddl }

            if (db.supportsAlterTableWithAddColumn) {
                // create indexes with new columns
                for (index in table.indices) {
                    if (index.columns.any { missingTableColumns.contains(it) }) {
                        statements.addAll(createIndex(index))
                    }
                }

                // sync nullability of existing columns
                val incorrectNullabilityColumns = table.columns.filter { c ->
                    thisTableExistingColumns.any {
                        c.name.equals(
                            it.name,
                            true
                        ) && it.nullable != c.columnType.nullable
                    }
                }
                incorrectNullabilityColumns.flatMapTo(statements) { it.modifyStatement() }
            }
        }

        if (db.supportsAlterTableWithAddColumn) {
            val existingColumnConstraint = db.dialect.columnConstraints(*tables)

            for (table in tables) {
                for (column in table.columns) {
                    val foreignKey = column.foreignKey
                    if (foreignKey != null) {
                        val existingConstraint = existingColumnConstraint[table to column]?.firstOrNull()
                        if (existingConstraint == null) {
                            statements.addAll(createFKey(column))
                        } else if (existingConstraint.target.table != foreignKey.target.table
                            || foreignKey.deleteRule != existingConstraint.deleteRule
                            || foreignKey.updateRule != existingConstraint.updateRule
                        ) {
                            statements.addAll(existingConstraint.dropStatement())
                            statements.addAll(createFKey(column))
                        }
                    }
                }
            }
        }

        return statements
    }
}
