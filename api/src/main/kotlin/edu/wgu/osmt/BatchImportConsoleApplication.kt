package edu.wgu.osmt

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext

@SpringBootApplication
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
class BatchImportConsoleApplication : CommandLineRunner {
    val LOG: Logger = LoggerFactory.getLogger(BatchImportConsoleApplication::class.java)

    @Autowired
    private lateinit var applicationContext: ApplicationContext;

    fun processCsv(csv_path: String) {

        LOG.info("hello process csv: ${csv_path}")
    }

    override fun run(vararg args: String?) {
        args[0]?.let { processCsv(it) }
        (applicationContext as ConfigurableApplicationContext).close()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(BatchImportConsoleApplication::class.java, *args)
}
