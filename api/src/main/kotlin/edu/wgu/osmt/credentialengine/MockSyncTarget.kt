package edu.wgu.osmt.credentialengine

import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.richskill.RichSkillDescriptor
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

class MockSyncTarget : SyncTarget {
    private val publishedSkills = CopyOnWriteArrayList<String>()
    private val publishedCollections = CopyOnWriteArrayList<String>()
    private val deprecatedSkills = CopyOnWriteArrayList<String>()
    private val deprecatedCollections = CopyOnWriteArrayList<String>()

    private val logger = LoggerFactory.getLogger(MockSyncTarget::class.java)

    override fun publishSkill(rsd: RichSkillDescriptor): Result<Unit> {
        logger.info("MockSyncTarget: publishSkill uuid={} name={}", rsd.uuid, rsd.name)
        publishedSkills.add(rsd.uuid)
        return Result.success(Unit)
    }

    override fun publishCollection(
        collection: Collection,
        skillCtids: List<String>,
    ): Result<Unit> {
        logger.info(
            "MockSyncTarget: publishCollection uuid={} name={} skillCtids={}",
            collection.uuid,
            collection.name,
            skillCtids,
        )
        publishedCollections.add(collection.uuid)
        return Result.success(Unit)
    }

    override fun deprecateSkill(rsd: RichSkillDescriptor): Result<Unit> {
        logger.info("MockSyncTarget: deprecateSkill uuid={}", rsd.uuid)
        deprecatedSkills.add(rsd.uuid)
        publishedSkills.remove(rsd.uuid)
        return Result.success(Unit)
    }

    override fun deprecateCollection(collection: Collection): Result<Unit> {
        logger.info("MockSyncTarget: deprecateCollection uuid={}", collection.uuid)
        deprecatedCollections.add(collection.uuid)
        publishedCollections.remove(collection.uuid)
        return Result.success(Unit)
    }

    override fun unpublishAll(
        collectionUuids: List<String>,
        skillUuids: List<String>,
    ): Result<Unit> {
        publishedSkills.clear()
        publishedCollections.clear()
        deprecatedSkills.clear()
        deprecatedCollections.clear()
        logger.info("MockSyncTarget: unpublishAll cleared all lists")
        return Result.success(Unit)
    }

    fun getPublishedSkillUuids(): List<String> = publishedSkills.toList()

    fun getPublishCountPerSkill(): Map<String, Int> = publishedSkills.groupingBy { it }.eachCount()

    fun getPublishedCollectionUuids(): List<String> = publishedCollections.toList()

    fun getDeprecatedSkillUuids(): List<String> = deprecatedSkills.toList()

    fun getDeprecatedCollectionUuids(): List<String> = deprecatedCollections.toList()
}
