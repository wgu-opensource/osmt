package edu.wgu.osmt.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class AppConfig {

    @Autowired
    lateinit var environment: Environment

    @Autowired
    lateinit var dbConfig: DbConfig
}
