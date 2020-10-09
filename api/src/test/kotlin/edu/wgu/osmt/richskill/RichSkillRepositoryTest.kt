package edu.wgu.osmt.richskill

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.api.model.ApiNamedReference
import edu.wgu.osmt.api.model.ApiReferenceListUpdate
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.collection.Collection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
class RichSkillRepositoryTest: SpringTest(), BaseDockerizedTest {

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var keywordRepository: KeywordRepository

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
        val collectionCount = 3
        val collections = ApiStringListUpdate(
            add=(1..collectionCount).toList().map { UUID.randomUUID().toString() }
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
            occupations=occupations,
            collections=collections
        )
    }

    fun assertThatKeywordMatchesNamedReference(keyword: Keyword?, namedReference: ApiNamedReference?) {
        assertThat(keyword).isNotNull
        assertThat(keyword?.uri).isEqualTo(namedReference?.id)
        assertThat(keyword?.value).isEqualTo(namedReference?.name)
    }

    fun assertThatKeywordsMatchStringList(keywords: List<Keyword>, stringList: ApiStringListUpdate) {
        stringList.add?.forEach { str ->
            val found = keywords.find { it.value == str }
            assertThat(found).isNotNull
        }

        stringList.remove?.forEach { str ->
            val found = keywords.find { it.value == str }
            assertThat(found).isNull()
        }
    }

    fun assertThatCollectionsMatchStringList(collections: List<Collection>, stringList: ApiStringListUpdate) {
        stringList.add?.forEach { str ->
            assertThat(collections.find { it.name == str }).isNotNull
        }
        stringList.remove?.forEach { str ->
            assertThat(collections.find { it.name == str }).isNull()
        }
    }

    fun assertThatJobCodesMatchStringList(jobCodes: List<JobCode>, stringList: ApiStringListUpdate) {
        stringList.add?.forEach { str ->
            val found = jobCodes.find { it.code == str }
            assertThat(found).isNotNull
        }

        stringList.remove?.forEach { str ->
            val found = jobCodes.find { it.code == str }
            assertThat(found).isNull()
        }
    }

    fun assertThatKeywordsMatchReferenceList(keywords: List<Keyword>, referenceList: ApiReferenceListUpdate) {
        referenceList.add?.forEach { namedReference ->
            val found = keywords.find { it.value == namedReference.name && it.uri == namedReference.id }
            assertThat(found).isNotNull
            assertThatKeywordMatchesNamedReference(found, namedReference)
        }

        referenceList.remove?.forEach { namedReference ->
            val found = keywords.find { it.value == namedReference.name && it.uri == namedReference.id }
            assertThat(found).isNull()
        }
    }

    fun assertThatRichSkillMatchesApiSkillUpdate(skill: RichSkillDescriptor, apiObj: ApiSkillUpdate) {
        assertThat(skill.name).isEqualTo(apiObj.skillName)
        assertThat(skill.statement).isEqualTo(apiObj.skillStatement)

        assertThat(skill.category).isNotNull
        assertThat(skill.category?.value).isEqualTo(apiObj.category)
        assertThat(skill.category?.uri).isNull()

        assertThatKeywordMatchesNamedReference(skill.author, apiObj.author)

        assertThatKeywordsMatchStringList(skill.searchingKeywords, apiObj.keywords!!)

        assertThatKeywordsMatchReferenceList(skill.certifications, apiObj.certifications!!)
        assertThatKeywordsMatchReferenceList(skill.standards, apiObj.standards!!)
        assertThatKeywordsMatchReferenceList(skill.alignments, apiObj.alignments!!)
        assertThatKeywordsMatchReferenceList(skill.employers, apiObj.employers!!)

        assertThatJobCodesMatchStringList(skill.jobCodes, apiObj.occupations!!)

        assertThatCollectionsMatchStringList(skill.collections, apiObj.collections!!)

        assertThat(skill.publishStatus()).isEqualTo(apiObj.publishStatus)
    }

    @Test
    fun `should update an existing skill from an ApiSkillUpdate object`() {
        val originalSkillUpdate = random_skill_update()
        val originalSkillDao = richSkillRepository.createFromApi(listOf(originalSkillUpdate), userString).first()

        var newSkillUpdate = random_skill_update()
        newSkillUpdate = newSkillUpdate.copy(
            keywords=newSkillUpdate.keywords?.copy(remove=originalSkillUpdate.keywords?.add),
            certifications=newSkillUpdate.certifications?.copy(remove=originalSkillUpdate.certifications?.add),
            standards=newSkillUpdate.standards?.copy(remove=originalSkillUpdate.standards?.add),
            alignments=newSkillUpdate.alignments?.copy(remove=originalSkillUpdate.alignments?.add),
            employers=newSkillUpdate.employers?.copy(remove=originalSkillUpdate.employers?.add),
            occupations=newSkillUpdate.occupations?.copy(remove=originalSkillUpdate.occupations?.add),
            collections=newSkillUpdate.collections?.copy(remove=originalSkillUpdate.collections?.add)
        )

        val updatedDao = richSkillRepository.updateFromApi(originalSkillDao.id.value, newSkillUpdate, userString)
        assertThat(updatedDao).isNotNull
        val updated = updatedDao!!.toModel()
        assertThatRichSkillMatchesApiSkillUpdate(updated, newSkillUpdate)
    }

    @Test
    fun `should create skills from ApiSkillUpdate objects`() {
        val skillCount = 1
        val skillUpdates = (1..skillCount).toList().map { random_skill_update() }

        val results: List<RichSkillDescriptorDao> = richSkillRepository.createFromApi(skillUpdates, userString)

        results.forEachIndexed { i, skillDao ->
            val skill = skillDao.toModel()
            val apiObj = skillUpdates[i]
            assertThatRichSkillMatchesApiSkillUpdate(skill, apiObj)
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
        val created = richSkillRepository.create(RsdUpdateObject(
            name = name,
            statement = statement,
            author = null,
            category = null
        ), "test")
        assertThat(created).isNotNull
        assertThat(created!!.name).isEqualTo(name)

        val newName = UUID.randomUUID().toString()
        val newStatement = UUID.randomUUID().toString()

        val updated = richSkillRepository.update(RsdUpdateObject(
            id=created.id.value,
            name=newName,
            statement=newStatement
        ), "test")
        val retrieved  = richSkillRepository.findById(created.id.value)

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

    @Test
    fun `should not error, or create a duplicate when adding an existing keyword`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()
        val keywordName = UUID.randomUUID().toString()
        val keyword = keywordRepository.findOrCreate(KeywordTypeEnum.Keyword, value=keywordName)!!
        val otherKeyword = keywordRepository.findOrCreate(KeywordTypeEnum.Keyword, value=UUID.randomUUID().toString())!!
        val createObject = RsdUpdateObject(
            name = name,
            statement = statement,
            keywords = ListFieldUpdate(add=listOf(keyword))
        )

        val created = richSkillRepository.create(createObject, userString)
        assertThat(created).isNotNull

        val updateObject = RsdUpdateObject(
            id = created?.id?.value,
            keywords = ListFieldUpdate(add=listOf(keyword), remove=listOf(otherKeyword))
        )
        val updated = richSkillRepository.update(updateObject, userString)
        assertThat(updated).isNotNull
        val skill = updated!!.toModel()
        assertThat(skill.searchingKeywords.size).isEqualTo(1)
        assertThat(skill.searchingKeywords[0].value == keywordName)
    }

    @Test
    fun `should be able to set an existing category to null on a skill via the API`() {
        val name = UUID.randomUUID().toString()
        val statement = UUID.randomUUID().toString()
        val categoryName = UUID.randomUUID().toString()
        val category = keywordRepository.findOrCreate(KeywordTypeEnum.Category, categoryName)!!

        val createObject = RsdUpdateObject(
            name = name,
            statement = statement,
            category = NullableFieldUpdate(category)
        )
        val created = richSkillRepository.create(createObject, userString)?.toModel()
        assertThat(created).isNotNull
        assertThat(created?.category?.value).isEqualTo(categoryName)

        // doesnt clear category if not specified in update object
        val newName = UUID.randomUUID().toString()
        val apiUpdate = ApiSkillUpdate(
            skillName=newName
        )
        var apiUpdated = richSkillRepository.updateFromApi(created!!.id!!, apiUpdate, userString)?.toModel()
        assertThat(apiUpdated).isNotNull
        assertThat(apiUpdated?.category?.value).isEqualTo(categoryName)

        // pass category as empty string to nullify it
        val apiUpdateBlank = ApiSkillUpdate(
            category=""
        )
        apiUpdated = richSkillRepository.updateFromApi(created!!.id!!, apiUpdateBlank, userString)?.toModel()
        assertThat(apiUpdated).isNotNull
        assertThat(apiUpdated?.category).isNull()
    }
}
