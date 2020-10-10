package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.collection.CollectionDoc
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.streams.toList

// TODO align with API spec once available
@Controller
@RequestMapping("/api/search")
@Transactional
class SearchController @Autowired constructor(
    val elasticsearchService: SearchService
) {

    @GetMapping("/collection_by_name", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findCollectionsByName(@RequestParam q: String): List<CollectionDoc> {
        val ids = elasticsearchService.esCollectionRepository.findByName(q, Pageable.unpaged()).get().toList()
        return ids
    }

    @GetMapping("/collection_by_name_and_skill_properties", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun findCollectionsByNameAndSkillProperties(@RequestParam q: String): List<CollectionDoc> =
        elasticsearchService.findCollectionsByNameAndSkillProperties(q).content
}
