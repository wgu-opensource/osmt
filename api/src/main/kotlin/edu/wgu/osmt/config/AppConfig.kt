package edu.wgu.osmt.config

import edu.wgu.osmt.db.DbConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class AppConfig {

    @Autowired
    lateinit var environment: Environment

    @Autowired
    lateinit var dbConfig: DbConfig
}
