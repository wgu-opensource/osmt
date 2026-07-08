package edu.wgu.osmt.credentialengine

import edu.wgu.osmt.BaseDockerizedTest
import edu.wgu.osmt.HasDatabaseReset
import edu.wgu.osmt.SpringTest
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillRepository
import edu.wgu.osmt.richskill.RsdUpdateObject
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.deleteAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Tests for sync pagination logic. Verifies:
 * - findSkillsUpdatedSince returns no duplicate uuids within a batch
 * - composite cursor (watermarkDate, watermarkId) correctly advances
 * - each skill appears at most once across all batches
 */
@Transactional
class SyncQueryHelpersTest :
    SpringTest(),
    BaseDockerizedTest,
    HasDatabaseReset {
    @Autowired
    lateinit var richSkillRepository: RichSkillRepository

    @BeforeEach
    fun clearData() {
        edu.wgu.osmt.collection.CollectionSkills
            .deleteAll()
        edu.wgu.osmt.richskill.RichSkillJobCodes
            .deleteAll()
        edu.wgu.osmt.richskill.RichSkillKeywords
            .deleteAll()
        edu.wgu.osmt.richskill.RichSkillDescriptorTable
            .deleteAll()
        SyncStateTable.deleteAll()
    }

    private fun createPublishedSkill(name: String = UUID.randomUUID().toString()): RichSkillDescriptorDao =
        richSkillRepository.create(
            RsdUpdateObject(
                name = name,
                statement = UUID.randomUUID().toString(),
                publishStatus = PublishStatus.Published,
            ),
            "test-user",
        )!!

    @Test
    fun `findSkillsUpdatedSince returns no duplicate uuids in batch`() {
        repeat(5) { createPublishedSkill() }

        val batch = findSkillsUpdatedSince(null, null, 20)
        val uuids = batch.map { it.uuid }

        assertThat(uuids).hasSize(uuids.toSet().size)
    }

    @Test
    fun `findSkillsUpdatedSince composite cursor excludes already-processed records`() {
        val s1 = createPublishedSkill("first")
        val s2 = createPublishedSkill("second")
        val s3 = createPublishedSkill("third")

        val batch1 = findSkillsUpdatedSince(null, null, 2)
        assertThat(batch1).hasSize(2)

        val last =
            batch1.maxWithOrNull(
                compareBy<RichSkillDescriptorDao> { it.updateDate }.thenBy { it.id.value },
            )!!
        val batch2 = findSkillsUpdatedSince(last.updateDate, last.id.value, 10)

        val batch1Uuids = batch1.map { it.uuid }.toSet()
        batch2.forEach {
            assertThat(batch1Uuids).doesNotContain(it.uuid)
        }
    }

    /**
     * Reproduces the infinite-loop bug: when watermark is (date, id) of the last
     * record with that date, the next fetch must return empty. Skill 780 scenario.
     */
    @Test
    fun `cursor with exact last record returns empty - no infinite loop`() {
        val skill = createPublishedSkill("single")
        val watermarkDate = skill.updateDate
        val watermarkId = skill.id.value

        val next = findSkillsUpdatedSince(watermarkDate, watermarkId, 10)

        assertThat(next)
            .describedAs(
                "fetch with watermark (date=%s, id=%d) must return empty - else sync loops forever",
                watermarkDate,
                watermarkId,
            ).isEmpty()
    }

    @Test
    fun `full pagination loop returns each skill exactly once`() {
        repeat(25) { createPublishedSkill() }

        val allUuids = mutableSetOf<String>()
        var watermarkDate: java.time.LocalDateTime? = null
        var watermarkId: Long? = null

        while (true) {
            val batch = findSkillsUpdatedSince(watermarkDate, watermarkId, 10)
            if (batch.isEmpty()) break

            val batchUuids = batch.map { it.uuid }
            batchUuids.forEach { uuid ->
                assertThat(allUuids).doesNotContain(uuid).withFailMessage(
                    "Duplicate uuid $uuid in pagination - already seen in previous batch",
                )
                allUuids.add(uuid)
            }

            val last =
                batch.maxWithOrNull(
                    compareBy<RichSkillDescriptorDao> { it.updateDate }.thenBy { it.id.value },
                )!!
            watermarkDate = last.updateDate
            watermarkId = last.id.value
        }

        assertThat(allUuids).hasSize(25)
    }

    @Test
    fun `countSkillsUpdatedSince with null watermark returns total published skill count`() {
        repeat(5) { createPublishedSkill() }
        assertThat(countSkillsUpdatedSince(null, null)).isEqualTo(5)
    }

    @Test
    fun `countSkillsUpdatedSince with watermark of last record returns 0`() {
        val skill = createPublishedSkill("single")
        val count =
            countSkillsUpdatedSince(skill.updateDate, skill.id.value)
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `countSkillsUpdatedSince matches find count after partial sync`() {
        repeat(5) { createPublishedSkill() }
        val batch1 = findSkillsUpdatedSince(null, null, 3)
        assertThat(batch1).hasSize(3)

        val last =
            batch1.maxWithOrNull(
                compareBy<RichSkillDescriptorDao> { it.updateDate }
                    .thenBy { it.id.value },
            )!!
        val remaining =
            findSkillsUpdatedSince(last.updateDate, last.id.value, 100)
        val count = countSkillsUpdatedSince(last.updateDate, last.id.value)
        assertThat(count)
            .describedAs("count should equal remaining find size")
            .isEqualTo(remaining.size)
    }
}
