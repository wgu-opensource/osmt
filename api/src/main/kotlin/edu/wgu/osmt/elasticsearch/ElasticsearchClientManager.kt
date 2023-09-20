package edu.wgu.osmt.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.ElasticsearchTransport
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@Configuration
@EnableElasticsearchRepositories(*["edu.wgu.osmt.elasticsearch"])
class ElasticsearchClientManager {
    @Autowired
    lateinit var esConfig: EsConfig

    @Override
    @Bean
    fun elasticSearchClient(): ElasticsearchClient {
        val builder = RestClient.builder(HttpHost("localhost", 9200, "http"))
        //val httpClientConfigCallback: HttpClientConfigCallback = HttpClientConfigCallbackImpl()
        //builder.setHttpClientConfigCallback(httpClientConfigCallback)
        val restClient = builder.build()
        val transport: ElasticsearchTransport = RestClientTransport(restClient, JacksonJsonpMapper())
        return ElasticsearchClient(transport)
    }

    @Bean
    fun elasticsearchTemplate(): ElasticsearchTemplate {
        return ElasticsearchTemplate(elasticSearchClient())
    }

    @Bean
    @Override
    fun elasticSearchCustomConversions(): ElasticsearchCustomConversions{
        return ElasticsearchCustomConversions(
            listOf(LocalDateTimeToString(), StringToLocalDatetime(),
                UuidToString(), StringToUuid()
            )
        )
    }

    @WritingConverter
    internal class LocalDateTimeToString : Converter<LocalDateTime, String> {
        override fun convert(source: LocalDateTime): String {
            return source.format(DateTimeFormatter.ISO_DATE_TIME)
        }
    }

    @ReadingConverter
    internal class StringToLocalDatetime: Converter<String, LocalDateTime>{
        override fun convert(source: String): LocalDateTime {
            return LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }

    @WritingConverter
    internal class UuidToString : Converter<UUID, String> {
        override fun convert(source: UUID): String {
            return source.toString()
        }
    }

    @ReadingConverter
    internal class StringToUuid: Converter<String, UUID>{
        override fun convert(source: String): UUID {
            return UUID.fromString(source)
        }
    }
}
