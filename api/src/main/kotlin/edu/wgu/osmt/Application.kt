package edu.wgu.osmt

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.db.TableWithMappers
import edu.wgu.osmt.elasticsearch.ElasticsearchService
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import kotlinx.coroutines.runBlocking
import org.elasticsearch.client.RestHighLevelClient
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication(exclude = arrayOf(DataSourceAutoConfiguration::class, FlywayAutoConfiguration::class))
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@EnableWebFlux
class Application {

	@Autowired
	private lateinit var appConfig: AppConfig

	@Autowired
	private lateinit var flywayManager: FlywayManager

	@Autowired
	private lateinit var elasticsearchService: ElasticsearchService

	@Autowired
	private lateinit var tables: List<TableWithMappers<*>>

	@Bean
	fun commandLineRunner(): CommandLineRunner {
		return CommandLineRunner {
			initializeTables()

			// TODO this works for happy path migrations, additional logic may be necessary for other flows
			flywayManager.flyway.migrate()

			elasticsearchService.findAll()

		}
	}

	fun initializeTables() {
		runBlocking {
			if (appConfig.dbConfig.createTablesAndColumnsIfMissing) {
				tables.forEach { table ->
					transaction { SchemaUtils.createMissingTablesAndColumns(table) }
				}
			}
		}


	}
}

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
