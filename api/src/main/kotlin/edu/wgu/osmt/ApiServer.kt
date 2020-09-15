package edu.wgu.osmt

import edu.wgu.osmt.auditlog.AuditLogTable
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.collection.CollectionTable
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.PublishStatusTable
import edu.wgu.osmt.db.addMissingColumnsStatementsPublic
import edu.wgu.osmt.jobcode.JobCodeTable
import edu.wgu.osmt.keyword.KeywordTable
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import edu.wgu.osmt.richskill.RichSkillJobCodes
import edu.wgu.osmt.richskill.RichSkillKeywords
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.api.FlywayException
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Component
@Profile("apiserver")
@EnableWebMvc
class ApiServer {
    private val tableList: List<Table> = listOf(
        AuditLogTable,
        RichSkillDescriptorTable,
        JobCodeTable,
        RichSkillJobCodes,
        KeywordTable,
        RichSkillKeywords,
        PublishStatusTable,
        CollectionTable,
        CollectionSkills
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
                var missingStatements: MutableList<String> = mutableListOf()
                tableList.forEach { table ->
                    transaction {
                        val statements = SchemaUtils.createStatements(table)
                        val missingColumnStatements = SchemaUtils.addMissingColumnsStatementsPublic(table)
                        if (statements.isNotEmpty()) {
                            missingStatements.addAll(statements)
                        }
                        if (missingColumnStatements.isNotEmpty()) {
                            missingStatements.addAll(missingColumnStatements)
                        }
                    }
                }
                if (missingStatements.size > 0) {
                    println("Database out of sync with application!")
                    missingStatements.forEach { println("$it;") }
                } else {
                    println("Tables ${tableList.map { it.tableName }} are in sync with application!")
                }
            }
        }
    }
}
