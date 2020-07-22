package edu.wgu.osmt

import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application{

	@Autowired
	private lateinit var appConfig: AppConfig

	@Autowired
	private lateinit var richSkillDescriptorTable: RichSkillDescriptorTable


	@Bean
	fun commandLineRunner(): CommandLineRunner{
		return CommandLineRunner {
			runBlocking {
				if (appConfig.environment.activeProfiles.contains("dev")) {
					transaction { SchemaUtils.createMissingTablesAndColumns(richSkillDescriptorTable) } // TODO remove once Flyway is fully established
				}
			}

		}
	}
}

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
