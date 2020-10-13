package edu.wgu.osmt.elasticsearch

import edu.wgu.osmt.BatchImportConsoleApplication
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.TestObjectHelpers.keywordGenerator
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
class EsCrudTests : SpringTest(), HasDatabaseReset {

    @Autowired
    lateinit var esRichSkillRepository: EsRichSkillRepository

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var keywordRepository: KeywordRepository

    @Test
    fun `should insert a rich skill into elastic search`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()

        val keywords = keywordGenerator(10, KeywordTypeEnum.Keyword)
        val employers = keywordGenerator(10, KeywordTypeEnum.Employer)

        val allKeywords = keywords + employers

        val allKeywordDao = allKeywords.mapNotNull { keywordRepository.create(it.type, it.value) }

        val rsd = richSkillRepository.create(
            RsdUpdateObject(
                name = name,
                statement = statement,
                keywords = ListFieldUpdate(add = allKeywordDao)
            ), BatchImportConsoleApplication.user
        )?.toModel()!!

        val elasticModel = esRichSkillRepository.findByUuid(rsd.uuid, Pageable.unpaged())
        val esRetrieved = elasticModel.content.first()

        assertThat(esRetrieved.uuid).isEqualTo(rsd.uuid)
    }

    @Test
    fun `should update a rich skill in elastic search`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()

        val keywords = keywordGenerator(10, KeywordTypeEnum.Keyword)
        val employers = keywordGenerator(10, KeywordTypeEnum.Employer)

        val allKeywords = keywords + employers

        val allKeywordDao = allKeywords.mapNotNull { keywordRepository.create(it.type, it.value) }

        val rsd = richSkillRepository.create(
            RsdUpdateObject(
                name = name,
                statement = statement,
                keywords = ListFieldUpdate(add = allKeywordDao)
            ), BatchImportConsoleApplication.user
        )?.toModel()!!

        val newName = UUID.randomUUID().toString()
        val newStatement = UUID.randomUUID().toString()

        val keywordToRemove = allKeywordDao.get(0)
        val keywordToAdd = keywordGenerator(1, KeywordTypeEnum.Keyword)[0]
        val keywordToAddDao = keywordRepository.create(keywordToAdd.type, keywordToAdd.value)

        val updateObject = RsdUpdateObject(
            id = rsd.id,
            name = newName,
            statement = newStatement,
            keywords = ListFieldUpdate(add = listOf(keywordToAddDao!!), remove = listOf(keywordToRemove))
        )
        val updateResult = richSkillRepository.update(updateObject, "test-user")

        val elasticModel = esRichSkillRepository.findByUuid(updateResult?.uuid.toString(), Pageable.unpaged())
        val esRetrieved = elasticModel.content.first()

        assertThat(esRetrieved.uuid).isEqualTo(rsd.uuid)
        assertThat(esRetrieved.searchingKeywords).contains(keywordToAdd.value)
        assertThat(esRetrieved.searchingKeywords).doesNotContain(keywordToRemove.toModel().value)
        assertThat(esRetrieved.name).isEqualTo(newName)
        assertThat(esRetrieved.statement).isEqualTo(newStatement)
    }

    @Test
    fun `Should be able to query skills across multiple fields`() {
        val keywords = TestObjectHelpers.keywordGenerator(10, KeywordTypeEnum.Keyword)

        val category = keywordRepository.create(KeywordTypeEnum.Category, "test category")!!

        val keywordsDao = keywords.mapNotNull { keywordRepository.create(it.type, it.value) }
        val rsd1 = richSkillRepository.create(
            RsdUpdateObject(
                name = "The best skill",
                statement = "a statement",
                category = NullableFieldUpdate(category),
                keywords = ListFieldUpdate(add = keywordsDao)
            ),
            testUser
        )
        val rsd2 = richSkillRepository.create(
            RsdUpdateObject(
                name = "A skill",
                statement = "statement"
            ),
            testUser
        )

        val searchByAKeyword = esRichSkillRepository.searchBySkillProperties(keywords.get(0).value!!)
        val searchByACategory = esRichSkillRepository.searchBySkillProperties(category.value!!)

        if (rsd1 != null) {
            assertThat(searchByAKeyword.content.get(0).uuid).isEqualTo(rsd1.uuid)
            assertThat(searchByACategory.content.get(0).uuid).isEqualTo(rsd1.uuid)
        } else {
            Assertions.fail("failed to creat a rich skill")
        }
        assert(true)
    }
}
