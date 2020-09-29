package edu.wgu.osmt.richskill

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiReferenceListUpdate
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.keyword.Keyword
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@ActiveProfiles("test,apiserver")
@ConfigurationPropertiesScan("edu.wgu.osmt.config")
@ContextConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class RichSkillRepositoryTest: BaseDockerizedTest() {

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    val userString = "unittestuser"

    fun random_reference(includeName: Boolean = true, includeUri: Boolean = true): ApiNamedReference {
        val name = if (includeName) UUID.randomUUID().toString() else null
        val uri = if (includeUri) UUID.randomUUID().toString() else null
        return ApiNamedReference(id=uri, name=name)
    }

    fun random_skill_update(): ApiSkillUpdate {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()
        val status = PublishStatus.Published
        val categoryName = UUID.randomUUID().toString()
        val author = random_reference(includeName=true, includeUri=false)

        val keywordCount = 3
        val keywords = ApiStringListUpdate(
            add=(1..keywordCount).toList().map { UUID.randomUUID().toString() }
        )
        val certificationCount = 3
        val certifications = ApiReferenceListUpdate(
            add=(1..certificationCount).toList().map { random_reference(includeName = false, includeUri = true) }
        )
        val standardCount = 3
        val standards = ApiReferenceListUpdate(
            add=(1..standardCount).toList().map { random_reference(includeName = true, includeUri = true) }
        )
        val alignmentCount = 3
        val alignments = ApiReferenceListUpdate(
            add=(1..alignmentCount).toList().map { random_reference(includeName = false, includeUri = true) }
        )
        val employerCount = 3
        val employers = ApiReferenceListUpdate(
            add=(1..employerCount).toList().map { random_reference(includeName = true, includeUri = false) }
        )
        val occupationCount = 3
        val occupations = ApiStringListUpdate(
            add=(1..occupationCount).toList().map { UUID.randomUUID().toString() }
        )

        return ApiSkillUpdate(
            skillName=name,
            skillStatement=statement,
            publishStatus=status,
            category=categoryName,
            author=author,
            keywords=keywords,
            certifications=certifications,
            standards=standards,
            alignments=alignments,
            employers=employers,
            occupations=occupations
        )
    }

    fun assertThatKeywordMatchesNamedReference(keyword: Keyword?, namedReference: ApiNamedReference?) {
        assertThat(keyword).isNotNull
        assertThat(keyword?.uri).isEqualTo(namedReference?.id)
        assertThat(keyword?.value).isEqualTo(namedReference?.name)
    }

    fun assertThatKeywordsMatchReferenceList(keywords: List<Keyword>, referenceList: ApiReferenceListUpdate) {
        referenceList.add?.forEach { namedReference ->
            val found = keywords.find { it.value == namedReference.name && it.uri == namedReference.id }
            assertThat(found).isNotNull
            assertThatKeywordMatchesNamedReference(found, namedReference)
        }
    }

    @Test
    fun `should create skills from ApiSkillUpdate objects`() {
        val skillCount = 1
        val skillUpdates = (1..skillCount).toList().map { random_skill_update() }

        val results: List<RichSkillDescriptorDao> = richSkillRepository.createFromApi(skillUpdates, userString)

        results.forEachIndexed { i, skillDao ->
            val skill = skillDao.toModel()
            val apiObj = skillUpdates[i]
            assertThat(skill.name).isEqualTo(apiObj.skillName)
            assertThat(skill.statement).isEqualTo(apiObj.skillStatement)

            assertThat(skill.category).isNotNull
            assertThat(skill.category?.value).isEqualTo(apiObj.category)
            assertThat(skill.category?.uri).isNull()

            assertThatKeywordMatchesNamedReference(skill.author, apiObj.author)

            apiObj.keywords!!.add!!.forEach { kwString ->
                val foundKeyword = skill.searchingKeywords.find { it.value == kwString }
                assertThat(foundKeyword).isNotNull
            }

            assertThatKeywordsMatchReferenceList(skill.certifications, apiObj.certifications!!)
            assertThatKeywordsMatchReferenceList(skill.standards, apiObj.standards!!)
            assertThatKeywordsMatchReferenceList(skill.alignments, apiObj.alignments!!)
            assertThatKeywordsMatchReferenceList(skill.employers, apiObj.employers!!)

            apiObj.occupations!!.add!!.forEach { jobCode ->
                val found = skill.jobCodes.find { it.code == jobCode }
                assertThat(found).isNotNull
            }

            assertThat(skill.publishStatus()).isEqualTo(apiObj.publishStatus)
        }
    }

    @Test
    fun `should not create a rich skill without a non-blank name and statement`(){
        assertThat(richSkillRepository.create(RsdUpdateObject(), userString)).isNull()
        assertThat(richSkillRepository.create(RsdUpdateObject(name="name"), userString)).isNull()
        assertThat(richSkillRepository.create(RsdUpdateObject(statement="statement"), userString)).isNull()
        assertThat(richSkillRepository.create(RsdUpdateObject(name=" ",statement=" "), userString)).isNull()
    }

    @Test
    fun `should update an existing skill`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()
        val created = richSkillRepository.create(name = name, statement = statement, author = null, user = "test", category = null).toModel()
        assertThat(created.name).isEqualTo(name)

        val newName = UUID.randomUUID().toString()
        val newStatement = UUID.randomUUID().toString()

        val updated = richSkillRepository.update(RsdUpdateObject(
            id=created.id!!,
            name=newName,
            statement=newStatement
        ), "test")
        val retrieved  = richSkillRepository.findById(created.id!!)

        assertThat(retrieved?.name).isEqualTo(newName)
        assertThat(updated?.name).isEqualTo(newName)

    }

    @Test
    fun `should insert and retrieve a rich skill to the database`(){
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()
        val updateObject = RsdUpdateObject(name = name, statement = statement)

        val created = richSkillRepository.create(updateObject, userString)
        assertThat(created).isNotNull

        val retrieved  = richSkillRepository.findById(created!!.id.value)
        assertThat(created.id.value).isEqualTo(retrieved?.id?.value)
        assertThat(retrieved?.name).isEqualTo(name)
        assertThat(retrieved?.statement).isEqualTo(statement)
    }
}
