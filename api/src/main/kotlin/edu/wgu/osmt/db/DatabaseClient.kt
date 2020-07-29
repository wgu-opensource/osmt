package edu.wgu.osmt.db

import edu.wgu.osmt.config.DbConfig
import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

abstract class DatabaseClient {
    abstract val db: Database
}

@Service
class DatabaseClientImpl @Autowired constructor(dbConfig: DbConfig) : DatabaseClient() {
    override val db: Database = Database.connect(
        url = dbConfig.url,
        driver = "com.mysql.cj.jdbc.Driver", user = dbConfig.username, password = dbConfig.password
    )
}
