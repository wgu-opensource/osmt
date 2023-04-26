package edu.wgu.osmt.elasticsearch

import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.AuthScope.ANY
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
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
    fun elasticSearchClient(): RestHighLevelClient {
        val login = esConfig.login
        val password = esConfig.password

        return when (StringUtils.isEmpty(login) && StringUtils.isEmpty(password) ) {
            true  ->  RestHighLevelClient(RestClient.builder(HttpHost.create(esConfig.uri)))
            false -> createRestClient(login, password)
        }
    }

    fun createRestClient(login: String, password: String): RestHighLevelClient {
        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(ANY, UsernamePasswordCredentials(login, password) )

        val host_port = esConfig.uri.split(":").toTypedArray()
        return RestHighLevelClient(
            RestClient.builder(HttpHost(host_port[0], host_port[1].toInt(), "http")).setHttpClientConfigCallback {
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            })
    }

    @Bean
    fun elasticsearchTemplate(): ElasticsearchRestTemplate {
        return ElasticsearchRestTemplate(elasticSearchClient())
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
