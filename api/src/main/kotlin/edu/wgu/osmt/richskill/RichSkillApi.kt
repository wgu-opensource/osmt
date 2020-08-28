package edu.wgu.osmt.richskill

import com.fasterxml.jackson.annotation.JsonView
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.keyword.KeywordDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.*

@Controller
@RequestMapping("/api/skills")
class RichSkillApi @Autowired constructor(
    val richSkillRepository: RichSkillRepository,
    val appConfig: AppConfig
    //val esRichSkillRepository: EsRichSkillRepository,
) {
    val keywordDao = KeywordDao.Companion

    // TODO pagination according to spec
    @GetMapping()
    @ResponseBody
    fun findAll() = richSkillRepository.findAll()

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

    // TODO remove once testing framework is implemented
    @GetMapping("/insert-random")
    fun insertRandom(@AuthenticationPrincipal user: OAuth2User): String {
        val title = UUID.randomUUID().toString()
        val result = richSkillRepository.create(title, "a randomly inserted skill", "an author", user)
        val updateResult = richSkillRepository.update(
            RsdUpdateObject(
                result.id.value,
                "updated title",
                "updated description",
                "updatedAuthor"
            ), user
        )
        //esRichSkillRepository.save(result)
        return "<html>" +
                "<body>" +
                "<p>inserted ${updateResult.toString()}</p>" +
                "<p><a href=\"/skills\">View all Rich Skills</a></p>" +
                "</body>" +
                "</html>"
    }
}
