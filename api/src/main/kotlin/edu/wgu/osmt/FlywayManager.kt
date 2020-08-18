package edu.wgu.osmt

import edu.wgu.osmt.config.DbConfig
import edu.wgu.osmt.config.FlyConfig
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class FlywayManager @Autowired constructor(dbConfig: DbConfig, flyConfig: FlyConfig) {

    val flyway = Flyway.configure()
        .dataSource(dbConfig.composedUri, "", "")
        .createSchemas(flyConfig.enabled)
        .baselineOnMigrate(flyConfig.baselineOnMigrate)
        .load()
}
