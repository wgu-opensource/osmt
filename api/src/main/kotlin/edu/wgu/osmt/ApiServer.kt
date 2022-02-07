package edu.wgu.osmt

import edu.wgu.osmt.auditlog.AuditLogTable
import edu.wgu.osmt.auditlog.AuditLogUtils
import edu.wgu.osmt.collection.CollectionSkills
import edu.wgu.osmt.collection.CollectionTable
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.addMissingColumnsStatementsPublic
import edu.wgu.osmt.jobcode.JobCodeTable
import edu.wgu.osmt.keyword.KeywordTable
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import edu.wgu.osmt.richskill.RichSkillJobCodes
import edu.wgu.osmt.richskill.RichSkillKeywords
import edu.wgu.osmt.searchhub.client.apis.LibrariesApi
import edu.wgu.osmt.searchhub.client.apis.SearchingApi
import edu.wgu.osmt.searchhub.client.apis.SharingApi
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Component
@Profile("apiserver")
@EnableWebMvc
@EnableCaching
class ApiServer {
    val logger: Logger = LoggerFactory.getLogger(ApiServer::class.java)

    private val tableList: List<Table> = listOf(
        AuditLogTable,
        RichSkillDescriptorTable,
        JobCodeTable,
        RichSkillJobCodes,
        KeywordTable,
        RichSkillKeywords,
        CollectionTable,
        CollectionSkills
    )

    @Autowired
    private lateinit var appConfig: AppConfig

    @Autowired
    private lateinit var auditLogUtils: AuditLogUtils

    @Bean
    fun searchHubLibarariesApi(): LibrariesApi? {
        return if (appConfig.searchHubEnabled) {
            val api = appConfig.searchHubBaseUrl?.let {
                LibrariesApi(appConfig.searchHubBaseUrl!!)
            } ?: LibrariesApi()

            appConfig.searchHubAccessToken?.let {
                api.setAccessToken(appConfig.searchHubAccessToken)
            }

            api
        } else null
    }

    @Bean
    fun searchHubSearchingApi(): SearchingApi? {
        return if (appConfig.searchHubEnabled) {
            val api = appConfig.searchHubBaseUrl?.let {
                SearchingApi(appConfig.searchHubBaseUrl!!)
            } ?: SearchingApi()

            appConfig.searchHubAccessToken?.let {
                api.setAccessToken(appConfig.searchHubAccessToken)
            }

            api
        } else null
    }

    @Bean
    fun searchHubSharingApi(): SharingApi? {
        return if (appConfig.searchHubEnabled) {
            val api = appConfig.searchHubBaseUrl?.let {
                SharingApi(appConfig.searchHubBaseUrl!!)
            } ?: SharingApi()

            appConfig.searchHubAccessToken?.let {
                api.setAccessToken(appConfig.searchHubAccessToken)
            }

            api
        } else null
    }

    @Bean
    fun commandLineRunner(): CommandLineRunner {
        return CommandLineRunner {
            printMissingTableAndColumnStatements()
            auditLogUtils.baseLineIfEmpty()
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
                    logger.warn("Database out of sync with application!")
                    missingStatements.forEach { println("$it;") }
                } else {
                    logger.info("Tables ${tableList.map { it.tableName }} are in sync with application!")
                }
            }
        }
    }
}
