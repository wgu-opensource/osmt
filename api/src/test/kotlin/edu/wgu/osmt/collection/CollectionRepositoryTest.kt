package edu.wgu.osmt.collection

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.api.model.ApiCollectionUpdate
import edu.wgu.osmt.api.model.ApiSearch
import edu.wgu.osmt.api.model.ApiSkillListUpdate
import edu.wgu.osmt.api.model.ApiStringListUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.CollectionAndRichSkills
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.RsdUpdateObject
import edu.wgu.osmt.task.PublishTask
import edu.wgu.osmt.task.UpdateCollectionSkillsTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
class CollectionRepositoryTest: SpringTest(), BaseDockerizedTest, HasDatabaseReset {

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
        assertThat(collection.author?.value).isEqualTo(apiObj.author)
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
        val author = UUID.randomUUID().toString()
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

    @Test
    fun testFindAll() {
        // Act
        val collectionDao = collectionRepository.findAll()

        // Assert
        assertThat(collectionDao).isNotNull
    }

    @Test
    fun testChangeStatusesForTask() {
        // Arrange
        val collectionsCount = 2
        val collections = (1..collectionsCount).toList().map { collectionRepository.create(UUID.randomUUID().toString(), userString)!!.toModel() }

        val task = PublishTask(
                search=ApiSearch(uuids= collections.map { it.uuid }),
                userString=userString
        )

        // Act
        val batchResult = collectionRepository.changeStatusesForTask(task)

        // Assert
        assertThat(batchResult?.modifiedCount).isEqualTo(collectionsCount)

    }

    @Disabled
    fun testChangeStatusesForTaskWithCollectionId() {
        // Arrange
        val skillCount = 3
        val skills = (1..skillCount).toList().map { TestObjectHelpers.apiSkillUpdateGenerator() }

        val collectionDao = collectionRepository.create(UUID.randomUUID().toString(), userString)
        val collection = collectionDao!!.toModel()

        richSkillRepository.createFromApi(skills, userString)

        val task = PublishTask(
            search=ApiSearch(),
            collectionUuid=collection.uuid,
            userString=userString
        )

        // Act
        val batchResult = collectionRepository.changeStatusesForTask(task)

        // Assert
        assertThat(batchResult?.modifiedCount).isEqualTo(skillCount*3)
    }

    @Test
    fun `remove finds and succesfully removes an existing collection`() {
        // Arrange
        val collection = collectionRepository.create(UUID.randomUUID().toString(), userString)!!.toModel()

        // Act
        val batchResult = collectionRepository.remove(collection.uuid)

        // Assert
        assertThat(batchResult?.modifiedCount).isEqualTo(1)
        assertThat(batchResult?.success).isEqualTo(true)

    }

    @Test
    fun `remove fails to remove a non-existing collection`() {
        // Act
        val batchResult = collectionRepository.remove(UUID.randomUUID().toString())

        // Assert
        assertThat(batchResult?.modifiedCount).isEqualTo(0)
        assertThat(batchResult?.success).isEqualTo(false)
    }

}