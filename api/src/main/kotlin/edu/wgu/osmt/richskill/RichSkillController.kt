package edu.wgu.osmt.richskill

import edu.wgu.osmt.api.model.ApiSkill
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.keyword.KeywordDao
import edu.wgu.osmt.security.OAuth2Helper.readableUsername
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/api/skills")
@Transactional
class RichSkillController @Autowired constructor(
    val richSkillRepository: RichSkillRepository,
    val appConfig: AppConfig
    //val esRichSkillRepository: EsRichSkillRepository,
) {
    val keywordDao = KeywordDao.Companion

    // TODO pagination according to spec
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allSkills(request: HttpServletRequest): List<ApiSkill>  {
        return richSkillRepository.findAll().map {
            ApiSkill(it.toModel(), appConfig.baseUrl)
        }
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createSkills(@RequestBody apiSkillUpdates: List<ApiSkillUpdate>,
                     @AuthenticationPrincipal user: OAuth2User?): List<ApiSkill>
    {
        return richSkillRepository.createFromApi(apiSkillUpdates, readableUsername(user)).map {
            ApiSkill(it.toModel(), appConfig.baseUrl)
        }
    }

    @GetMapping("/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun byUUID(@PathVariable uuid: String): ApiSkill? {
        return richSkillRepository.findByUUID(uuid)?.let { ApiSkill(it.toModel(), appConfig.baseUrl) }
    }

    @PostMapping("/{uuid}/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun updateSkill(@PathVariable uuid: String,
                    @RequestBody skillUpdate: ApiSkillUpdate,
                    @AuthenticationPrincipal user: OAuth2User?): ApiSkill
    {
        val existingSkill = richSkillRepository.findByUUID(uuid)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        val updatedSkill = richSkillRepository.updateFromApi(existingSkill.id.value, skillUpdate, readableUsername(user))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)

        return ApiSkill(updatedSkill.toModel(), appConfig.baseUrl)
    }

    @RequestMapping("/{uuid}", produces = [MediaType.TEXT_HTML_VALUE])
    fun byUUIDHtmlView(@PathVariable uuid: String): String {
        return "forward:/skills/$uuid"
    }


}
