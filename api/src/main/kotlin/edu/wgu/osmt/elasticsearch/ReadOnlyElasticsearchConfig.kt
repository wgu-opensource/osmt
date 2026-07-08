package edu.wgu.osmt.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.IndexOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates

/**
 * Overrides the [ElasticsearchTemplate] bean in readonly mode to
 * skip automatic index creation on startup. The writable (staff)
 * instance owns index lifecycle; the readonly instance only reads.
 *
 * The bean must be named "elasticsearchTemplate" because
 * [EnableElasticsearchRepositories] resolves by bean name.
 */
@Configuration
@Profile("readonly")
class ReadOnlyElasticsearchConfig {
    private val log =
        LoggerFactory.getLogger(ReadOnlyElasticsearchConfig::class.java)

    @Bean
    fun elasticsearchTemplate(client: ElasticsearchClient): ElasticsearchTemplate {
        log.info(
            "Readonly mode: ES index auto-creation disabled",
        )
        return object : ElasticsearchTemplate(client) {
            override fun indexOps(clazz: Class<*>): IndexOperations = SkipCreateIndexOperations(super.indexOps(clazz))

            override fun indexOps(index: IndexCoordinates): IndexOperations =
                SkipCreateIndexOperations(super.indexOps(index))
        }
    }
}

/**
 * Delegates all [IndexOperations] to [delegate] but makes
 * index-creation methods no-ops. Read operations pass through.
 */
internal class SkipCreateIndexOperations(
    private val delegate: IndexOperations,
) : IndexOperations by delegate {
    private val log =
        LoggerFactory.getLogger(SkipCreateIndexOperations::class.java)

    override fun create(): Boolean {
        log.debug("Skipping index create (readonly)")
        return true
    }

    override fun create(settings: MutableMap<String, Any>): Boolean {
        log.debug("Skipping index create with settings (readonly)")
        return true
    }

    override fun create(
        settings: MutableMap<String, Any>,
        mapping: org.springframework.data.elasticsearch.core.document.Document,
    ): Boolean {
        log.debug(
            "Skipping index create with settings+mapping (readonly)",
        )
        return true
    }

    override fun createWithMapping(): Boolean {
        log.debug("Skipping createWithMapping (readonly)")
        return true
    }

    override fun delete(): Boolean {
        log.debug("Skipping index delete (readonly)")
        return true
    }
}
