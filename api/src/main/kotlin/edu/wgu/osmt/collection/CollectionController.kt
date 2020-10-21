package edu.wgu.osmt.collection

import edu.wgu.osmt.HasAllPaginated
import edu.wgu.osmt.api.model.ApiCollection
import edu.wgu.osmt.api.model.ApiCollectionUpdate
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.elasticsearch.*
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.security.OAuth2Helper
import edu.wgu.osmt.security.OAuth2Helper.readableUsername
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder

@Controller
@Transactional
class CollectionController @Autowired constructor(
    val collectionRepository: CollectionRepository,
    val richSkillRepository: RichSkillRepository,
    override val elasticRepository: EsCollectionRepository,
    val appConfig: AppConfig
): HasAllPaginated<CollectionDoc> {

    override val allPaginatedPath: String = COLLECTIONS_PATH

    @GetMapping(COLLECTIONS_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    override fun allPaginated(
        uriComponentsBuilder: UriComponentsBuilder,
        size: Int,
        from: Int,
        status: Array<String>,
        sort: String
    ): HttpEntity<List<CollectionDoc>> {
        return super.allPaginated(uriComponentsBuilder, size, from, status, sort)
    }

    @GetMapping(DETAIL_COLLECTION_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun byUUID(@PathVariable uuid: String): ApiCollection? {
        return collectionRepository.findByUUID(uuid)?.let {
            ApiCollection.fromDao(it, appConfig)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping(DETAIL_COLLECTION_PATH, produces = [MediaType.TEXT_HTML_VALUE])
    fun byUUIDHtmlView(@PathVariable uuid: String): String {
        return "forward:/collections/$uuid"
    }

    @PostMapping(COLLECTIONS_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createCollections(
        @RequestBody apiCollectionUpdates: List<ApiCollectionUpdate>,
        @AuthenticationPrincipal user: Jwt?
    ): List<ApiCollection> {
        return collectionRepository.createFromApi(
            apiCollectionUpdates,
            richSkillRepository,
            OAuth2Helper.readableUsername(user)
        ).map {
            ApiCollection.fromDao(it, appConfig)
        }

    }

    @PostMapping(DETAIL_COLLECTION_UPDATE_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateCollection(
        @PathVariable uuid: String,
        @RequestBody apiUpdate: ApiCollectionUpdate,
        @AuthenticationPrincipal user: Jwt?
    ): ApiCollection {
        val existing = collectionRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val updated = collectionRepository.updateFromApi(
            existing.id.value,
            apiUpdate,
            richSkillRepository,
            readableUsername(user)
        )
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiCollection.fromDao(updated, appConfig)
    }

    companion object {
        const val COLLECTIONS_PATH = "/api/collections"
        const val DETAIL_COLLECTION_PATH = "${COLLECTIONS_PATH}/{uuid}"
        const val DETAIL_COLLECTION_UPDATE_PATH = "${DETAIL_COLLECTION_PATH}/update"
    }
}
