package edu.wgu.osmt.richskill

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers.apiSkillUpdateGenerator
import edu.wgu.osmt.TestObjectHelpers.assertThatKeywordMatchesNamedReference
import edu.wgu.osmt.TestObjectHelpers.namedReferenceGenerator
import edu.wgu.osmt.api.model.ApiReferenceListUpdate
import edu.wgu.osmt.api.model.ApiSearch
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
import edu.wgu.osmt.task.PublishSkillsTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
class RichSkillRepositoryTest: SpringTest(), BaseDockerizedTest, HasDatabaseReset  {

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var keywordRepository: KeywordRepository

    val userString = "unittestuser"


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

    fun assertThatCollectionsMatchStringList(collections: Set<Collection>, stringList: ApiStringListUpdate) {
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

    fun assertThatRichSkillMatchesApiSkillUpdate(rsc: RichSkillAndCollections, apiObj: ApiSkillUpdate) {
        val skill = rsc.rs

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

        assertThatCollectionsMatchStringList(rsc.collections, apiObj.collections!!)

        assertThat(skill.publishStatus()).isEqualTo(apiObj.publishStatus)
    }

    @Test
    fun `should update an existing skill from an ApiSkillUpdate object`() {
        val originalSkillUpdate = apiSkillUpdateGenerator()
        val originalSkillDao = richSkillRepository.createFromApi(listOf(originalSkillUpdate), userString).first()

        var newSkillUpdate = apiSkillUpdateGenerator()
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
        assertThatRichSkillMatchesApiSkillUpdate(RichSkillAndCollections.fromDao(updatedDao!!), newSkillUpdate)
    }

    @Test
    fun `should create skills from ApiSkillUpdate objects`() {
        val skillCount = 1
        val skillUpdates = (1..skillCount).toList().map { apiSkillUpdateGenerator() }

        val results: List<RichSkillDescriptorDao> = richSkillRepository.createFromApi(skillUpdates, userString)

        results.forEachIndexed { i, skillDao ->
            val skillAndCollections = RichSkillAndCollections.fromDao(skillDao)
            val apiObj = skillUpdates[i]
            assertThatRichSkillMatchesApiSkillUpdate(skillAndCollections, apiObj)
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
        apiUpdated = richSkillRepository.updateFromApi(created.id!!, apiUpdateBlank, userString)?.toModel()
        assertThat(apiUpdated).isNotNull
        assertThat(apiUpdated?.category).isNull()
    }

    @Test
    fun `should be able to bulk publish all pages of search results`() {
        val totalSkillCount = 10
        val toPublishCount = 7
        val searchQuery = "a different thing"
        val skillUpdates = (1..totalSkillCount-toPublishCount).toList().map { apiSkillUpdateGenerator() }
        val knownUpdates = (1..toPublishCount).toList().map { apiSkillUpdateGenerator(
            name="${searchQuery} ${UUID.randomUUID()}"
        )}

        val skillDaos = richSkillRepository.createFromApi(skillUpdates, userString)
        val knownDaos = richSkillRepository.createFromApi(knownUpdates, userString)
        assertThat(skillDaos.size + knownDaos.size).isEqualTo(totalSkillCount)

        val batchResult = richSkillRepository.changeStatusesForTask(PublishSkillsTask(
            search=ApiSearch(query=searchQuery),
            publishStatus=PublishStatus.Published,
            userString=userString
        ))

        assertThat(batchResult.totalCount).isEqualTo(toPublishCount)
        assertThat(batchResult.modifiedCount).isEqualTo(toPublishCount)
        knownDaos.forEach { oldDao ->
            val newDao = richSkillRepository.findById(oldDao.id.value)
            val skill = newDao!!.toModel()
            assertThat(skill.publishStatus()).isEqualTo(PublishStatus.Published)
            assertThat(skill.publishDate).isNotNull()
            assertThat(skill.archiveDate).isNull()
        }
        skillDaos.forEach { oldDao ->
            val newDao = richSkillRepository.findById(oldDao.id.value)
            val skill = newDao!!.toModel()
            assertThat(skill.publishStatus()).isEqualTo(PublishStatus.Unpublished)
        }
    }

    @Test
    fun `should be able to bulk publish or archive skills based on uuids`() {
        val totalSkillCount = 10
        val skillUpdates = (1..totalSkillCount).toList().map { apiSkillUpdateGenerator() }
        val skillDaos = richSkillRepository.createFromApi(skillUpdates, userString)
        assertThat(skillDaos.size).isEqualTo(totalSkillCount)

        val toPublishCount = 3
        val skillDaosToPublish = skillDaos.subList(0, toPublishCount)

        val batchResult = richSkillRepository.changeStatusesForTask(PublishSkillsTask(
            search=ApiSearch(uuids=skillDaosToPublish.map { it.uuid }),
            publishStatus=PublishStatus.Published,
            userString=userString
        ))

        assertThat(batchResult.totalCount).isEqualTo(toPublishCount)
        assertThat(batchResult.modifiedCount).isEqualTo(toPublishCount)
        skillDaosToPublish.forEach { oldDao ->
            val newDao = richSkillRepository.findById(oldDao.id.value)
            val skill = newDao!!.toModel()
            assertThat(skill.publishStatus()).isEqualTo(PublishStatus.Published)
            assertThat(skill.publishDate).isNotNull()
            assertThat(skill.archiveDate).isNull()
        }

        // attempt to archive all the skills, only published ones should get archived
        val archiveResult = richSkillRepository.changeStatusesForTask(PublishSkillsTask(
            search=ApiSearch(uuids=skillDaos.map { it.uuid }),
            publishStatus=PublishStatus.Archived,
            userString=userString
        ))

        assertThat(archiveResult.totalCount).isEqualTo(totalSkillCount)
        assertThat(archiveResult.modifiedCount).isEqualTo(toPublishCount)
        skillDaos.forEach { oldDao ->
            val newDao = richSkillRepository.findById(oldDao.id.value)
            val skill = newDao!!.toModel()
            if (skillDaosToPublish.contains(oldDao)) {
                assertThat(skill.publishStatus()).isEqualTo(PublishStatus.Archived)
                assertThat(skill.publishDate).isNotNull()
                assertThat(skill.archiveDate).isNotNull()
            } else {
                assertThat(skill.publishStatus()).isEqualTo(PublishStatus.Unpublished)
                assertThat(skill.publishDate).isNull()
                assertThat(skill.archiveDate).isNull()
            }

        }
    }
}
