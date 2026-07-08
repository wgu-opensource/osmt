package edu.wgu.osmt.db

import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
@Profile("migrate")
class MigrateCommand(
    private val applicationContext: ApplicationContext,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        // Flyway already ran during DataSource init. We just exit with success.
        (applicationContext as ConfigurableApplicationContext).close()
        exitProcess(0)
    }
}
