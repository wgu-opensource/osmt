package edu.wgu.osmt.richskill

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.HasElasticsearchReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.TestObjectHelpers.apiSkillUpdateGenerator
import edu.wgu.osmt.TestObjectHelpers.assertThatKeywordMatchesAlignment
import edu.wgu.osmt.TestObjectHelpers.assertThatKeywordMatchesNamedReference
import edu.wgu.osmt.api.model.ApiAlignmentListUpdate
import edu.wgu.osmt.api.model.ApiReferenceListUpdate
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSkillUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionEsRepo
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.collection.CollectionUpdateObject
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCode
import edu.wgu.osmt.jobcode.JobCodeEsRepo
import edu.wgu.osmt.keyword.Keyword
import edu.wgu.osmt.keyword.KeywordEsRepo
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.task.PublishTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
class RichSkillRepositoryTest @Autowired constructor(
    override val richSkillEsRepo: RichSkillEsRepo,
    override val collectionEsRepo: CollectionEsRepo,
    override val keywordEsRepo: KeywordEsRepo,
    override val jobCodeEsRepo: JobCodeEsRepo
) : SpringTest(), BaseDockerizedTest, HasDatabaseReset, HasElasticsearchReset  {

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var keywordRepository: KeywordRepository

    val userString = "unittestuser"
    
    val userEmail = "user@email.com"


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

    fun assertThatKeywordsMatchAlignmentList(keywords: List<Keyword>, referenceList: ApiAlignmentListUpdate) {
        referenceList.add?.forEach { align ->
            val found = keywords.find { it.value == align.skillName && it.uri == align.id }
            assertThat(found).isNotNull
            assertThatKeywordMatchesAlignment(found, align)
        }

        referenceList.remove?.forEach { align ->
            val found = keywords.find { it.value == align.skillName && it.uri == align.id }
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

        assertThatKeywordsMatchStringList(skill.authors, apiObj.authors!!)

        assertThatKeywordsMatchStringList(skill.searchingKeywords, apiObj.keywords!!)

        assertThatKeywordsMatchReferenceList(skill.certifications, apiObj.certifications!!)
        assertThatKeywordsMatchAlignmentList(skill.standards, apiObj.standards!!)
        assertThatKeywordsMatchAlignmentList(skill.alignments, apiObj.alignments!!)
        assertThatKeywordsMatchReferenceList(skill.employers, apiObj.employers!!)

        assertThatJobCodesMatchStringList(skill.jobCodes, apiObj.occupations!!)

        assertThatCollectionsMatchStringList(rsc.collections, apiObj.collections!!)

        assertThat(skill.publishStatus()).isEqualTo(apiObj.publishStatus)
    }

    @Test
    fun `should update an existing skill from an ApiSkillUpdate object`() {
        val originalSkillUpdate = apiSkillUpdateGenerator()
        val originalSkillDao = richSkillRepository.createFromApi(
            listOf(originalSkillUpdate),
            userString,
            userEmail
        ).first()

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

        val updatedDao = richSkillRepository.updateFromApi(
            originalSkillDao.id.value,
            newSkillUpdate,
            userString,
            userEmail
        )
        assertThat(updatedDao).isNotNull
        assertThatRichSkillMatchesApiSkillUpdate(RichSkillAndCollections.fromDao(updatedDao!!), newSkillUpdate)
    }

    @Test
    fun `should create skills from ApiSkillUpdate objects`() {
        val skillCount = 1
        val skillUpdates = (1..skillCount).toList().map { apiSkillUpdateGenerator() }

        val results: List<RichSkillDescriptorDao> = richSkillRepository.createFromApi(
            skillUpdates,
            userString,
            userEmail
        )

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
            authors = null,
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
        var apiUpdated = richSkillRepository.updateFromApi(
            created!!.id!!,
            apiUpdate,
            userString,
            userEmail
        )?.toModel()
        assertThat(apiUpdated).isNotNull
        assertThat(apiUpdated?.category?.value).isEqualTo(categoryName)

        // pass category as empty string to nullify it
        val apiUpdateBlank = ApiSkillUpdate(
            category=""
        )
        apiUpdated = richSkillRepository.updateFromApi(
            created.id!!,
            apiUpdateBlank,
            userString,
            userEmail
        )?.toModel()
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

        val skillDaos = richSkillRepository.createFromApi(skillUpdates, userString, userEmail)
        val knownDaos = richSkillRepository.createFromApi(knownUpdates, userString, userEmail)
        assertThat(skillDaos.size + knownDaos.size).isEqualTo(totalSkillCount)

        val batchResult = richSkillRepository.changeStatusesForTask(PublishTask(
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
            assertThat(skill.publishStatus()).isEqualTo(PublishStatus.Draft)
        }
    }

    @Test
    fun `should be able to bulk publish some skills within a collection`() {
        val archivedCount = 2
        val publishedCount = 2
        val draftCount = 2
        val deletedCount = 2
        val includedCount = archivedCount+publishedCount+draftCount+deletedCount
        val notIncludedCount = 10

        val archivedSkills = (1..archivedCount).toList().flatMap {
            richSkillRepository.createFromApi(
                listOf(apiSkillUpdateGenerator(publishStatus=PublishStatus.Published)),
                userString,
                userEmail
            ).map { skillDao ->
                richSkillRepository.update(RsdUpdateObject(id=skillDao.id.value, publishStatus=PublishStatus.Archived), userString)
            }.filterNotNull()
        }
        val publishedSkills = (1..publishedCount).toList().flatMap {
            richSkillRepository.createFromApi(
                listOf(apiSkillUpdateGenerator(publishStatus=PublishStatus.Published)),
                userString,
                userEmail
            )
        }
        val draftSkills = (1..draftCount).toList().flatMap {
            richSkillRepository.createFromApi(
                listOf(apiSkillUpdateGenerator(publishStatus=PublishStatus.Draft)),
                userString,
                userEmail
            )
        }

        // make skills that are draft+archived
        val deletedSkills = (1..deletedCount).toList().flatMap {
            richSkillRepository.createFromApi(
                listOf(apiSkillUpdateGenerator(publishStatus=PublishStatus.Draft)),
                userString,
                userEmail
            ).map { skillDao ->
                richSkillRepository.update(RsdUpdateObject(id=skillDao.id.value, publishStatus=PublishStatus.Archived), userString)
            }.filterNotNull()
        }

        val extraSkills = (1..notIncludedCount).toList().flatMap {
            richSkillRepository.createFromApi(
                listOf(apiSkillUpdateGenerator(publishStatus=PublishStatus.Draft)),
                userString,
                userEmail
            )
        }

        publishedSkills.forEach { assertThat(it.publishStatus()).isEqualTo(PublishStatus.Published) }
        archivedSkills.forEach { assertThat(it.publishStatus()).isEqualTo(PublishStatus.Archived) }
        draftSkills.forEach { assertThat(it.publishStatus()).isEqualTo(PublishStatus.Draft) }
        deletedSkills.forEach { assertThat(it.publishStatus()).isEqualTo(PublishStatus.Deleted) }

        // create collection
        val collectionDao = collectionRepository.create(UUID.randomUUID().toString(), userString, userEmail)
        val collection = collectionDao?.toModel()
        assertThat(collection).isNotNull

        // add skills to collection
        collectionRepository.update(CollectionUpdateObject(
            id=collection!!.id!!,
            skills=ListFieldUpdate(add=(archivedSkills+publishedSkills+draftSkills+deletedSkills))
        ), userString)

        // publish all draft skills in collection
        val publishTask = PublishTask(
            search=ApiSearch(),
            publishStatus=PublishStatus.Published,
            filterByStatus=setOf(PublishStatus.Draft),
            collectionUuid=collection.uuid,
            userString=userString
        )

        val batchResult = richSkillRepository.changeStatusesForTask(publishTask)
        assertThat(batchResult.totalCount).isEqualTo(draftCount)
        assertThat(batchResult.modifiedCount).isEqualTo(draftCount)
    }

    @Test
    fun `should be able to bulk publish or archive skills based on uuids`() {
        val totalSkillCount = 10
        val skillUpdates = (1..totalSkillCount).toList().map { apiSkillUpdateGenerator() }
        val skillDaos = richSkillRepository.createFromApi(skillUpdates, userString, userEmail)
        assertThat(skillDaos.size).isEqualTo(totalSkillCount)

        val toPublishCount = 3
        val skillDaosToPublish = skillDaos.subList(0, toPublishCount)

        val batchResult = richSkillRepository.changeStatusesForTask(PublishTask(
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

        // attempt to publish all the skills, only un-published ones should get published
        val archiveResult = richSkillRepository.changeStatusesForTask(PublishTask(
            search=ApiSearch(uuids=skillDaos.map { it.uuid }),
            publishStatus=PublishStatus.Published,
            userString=userString
        ))

        assertThat(archiveResult.totalCount).isEqualTo(totalSkillCount)
        assertThat(archiveResult.modifiedCount).isEqualTo(totalSkillCount - toPublishCount)
        skillDaos.forEach { oldDao ->
            val newDao = richSkillRepository.findById(oldDao.id.value)
            val skill = newDao!!.toModel()
            assertThat(skill.publishStatus()).isEqualTo(PublishStatus.Published)
            assertThat(skill.publishDate).isNotNull()
            assertThat(skill.archiveDate).isNull()
        }
    }

    @Test
    fun `should find rich skills by jobcode`(){
        val jobCode = TestObjectHelpers.randomJobCode()
        val skillUpdate = apiSkillUpdateGenerator().copy(occupations = ApiStringListUpdate(add = listOf(jobCode.code)))
        val noiseSkillCount = 10
        val noiseSkillUpdates = (1..noiseSkillCount).toList().map { apiSkillUpdateGenerator() }
        richSkillRepository.createFromApi(noiseSkillUpdates, userString, userEmail)
        val skillDao = richSkillRepository.createFromApi(listOf(skillUpdate), userString, userEmail).first()

        val result = richSkillRepository.containingJobCode(jobCode.code)
        assertThat(result.first().uuid).isEqualTo(skillDao.uuid)
    }
}
