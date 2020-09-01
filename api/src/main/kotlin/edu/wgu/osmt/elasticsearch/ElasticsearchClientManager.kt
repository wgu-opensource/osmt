package edu.wgu.osmt.elasticsearch

import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients

@Configuration
class ElasticsearchClientManager {
    @Autowired
    lateinit var esConfig: EsConfig

    @Override
    @Bean
    fun elasticSearchClient(): RestHighLevelClient {
        val clientConfiguration = ClientConfiguration.builder().connectedTo(esConfig.uri).build()
        return RestClients.create(clientConfiguration).rest()
    }
}
