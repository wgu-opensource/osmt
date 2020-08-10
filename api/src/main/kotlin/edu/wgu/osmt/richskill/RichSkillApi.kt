package edu.wgu.osmt.richskill

import edu.wgu.osmt.auditlog.AuditLog
import edu.wgu.osmt.auditlog.AuditLogRepository
import edu.wgu.osmt.elasticsearch.EsRichSkillRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Service
@RestController
@RequestMapping("/skills")
class RichSkillApi @Autowired constructor(
    val richSkillRepository: RichSkillRepository,
    val esRichSkillRepository: EsRichSkillRepository,
    val auditLogRepository: AuditLogRepository
) {

    // TODO pagination according to spec
    @GetMapping()
    suspend fun findAll() = richSkillRepository.findAll()

    // TODO remove once testing framework is implemented
    @GetMapping("/insert-random")
    suspend fun insertRandom(@AuthenticationPrincipal user: OAuth2User?): String {
        val title = UUID.randomUUID().toString()
        val result = richSkillRepository.insert(
            RichSkillDescriptor.create(
                title,
                "a randomly inserted skill"
            )
        )
        val auditLogResult = auditLogRepository.insert(AuditLog.fromRichSkillDescriptorInsert(result, user!!))
        val updateResult = richSkillRepository.update(RsdUpdateObject(result.id!!, "updated title", null), user)
        esRichSkillRepository.save(result)
        return "<html>" +
                "<body>" +
                "<p>inserted ${updateResult.toString()}</p>" +
                "<p><a href=\"/skills\">View all Rich Skills</a></p>" +
                "</body>" +
                "</html>"
    }
}
