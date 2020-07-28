package edu.wgu.osmt.elasticsearch

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

interface ElasticsearchService{
    fun findAll(): Unit
}

// TODO this should rely on Spring Data Repositories and the domain model(s)
@Service
@Primary
class ElasticsearchServiceImpl @Autowired constructor(@Qualifier("elasticSearchClient") val esClient: RestHighLevelClient): ElasticsearchService{

    override fun findAll(): Unit{
        val searchSourceBuilder = SearchSourceBuilder()
        val searchRequest = SearchRequest().indices("richskilldescriptor").source(searchSourceBuilder)
        val response  = esClient.search(searchRequest, RequestOptions.DEFAULT)
        println(response.toString())
    }
}
