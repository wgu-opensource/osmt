package edu.wgu.osmt.collection

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.TestObjectHelpers.assertThatKeywordMatchesNamedReference
import edu.wgu.osmt.TestObjectHelpers.namedReferenceGenerator
import edu.wgu.osmt.api.model.ApiCollectionUpdate
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSkillListUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.CollectionAndRichSkills
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.RsdUpdateObject
import edu.wgu.osmt.task.UpdateCollectionSkillsTask
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.assertj.core.api.Assertions.assertThat
import java.util.*

@Transactional
class CollectionRepositoryTest: SpringTest(), BaseDockerizedTest {

    @Autowired
    lateinit var collectionRepository: CollectionRepository

    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    val userString = "unittestuser"

    @Test
    fun `should not create a blank collection`() {
        assertThat(collectionRepository.create(CollectionUpdateObject(), userString)).isNull()
        assertThat(collectionRepository.create(CollectionUpdateObject(name=""), userString)).isNull()
        assertThat(collectionRepository.create(CollectionUpdateObject(name=" "), userString)).isNull()
    }

    @Test
    fun `should create collections from ApiCollectionUpdate objects`() {
        val count = 1
        val updates = (1..count).toList().map { random_collection_update() }
        val results = collectionRepository.createFromApi(updates, richSkillRepository, userString)

        results.forEachIndexed { i, collectionDao ->
            val collectionAndSkills = CollectionAndRichSkills.fromDao(collectionDao)
            val apiObj = updates[i]
            assertThatCollectionMatchesApiCollectionUpdate(collectionAndSkills, apiObj)
        }
    }

    private fun assertThatCollectionMatchesApiCollectionUpdate(crs: CollectionAndRichSkills, apiObj: ApiCollectionUpdate) {
        val collection = crs.collection

        assertThat(collection.name).isEqualTo(apiObj.name)
        assertThatKeywordMatchesNamedReference(collection.author, apiObj.author)
        assertThat(collection.publishStatus()).isEqualTo(apiObj.publishStatus)

        assertThat(crs.skills.map { it.uuid }.toSet() ).isEqualTo(apiObj.skills!!.add!!.toSet())
    }

    private fun random_skill(): RichSkillDescriptorDao {
        return richSkillRepository.create(RsdUpdateObject(
            name=UUID.randomUUID().toString(),
            statement=UUID.randomUUID().toString()
        ), userString)!!
    }

    private fun random_collection_update(): ApiCollectionUpdate {
        val name = UUID.randomUUID().toString()
        val author = namedReferenceGenerator(includeName=true, includeUri=false)
        val status = PublishStatus.Published
        val skillCount = 3
        val skillDaos = (1..skillCount).toList().map { random_skill() }
        val skills = ApiStringListUpdate(
            add=skillDaos.map { it.toModel().uuid }
        )
        return ApiCollectionUpdate(
            name=name,
            author=author,
            publishStatus=status,
            skills=skills
        )
    }

    @Test
    fun `should update an existing collection from an ApiCollectionUpdate`() {
        val originalUpdate = random_collection_update()
        val originalDao = collectionRepository.createFromApi(listOf(originalUpdate), richSkillRepository, userString).first()

        var newUpdate = random_collection_update()
        newUpdate = newUpdate.copy(
            skills=newUpdate.skills?.copy(remove=originalUpdate.skills?.add)
        )

        val updatedDao = collectionRepository.updateFromApi(originalDao.id.value, newUpdate, richSkillRepository, userString)
        assertThat(updatedDao).isNotNull
        assertThatCollectionMatchesApiCollectionUpdate(CollectionAndRichSkills.fromDao(updatedDao!!), newUpdate)
    }

    @Test
    fun `should add all skills from all pages of a search result to a collection`() {
        val totalSkillCount = 10
        val toAddCount = 7
        val searchQuery = "known prefix"
        val skillUpdates = (1..totalSkillCount-toAddCount).toList().map { TestObjectHelpers.apiSkillUpdateGenerator() }
        val knownUpdates = (1..toAddCount).toList().map {
            TestObjectHelpers.apiSkillUpdateGenerator(
                name = "${searchQuery} ${UUID.randomUUID()}"
            )
        }

        val collectionDao = collectionRepository.create(UUID.randomUUID().toString(), userString)
        val collection = collectionDao!!.toModel()

        val skillDaos = richSkillRepository.createFromApi(skillUpdates, userString)
        val knownDaos = richSkillRepository.createFromApi(knownUpdates, userString)
        assertThat(skillDaos.size + knownDaos.size).isEqualTo(totalSkillCount)

        val task = UpdateCollectionSkillsTask(
            collection.uuid,
            skillListUpdate = ApiSkillListUpdate(add= ApiSearch(query=searchQuery)),
            userString=userString
        )
        val batchResult = collectionRepository.updateSkillsForTask(collection.uuid, task, richSkillRepository)

        assertThat(batchResult.totalCount).isEqualTo(toAddCount)
        assertThat(batchResult.modifiedCount).isEqualTo(toAddCount)

        val newCollectionDao = collectionRepository.findById(collection.id!!)!!
        val skillUuids = newCollectionDao.skills.map { it.toModel().uuid }
        knownDaos.forEach { knownDao ->
            assertThat(skillUuids).contains(knownDao.uuid)
        }
    }
}