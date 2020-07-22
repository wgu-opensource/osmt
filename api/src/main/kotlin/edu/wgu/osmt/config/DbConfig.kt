package edu.wgu.osmt.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service

@Service
@ConfigurationProperties(prefix = "db")
class DbConfig{
    lateinit var host: String

    lateinit var port: String

    lateinit var username: String

    lateinit var password: String

    lateinit var name: String

    lateinit var url: String
}
