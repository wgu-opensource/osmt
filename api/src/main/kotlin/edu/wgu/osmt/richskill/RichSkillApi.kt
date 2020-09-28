package edu.wgu.osmt.richskill

import com.fasterxml.jackson.annotation.JsonView
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.keyword.KeywordDao
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/api/skills")
@Transactional
class RichSkillApi @Autowired constructor(
    val richSkillRepository: RichSkillRepository,
    val appConfig: AppConfig
    //val esRichSkillRepository: EsRichSkillRepository,
) {
    val keywordDao = KeywordDao.Companion

    // TODO pagination according to spec
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun allSkills(request: HttpServletRequest): List<RichSkillDTO>  {
        return richSkillRepository.findAll().map {
            RichSkillDTO(it.toModel(), appConfig.baseUrl)
        }
    }

    @GetMapping("/{uuid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @JsonView(RichSkillView.PublicDetailView::class)
    @ResponseBody
    fun byUUID(@PathVariable uuid: String): RichSkillDTO? {
        return richSkillRepository.findByUUID(uuid)?.let { RichSkillDTO(it.toModel(), appConfig.baseUrl) }
    }

    @RequestMapping("/{uuid}", produces = [MediaType.TEXT_HTML_VALUE])
    @JsonView(RichSkillView.PublicDetailView::class)
    fun byUUIDHtmlView(@PathVariable uuid: String): String {
        return "forward:/skills/$uuid"
    }


}
