package edu.wgu.osmt.collection

import edu.wgu.osmt.api.model.ApiCollection
import edu.wgu.osmt.api.model.ApiSkill
import edu.wgu.osmt.config.AppConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.awt.PageAttributes
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/api/collections")
@Transactional
class CollectionController @Autowired constructor(
    val collectionRepository: CollectionRepository,
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


}
