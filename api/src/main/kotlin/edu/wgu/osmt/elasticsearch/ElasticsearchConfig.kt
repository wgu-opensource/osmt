package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.config.EsConfig
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients

@Configuration
class ElasticsearchConfig {
    @Autowired
    lateinit var esConfig: EsConfig

    @Override
    @Bean
    fun elasticSearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder().connectedTo("${esConfig.host}:${esConfig.port}").build()
        return RestClients.create(clientConfiguration).rest()
    }
}
