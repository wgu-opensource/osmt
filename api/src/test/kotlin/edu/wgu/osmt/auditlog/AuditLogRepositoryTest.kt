package edu.wgu.osmt.auditlog

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.TestObjectHelpers
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.collection.CollectionTable
import edu.wgu.osmt.collection.CollectionUpdateObject
import edu.wgu.osmt.db.ListFieldUpdate
import edu.wgu.osmt.db.NullableFieldUpdate
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.jobcode.JobCodeRepository
import edu.wgu.osmt.keyword.KeywordRepository
import edu.wgu.osmt.keyword.KeywordTypeEnum
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.RsdUpdateObject
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.SizedIterable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class AuditLogRepositoryTest @Autowired constructor(
    val auditLogRepository: AuditLogRepository,
    val richSkillRepository: RichSkillRepository,
    val collectionRepository: CollectionRepository,
    val keywordRepository: KeywordRepository,
    val jobCodeRepository: JobCodeRepository
) : SpringTest(), BaseDockerizedTest, HasDatabaseReset {

    @Test
    fun `can insert a collection audit log`() {
        val auditLog =
            AuditLog.Companion.fromAtomicOp(CollectionTable, 1L, listOf(), "test user", AuditOperationType.Insert)
        auditLogRepository.create(auditLog)
        val result: SizedIterable<AuditLogDao> =
            auditLogRepository.findByTableAndId(auditLog.tableName, auditLog.entityId)
        assertThat(result.first().entityId).isEqualTo(auditLog.entityId)
    }

    @Test
    fun `can insert a skill audit log`() {
        val auditLog = AuditLog.Companion.fromAtomicOp(
            RichSkillDescriptorTable,
            1L,
            listOf(),
            "test user",
            AuditOperationType.Insert
        )
        auditLogRepository.create(auditLog)
        val result: SizedIterable<AuditLogDao> =
            auditLogRepository.findByTableAndId(auditLog.tableName, auditLog.entityId)
        assertThat(result.first().entityId).isEqualTo(auditLog.entityId)
    }

    @Test
    fun `generates an audit log on collection creation`() {
        val collectionDao = collectionRepository.create("test collection", testUser, testEmail)
        val auditLog = collectionDao?.id?.value?.let {
            auditLogRepository.findByTableAndId(CollectionTable.tableName, it)
        }?.first()

        assertThat(auditLog?.entityId).isEqualTo(collectionDao?.id?.value)
    }

    @Test
    fun `generates an audit log on rich skill creation`() {
        val updateObject = RsdUpdateObject(name = "test skill", statement = testUser)
        val skillDao = richSkillRepository.create(updateObject, testUser)
        val auditLog = skillDao?.id?.value?.let {
            auditLogRepository.findByTableAndId(RichSkillDescriptorTable.tableName, it)
        }?.first()

        assertThat(auditLog?.entityId).isEqualTo(skillDao?.id?.value)
    }

    @Test
    fun `generates audit logs on collection update`() {
        val updatedAuthorName = "Updated Author"
        val initialSkill =
            richSkillRepository.create(RsdUpdateObject(name = "initial skill", statement = "test statement"), testUser)
        val addedSkill =
            richSkillRepository.create(RsdUpdateObject(name = "added skill", statement = "test statement"), testUser)

        val initialCollectionUpdate =
            CollectionUpdateObject(name = "test collection", skills = ListFieldUpdate(add = listOf(initialSkill!!)))

        val collectionDao = collectionRepository.create(initialCollectionUpdate, testUser, testEmail)
        val newAuthorDao = keywordRepository.create(KeywordTypeEnum.Author, updatedAuthorName)

        val collectionUpdateObject = CollectionUpdateObject(
            id = collectionDao?.id?.value,
            name = "new name",
            author = NullableFieldUpdate(newAuthorDao),
            skills = ListFieldUpdate(add = listOf(addedSkill!!), remove = listOf(initialSkill)),
            publishStatus = PublishStatus.Published
        )
        val collectionUpdateDao = collectionRepository.update(collectionUpdateObject, testUser)

        val logs = auditLogRepository.findByTableAndId(CollectionTable.tableName, collectionDao!!.id.value).toList()
            .map { it.toModel() }
        val initialLog = logs.find { it.operationType == AuditOperationType.Insert.name }!!
        val updateLog = logs.find { it.operationType == AuditOperationType.Update.name }!!
        val publishStatusLog = logs.find { it.operationType == AuditOperationType.PublishStatusChange.name }!!

        val initialRichSkillLogs =
            auditLogRepository.findByTableAndId(RichSkillDescriptorTable.tableName, initialSkill.id.value).toList()
                .map { it.toModel() }
        val updatedRichSkillLogs =
            auditLogRepository.findByTableAndId(RichSkillDescriptorTable.tableName, addedSkill.id.value).toList()
                .map { it.toModel() }

        assertThat(initialLog.changedFields.findByFieldName(CollectionDao::name.name)).isEqualTo(
            Change(
                CollectionDao::name.name,
                old = null,
                new = initialCollectionUpdate.name
            )
        )

        // updates
        assertThat(updateLog.changedFields.findByFieldName(CollectionDao::author.name)).isEqualTo(
            Change(
                CollectionDao::author.name,
                old = null,
                new = collectionUpdateDao?.author?.value
            )
        )

        assertThat(publishStatusLog.changedFields.findByFieldName(CollectionDao::publishStatus.name)).isEqualTo(
            Change(
                CollectionDao::publishStatus.name,
                old = PublishStatus.Draft.name,
                new = PublishStatus.Published.name
            )
        )

        assertThat(
            initialRichSkillLogs.find { it.operationType == AuditOperationType.Update.name }!!.changedFields.findByFieldName(
                RichSkillDescriptorDao::collections.name
            )
        ).isEqualTo(
            Change(
                RichSkillDescriptorDao::collections.name, old = initialCollectionUpdate.name, new = null
            )
        )

        assertThat(
            updatedRichSkillLogs.find { it.operationType == AuditOperationType.Update.name }!!.changedFields.findByFieldName(
                RichSkillDescriptorDao::collections.name
            )
        ).isEqualTo(
            Change(
                RichSkillDescriptorDao::collections.name, old = null, new = collectionDao.name
            )
        )
    }

    @Test
    fun `generates audit logs on rich skill update`() {
        val initialSkillName = "initial skill"
        val initialStatement = "test statement"
        val skill =
            richSkillRepository.create(RsdUpdateObject(name = initialSkillName, statement = initialStatement), testUser)
        val authorDaos = listOf(keywordRepository.getDefaultAuthor())
        val categoryDao = keywordRepository.create(KeywordTypeEnum.Category, "Test Category")

        val keywordDaos = TestObjectHelpers.keywordsGenerator(10, KeywordTypeEnum.Keyword)
            .mapNotNull { keywordRepository.create(it.type, it.value) }
        val jobCodeDaos = listOf(jobCodeRepository.create("11-1170"))
        val collectionDaos = listOf(collectionRepository.create("test collection", testUser, testEmail)!!)

        val newName = "updated skill"
        val newStatement = "new statement"
        val skillUpdate = RsdUpdateObject(
            id = skill!!.id.value,
            name = newName,
            statement = newStatement,
            category = NullableFieldUpdate(categoryDao),
            keywords = ListFieldUpdate(add = (keywordDaos + authorDaos)),
            jobCodes = ListFieldUpdate(add = jobCodeDaos),
            collections = ListFieldUpdate(add = collectionDaos),
            publishStatus = PublishStatus.Published
        )
        val updatedResult = richSkillRepository.update(skillUpdate, testUser)?.toModel()

        val secondUpdate = RsdUpdateObject(
            id = skill.id.value,
            category = NullableFieldUpdate(null),
            keywords = ListFieldUpdate(remove = (keywordDaos + authorDaos)),
            jobCodes = ListFieldUpdate(remove = jobCodeDaos),
            collections = ListFieldUpdate(remove = collectionDaos)
        )

        val secondUpdateResult = richSkillRepository.update(secondUpdate, testUser)?.toModel()

        val updateLogs =
            auditLogRepository.findByTableAndId(RichSkillDescriptorTable.tableName, skill.id.value).map { it.toModel() }
                .filter { it.operationType == AuditOperationType.Update.name }.sortedBy { it.creationDate }

        val publishStatusLogs = auditLogRepository.findByTableAndId(RichSkillDescriptorTable.tableName, skill.id.value).map { it.toModel() }
            .filter { it.operationType == AuditOperationType.PublishStatusChange.name }.sortedBy { it.creationDate }

        val firstUpdateLog = updateLogs.get(0)
        val secondUpdateLog = updateLogs.get(1)

        assertThat(firstUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::name.name)).isEqualTo(
            Change(
                RichSkillDescriptor::name.name,
                initialSkillName,
                newName
            )
        )

        assertThat(firstUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::statement.name)).isEqualTo(
            Change(
                RichSkillDescriptor::statement.name,
                initialStatement,
                newStatement
            )
        )

        assertThat(firstUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::authors.name)).isEqualTo(
            Change(
                RichSkillDescriptor::authors.name,
                null,
                authorDaos.map { it.value }.joinToString(DELIMITER)
            )
        )

        assertThat(firstUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::category.name)).isEqualTo(
            Change(
                RichSkillDescriptor::category.name,
                null,
                categoryDao?.value
            )
        )

        assertThat(firstUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::searchingKeywords.name)).isEqualTo(
            Change(
                RichSkillDescriptor::searchingKeywords.name,
                null,
                keywordDaos.map { it.value }.joinToString(DELIMITER)
            )
        )

        assertThat(firstUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::collections.name)).isEqualTo(
            Change(
                RichSkillDescriptor::collections.name,
                null,
                collectionDaos.map { it.name }.joinToString(DELIMITER)
            )
        )

        assertThat(firstUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::jobCodes.name)).isEqualTo(
            Change(
                RichSkillDescriptor::jobCodes.name,
                null,
                jobCodeDaos.map { it.code }.joinToString(DELIMITER)
            )
        )

        assertThat(secondUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::authors.name)).isEqualTo(
            Change(
                RichSkillDescriptor::authors.name,
                updatedResult?.authors?.map { it.value }?.joinToString(DELIMITER),
                null
            )
        )

        assertThat(secondUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::category.name)).isEqualTo(
            Change(
                RichSkillDescriptor::category.name,
                updatedResult?.category?.value,
                null
            )
        )

        assertThat(secondUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::searchingKeywords.name)).isEqualTo(
            Change(
                RichSkillDescriptor::searchingKeywords.name,
                updatedResult?.searchingKeywords?.map { it.value }?.joinToString(DELIMITER),
                null
            )
        )

        assertThat(secondUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::jobCodes.name)).isEqualTo(
            Change(
                RichSkillDescriptor::jobCodes.name,
                updatedResult?.jobCodes?.map { it.code }?.joinToString(DELIMITER),
                null
            )
        )

        assertThat(secondUpdateLog.changedFields.findByFieldName(RichSkillDescriptor::collections.name)).isEqualTo(
            Change(
                RichSkillDescriptor::collections.name,
                updatedResult?.collections?.map { it.name }?.joinToString(DELIMITER),
                null
            )
        )

        assertThat(publishStatusLogs.get(0).changedFields.findByFieldName(RichSkillDescriptor::publishStatus.name)).isEqualTo(
            Change(
                RichSkillDescriptor::publishStatus.name,
                PublishStatus.Draft.name,
                PublishStatus.Published.name
            )
        )
    }

    @Test
    fun `generates rich skill audit log for collection name changes`() {
        // Arrange
        val initialCollectionName = "test collection"
        val updatedCollectionName = "updated collection"
        val expected = Change("name", initialCollectionName, updatedCollectionName)

        val initialSkill =
            richSkillRepository.create(RsdUpdateObject(name = "initial skill", statement = "test statement"), testUser)

        val initialCollectionUpdate =
            CollectionUpdateObject(name = initialCollectionName, skills = ListFieldUpdate(add = listOf(initialSkill!!)))

        // Act
        val collectionDao = collectionRepository.create(initialCollectionUpdate, testUser, testEmail)

        val collectionUpdate = CollectionUpdateObject(id = collectionDao?.id?.value, name = updatedCollectionName)
        collectionRepository.update(collectionUpdate, testUser)

        val auditLogResults =
            collectionDao?.id?.let {
                auditLogRepository.findByTableAndId(CollectionTable.tableName, it.value)
                    .find { it.operationType == AuditOperationType.Update.name }?.toModel()
            }

        // Assert
        val actual = auditLogResults?.changedFields?.first()
        assertThat(actual).isEqualTo(expected)
    }
}
