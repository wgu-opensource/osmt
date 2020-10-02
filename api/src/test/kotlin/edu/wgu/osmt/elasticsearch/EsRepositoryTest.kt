package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.BatchImportConsoleApplication
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.*
import org.assertj.core.api.Assertions
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.streams.asSequence

class EsRepositoryTest : SpringTest(), HasDatabaseReset {

    @Autowired
    lateinit var esRichSkillRepository: EsRichSkillRepository

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var keywordRepository: KeywordRepository

    fun keywordGenerator(n: Int, type: KeywordTypeEnum): List<Keyword> {
        val keywords = (0..n).toList().map {
            val chars = "abcdefghijklmnopqrstuvwxyz"
            val word = Random().ints(10, 0, chars.length).asSequence().map(chars::get).joinToString().capitalize()
            Keyword(null, LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC), type, word, null)
        }
        return keywords
    }

    @Test
    @Transactional
    fun `should insert a rich skill into elastic search`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()

        val keywords = keywordGenerator(10, KeywordTypeEnum.Keyword)
        val employers = keywordGenerator(10, KeywordTypeEnum.Employer)

        val allKeywords = keywords + employers

        val allKeywordDao = allKeywords.map{ keywordRepository.create(it.type,it.value) }

        val rsd = richSkillRepository.create(RsdUpdateObject(
            name = name,
            statement = statement,
            keywords = ListFieldUpdate(add = allKeywordDao)
        ), BatchImportConsoleApplication.user)?.toModel()!!
        esRichSkillRepository.save(rsd)

        val elasticModel = esRichSkillRepository.findByUUID(rsd.uuid.toString(), Pageable.unpaged())
        val m = elasticModel.content.first()
        val esRetrieved = elasticModel.content.first()

        Assertions.assertThat(esRetrieved.uuid).isEqualTo(rsd.uuid)
    }
}
