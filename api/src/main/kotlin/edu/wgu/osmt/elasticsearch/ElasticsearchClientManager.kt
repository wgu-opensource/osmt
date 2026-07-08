package edu.wgu.osmt.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
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
        val transport =
            RestClientTransport(
                createRestClient(),
                JacksonJsonpMapper(),
            )
        return ElasticsearchClient(transport)
    }

    @Bean
    @Profile("!readonly")
    fun elasticsearchTemplate(): ElasticsearchTemplate = ElasticsearchTemplate(elasticSearchClient())

    @Bean
    @Override
    fun elasticSearchCustomConversions(): ElasticsearchCustomConversions =
        ElasticsearchCustomConversions(
            listOf(
                LocalDateTimeToString(),
                StringToLocalDatetime(),
                UuidToString(),
                StringToUuid(),
            ),
        )

    @WritingConverter
    internal class LocalDateTimeToString : Converter<LocalDateTime, String> {
        override fun convert(source: LocalDateTime): String = source.format(DateTimeFormatter.ISO_DATE_TIME)
    }

    @ReadingConverter
    internal class StringToLocalDatetime : Converter<String, LocalDateTime> {
        override fun convert(source: String): LocalDateTime =
            LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @WritingConverter
    internal class UuidToString : Converter<UUID, String> {
        override fun convert(source: UUID): String = source.toString()
    }

    @ReadingConverter
    internal class StringToUuid : Converter<String, UUID> {
        override fun convert(source: String): UUID = UUID.fromString(source)
    }

    private fun createRestClient(): RestClient {
        val host = HttpHost.create(esConfig.uri)
        val restClientBuilder = RestClient.builder(host)
        val credentialsProvider = getCredentialsProvider()

        credentialsProvider?.let {
            restClientBuilder.setHttpClientConfigCallback { b ->
                b.setDefaultCredentialsProvider(it)
            }
        }
        return restClientBuilder.build()
    }

    private fun getCredentialsProvider(): CredentialsProvider? {
        if (esConfig.username.isNullOrBlank() || esConfig.password.isNullOrBlank()) {
            return null
        }

        val credentialsProvider = BasicCredentialsProvider()
        val credential = UsernamePasswordCredentials(esConfig.username, esConfig.password)
        credentialsProvider.setCredentials(AuthScope.ANY, credential)
        return credentialsProvider
    }
}
