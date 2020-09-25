package edu.wgu.osmt.db

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DatabaseClient @Autowired constructor(val dbConfig: DbConfig) {

    @Bean
    fun db(): Database = Database.connect(
        url = dbConfig.composedUrl,
        driver = "com.mysql.cj.jdbc.Driver", user = "", password = ""
    )

    @Bean
    fun transactionManager(dataSource: DataSource): SpringTransactionManager {
        return SpringTransactionManager(dataSource)
    }
}
