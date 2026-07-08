package edu.wgu.osmt.credentialengine

/*
 * Sync cursor queries for Credential Engine.
 *
 * IMPORTANT: All functions require an active Spring-managed transaction.
 * Callers must wrap calls in TransactionTemplate or @Transactional.
 *
 * When watermarkId != null, we use raw JDBC (find*Raw, count*Raw)
 * instead of Exposed DSL—Exposed's parameter binding returns
 * incorrect results for `id >= ?`.
 * See docs/known-issues/2026-03-04-exposed-sync-cursor-infinite-loop.md.
 */
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionTable
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillDescriptorTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.LocalDateTime

private val log =
    LoggerFactory.getLogger(
        "edu.wgu.osmt.credentialengine.SyncQueryHelpers",
    )

object SyncRecordType {
    const val SKILL = "skill"
    const val COLLECTION = "collection"
}

/**
 * Raw JDBC cursor query. Exposed DSL and exec() both bind
 * the id parameter incorrectly; only direct
 * PreparedStatement.setLong() returns correct results.
 * See docs/known-issues/2026-03-04-exposed-sync-cursor-infinite-loop.md
 */
private fun findSkillsUpdatedSinceRaw(
    watermarkDate: LocalDateTime,
    watermarkId: Long,
    limit: Int,
): List<RichSkillDescriptorDao> {
    val nextId = watermarkId + 1L
    val sql =
        """
        SELECT id FROM RichSkillDescriptor
        WHERE ((updateDate > ?) OR ((updateDate = ?) AND (id >= ?)))
        AND (publishDate IS NOT NULL)
        ORDER BY updateDate ASC, id ASC
        LIMIT ?
        """.trimIndent()
    val ts = Timestamp.valueOf(watermarkDate)
    val conn =
        TransactionManager.current().connection.connection
            as java.sql.Connection
    val ids =
        conn.prepareStatement(sql).use { ps ->
            ps.setTimestamp(1, ts)
            ps.setTimestamp(2, ts)
            ps.setLong(3, nextId)
            ps.setInt(4, limit)
            ps.executeQuery().use { rs ->
                val list = mutableListOf<Long>()
                while (rs.next()) list.add(rs.getLong(1))
                list
            }
        }
    if (ids.isEmpty()) return emptyList()
    return ids.mapNotNull { id ->
        RichSkillDescriptorDao
            .findById(EntityID(id, RichSkillDescriptorTable))
            .also { dao ->
                if (dao == null) {
                    log.warn(
                        "Skill id={} from cursor query missing on load",
                        id,
                    )
                }
            }
    }
}

/**
 * Composite cursor: (watermarkDate, watermarkId) for deterministic
 * pagination when many records share the same updateDate.
 */
fun findSkillsUpdatedSince(
    watermarkDate: LocalDateTime?,
    watermarkId: Long?,
    limit: Int,
): List<RichSkillDescriptorDao> =
    when {
        watermarkDate != null && watermarkId != null -> {
            findSkillsUpdatedSinceRaw(watermarkDate, watermarkId, limit)
        }

        else -> {
            RichSkillDescriptorDao
                .wrapRows(
                    when {
                        watermarkDate == null -> {
                            RichSkillDescriptorTable.select {
                                RichSkillDescriptorTable.publishDate
                                    .isNotNull()
                            }
                        }

                        else -> {
                            RichSkillDescriptorTable.select {
                                (
                                    RichSkillDescriptorTable.updateDate
                                        greater watermarkDate
                                ) and
                                    RichSkillDescriptorTable.publishDate
                                        .isNotNull()
                            }
                        }
                    }.orderBy(
                        RichSkillDescriptorTable.updateDate
                            to SortOrder.ASC,
                        RichSkillDescriptorTable.id to SortOrder.ASC,
                    ),
                ).limit(limit, 0)
                .toList()
        }
    }

/** Raw JDBC count; same predicate as findSkillsUpdatedSinceRaw. */
private fun countSkillsUpdatedSinceRaw(
    watermarkDate: LocalDateTime,
    watermarkId: Long,
): Int {
    val sql =
        """
        SELECT COUNT(*) FROM RichSkillDescriptor
        WHERE ((updateDate > ?) OR (updateDate = ? AND id > ?))
        AND (publishDate IS NOT NULL)
        """.trimIndent()
    val ts = Timestamp.valueOf(watermarkDate)
    val conn =
        TransactionManager.current().connection.connection
            as java.sql.Connection
    return conn.prepareStatement(sql).use { ps ->
        ps.setTimestamp(1, ts)
        ps.setTimestamp(2, ts)
        ps.setLong(3, watermarkId)
        ps.executeQuery().use { rs ->
            if (rs.next()) rs.getInt(1) else 0
        }
    }
}

fun countSkillsUpdatedSince(
    watermarkDate: LocalDateTime?,
    watermarkId: Long?,
): Int =
    when {
        watermarkDate != null && watermarkId != null -> {
            countSkillsUpdatedSinceRaw(watermarkDate, watermarkId)
        }

        watermarkDate == null -> {
            RichSkillDescriptorTable
                .select {
                    RichSkillDescriptorTable.publishDate.isNotNull()
                }.count()
                .toInt()
        }

        else -> {
            RichSkillDescriptorTable
                .select {
                    (
                        RichSkillDescriptorTable.updateDate greater
                            watermarkDate
                    ) and
                        RichSkillDescriptorTable.publishDate.isNotNull()
                }.count()
                .toInt()
        }
    }

/** Raw JDBC count; same predicate as findCollectionsUpdatedSinceRaw. */
private fun countCollectionsUpdatedSinceRaw(
    watermarkDate: LocalDateTime,
    watermarkId: Long,
): Int {
    val sql =
        """
        SELECT COUNT(*) FROM Collection
        WHERE ((updateDate > ?) OR (updateDate = ? AND id > ?))
        AND (status IN ('Published', 'Archived'))
        """.trimIndent()
    val ts = Timestamp.valueOf(watermarkDate)
    val conn =
        TransactionManager.current().connection.connection
            as java.sql.Connection
    return conn.prepareStatement(sql).use { ps ->
        ps.setTimestamp(1, ts)
        ps.setTimestamp(2, ts)
        ps.setLong(3, watermarkId)
        ps.executeQuery().use { rs ->
            if (rs.next()) rs.getInt(1) else 0
        }
    }
}

fun countCollectionsUpdatedSince(
    watermarkDate: LocalDateTime?,
    watermarkId: Long?,
): Int =
    when {
        watermarkDate != null && watermarkId != null -> {
            countCollectionsUpdatedSinceRaw(watermarkDate, watermarkId)
        }

        watermarkDate == null -> {
            CollectionTable
                .select {
                    CollectionTable.status inList
                        listOf(PublishStatus.Published, PublishStatus.Archived)
                }.count()
                .toInt()
        }

        else -> {
            CollectionTable
                .select {
                    (CollectionTable.updateDate greater watermarkDate) and
                        (
                            CollectionTable.status inList
                                listOf(
                                    PublishStatus.Published,
                                    PublishStatus.Archived,
                                )
                        )
                }.count()
                .toInt()
        }
    }

/** Raw JDBC cursor query; same rationale as findSkillsUpdatedSinceRaw. */
private fun findCollectionsUpdatedSinceRaw(
    watermarkDate: LocalDateTime,
    watermarkId: Long,
    limit: Int,
): List<CollectionDao> {
    val nextId = watermarkId + 1L
    val sql =
        """
        SELECT id FROM Collection
        WHERE ((updateDate > ?) OR ((updateDate = ?) AND (id >= ?)))
        AND (status IN ('Published', 'Archived'))
        ORDER BY updateDate ASC, id ASC
        LIMIT ?
        """.trimIndent()
    val ts = Timestamp.valueOf(watermarkDate)
    val conn =
        TransactionManager.current().connection.connection
            as java.sql.Connection
    val ids =
        conn.prepareStatement(sql).use { ps ->
            ps.setTimestamp(1, ts)
            ps.setTimestamp(2, ts)
            ps.setLong(3, nextId)
            ps.setInt(4, limit)
            ps.executeQuery().use { rs ->
                val list = mutableListOf<Long>()
                while (rs.next()) list.add(rs.getLong(1))
                list
            }
        }
    if (ids.isEmpty()) return emptyList()
    return ids.mapNotNull { id ->
        CollectionDao.findById(EntityID(id, CollectionTable)).also { dao ->
            if (dao == null) {
                log.warn(
                    "Collection id={} from cursor query missing on load",
                    id,
                )
            }
        }
    }
}

fun findCollectionsUpdatedSince(
    watermarkDate: LocalDateTime?,
    watermarkId: Long?,
    limit: Int,
): List<CollectionDao> =
    when {
        watermarkDate != null && watermarkId != null -> {
            findCollectionsUpdatedSinceRaw(
                watermarkDate,
                watermarkId,
                limit,
            )
        }

        else -> {
            CollectionDao
                .wrapRows(
                    when {
                        watermarkDate == null -> {
                            CollectionTable.select {
                                CollectionTable.status inList
                                    listOf(
                                        PublishStatus.Published,
                                        PublishStatus.Archived,
                                    )
                            }
                        }

                        else -> {
                            CollectionTable.select {
                                (
                                    CollectionTable.updateDate
                                        greater watermarkDate
                                ) and
                                    (
                                        CollectionTable.status inList
                                            listOf(
                                                PublishStatus.Published,
                                                PublishStatus.Archived,
                                            )
                                    )
                            }
                        }
                    }.orderBy(
                        CollectionTable.updateDate to SortOrder.ASC,
                        CollectionTable.id to SortOrder.ASC,
                    ),
                ).limit(limit, 0)
                .toList()
        }
    }

fun findAllPublishedOrArchivedSkillUuids(): List<String> =
    RichSkillDescriptorTable
        .select {
            RichSkillDescriptorTable.publishDate.isNotNull()
        }.map { it[RichSkillDescriptorTable.uuid] }

fun findAllPublishedOrArchivedCollectionUuids(): List<String> =
    CollectionTable
        .select {
            CollectionTable.status inList
                listOf(PublishStatus.Published, PublishStatus.Archived)
        }.map { it[CollectionTable.uuid] }
