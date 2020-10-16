package edu.wgu.osmt.collection

import edu.wgu.osmt.api.model.ApiCollection
import edu.wgu.osmt.api.model.ApiCollectionUpdate
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.security.OAuth2Helper
import edu.wgu.osmt.security.OAuth2Helper.readableUsername
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/api/collections")
@Transactional
class CollectionController @Autowired constructor(
    val collectionRepository: CollectionRepository,
    val richSkillRepository: RichSkillRepository,
    val appConfig: AppConfig
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allCollections(request: HttpServletRequest): List<ApiCollection> {
        return collectionRepository.findAll().map {
            ApiCollection.fromDao(it, appConfig)
        }
    }

    @GetMapping("/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun byUUID(@PathVariable uuid: String): ApiCollection? {
        return collectionRepository.findByUUID(uuid)?.let {
            ApiCollection.fromDao(it, appConfig)
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @RequestMapping("/{uuid}", produces = [MediaType.TEXT_HTML_VALUE])
    fun byUUIDHtmlView(@PathVariable uuid: String): String {
        return "forward:/collections/$uuid"
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createCollections(@RequestBody apiCollectionUpdates: List<ApiCollectionUpdate>,
                          @AuthenticationPrincipal user: Jwt?): List<ApiCollection>
    {
        return collectionRepository.createFromApi(apiCollectionUpdates, richSkillRepository, OAuth2Helper.readableUsername(user)).map {
            ApiCollection.fromDao(it, appConfig)
        }

    }

    @PostMapping("/{uuid}/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateCollection(@PathVariable uuid: String,
                         @RequestBody apiUpdate: ApiCollectionUpdate,
                         @AuthenticationPrincipal user: Jwt?): ApiCollection
    {
        val existing = collectionRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val updated = collectionRepository.updateFromApi(existing.id.value, apiUpdate, richSkillRepository, readableUsername(user))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiCollection.fromDao(updated, appConfig)
    }
}
