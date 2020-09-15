package edu.wgu.osmt.db

import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseClient @Autowired constructor(val dbConfig: DbConfig) {

    @Bean
    fun db(): Database = Database.connect(
        url = dbConfig.composedUrl,
        driver = "com.mysql.cj.jdbc.Driver", user = "", password = ""
    )
}
