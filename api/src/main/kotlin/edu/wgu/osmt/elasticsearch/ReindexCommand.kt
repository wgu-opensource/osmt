package edu.wgu.osmt.elasticsearch

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
@Profile("reindex")
class ReindexCommand: CommandLineRunner {
    val logger: Logger = LoggerFactory.getLogger(ReindexCommand::class.java)

    @Autowired
    lateinit var elasticSearchReindexer: ElasticSearchReindexer

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    override fun run(vararg args: String?) {
        elasticSearchReindexer.reindexAll()
        (applicationContext as ConfigurableApplicationContext).close()
        exitProcess(0) // "./osmt_cli.sh -r" blocks without this
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(ReindexCommand::class.java, *args)
}
