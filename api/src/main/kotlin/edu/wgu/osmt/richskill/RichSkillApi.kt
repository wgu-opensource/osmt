package edu.wgu.osmt.richskill

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Service
@RestController
@RequestMapping("/rich-skill")
class RichSkillApi @Autowired constructor(val richSkillRepository: RichSkillRepository){


    @GetMapping()
    suspend fun findAll() = richSkillRepository.findAll()

    // TODO remove once testing framework is implemented
    @GetMapping("/insert-random")
    suspend fun insertRandom(): String{
        val title = UUID.randomUUID().toString()
        val result = richSkillRepository.insert(RichSkillDescriptor(title, "a randomly inserted skill", null))
        return "<html>" +
                "<body>" +
                "<p>inserted ${result.toString()}</p>" +
                "<p><a href=\"/rich-skill\">View all Rich Skills</a></p>" +
                "</body>" +
                "</html>"
    }
}
