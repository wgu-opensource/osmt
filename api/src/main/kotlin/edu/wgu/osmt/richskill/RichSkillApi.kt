package edu.wgu.osmt.richskill

import com.fasterxml.jackson.annotation.JsonView
import edu.wgu.osmt.api.FormValidationException
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.keyword.KeywordDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/api/skills")
class RichSkillApi @Autowired constructor(
    val richSkillRepository: RichSkillRepository,
    val appConfig: AppConfig
    //val esRichSkillRepository: EsRichSkillRepository,
) {
    val keywordDao = KeywordDao.Companion

    // TODO pagination according to spec
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allSkills(request: HttpServletRequest): List<RichSkillDTO> {
        return richSkillRepository.findAll().map {
            RichSkillDTO(it, appConfig.baseUrl)
        }
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun createSkills(@RequestBody skillUpdateDescriptors: List<SkillUpdateDescriptor>,
                     @AuthenticationPrincipal user: OAuth2User?): List<RichSkillDTO>
    {
        // pre validate all rows
        val allErrors = skillUpdateDescriptors.mapIndexed { i, descriptor ->
            descriptor.validateForCreation(i)
        }.filterNotNull().flatten()
        if (allErrors.isNotEmpty()) {
            throw FormValidationException("Invalid SkillUpdateDescriptor", allErrors)
        }

        // create records
        val newSkills = skillUpdateDescriptors.map { update ->
            richSkillRepository.createFromUpdateObject(update.asRsdUpdateObject(), user)
        }
        return newSkills.filterNotNull().map { RichSkillDTO(it, appConfig.baseUrl) }
    }

    @GetMapping("/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @JsonView(RichSkillView.PublicDetailView::class)
    @ResponseBody
    fun byUUID(@PathVariable uuid: String): RichSkillDTO? {
        return richSkillRepository.findByUUID(uuid)?.let { RichSkillDTO(it, appConfig.baseUrl) }
    }

    @RequestMapping("/{uuid}", produces = [MediaType.TEXT_HTML_VALUE])
    @JsonView(RichSkillView.PublicDetailView::class)
    fun byUUIDHtmlView(@PathVariable uuid: String): String {
        return "forward:/skills/$uuid"
    }


}
