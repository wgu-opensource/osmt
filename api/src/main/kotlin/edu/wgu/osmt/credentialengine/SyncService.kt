package edu.wgu.osmt.credentialengine

import edu.wgu.osmt.collection.Collection
import edu.wgu.osmt.collection.CollectionDao
import edu.wgu.osmt.collection.CollectionRepository
import edu.wgu.osmt.db.PublishStatus
import edu.wgu.osmt.richskill.RichSkillDescriptor
import edu.wgu.osmt.richskill.RichSkillDescriptorDao
import edu.wgu.osmt.richskill.RichSkillRepository
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional

private const val SYNC_TYPE = "credential-engine"
private const val SYNC_KEY_DEFAULT = "default"
private const val MDC_CORRELATION_ID = "syncCorrelationId"

private val log = LoggerFactory.getLogger(SyncService::class.java)

private data class SyncBatchParams<T>(
    val fetchBatch: (LocalDateTime?, Long?, Int) -> List<T>,
    val itemId: (T) -> Long,
    val itemUuid: (T) -> String,
    val itemUpdateDate: (T) -> LocalDateTime,
    val processBatch: (
        SyncTarget,
        List<T>,
        String,
        String,
        Int,
        String,
    ) -> Result<Unit>,
    val recordLabel: String,
)

@Service
class SyncService(
    private val syncTargetOpt: Optional<SyncTarget>,
    private val ctidGeneratorOpt: Optional<CtidGenerator>,
    private val syncStateRepository: SyncStateRepository,
    private val richSkillRepository: RichSkillRepository,
    private val collectionRepository: CollectionRepository,
    private val syncRetryHelper: SyncRetryHelper,
    transactionManager: PlatformTransactionManager,
    @Value("\${credential-engine.sync.batch-size:20}") private val batchSize: Int,
    @Value("\${credential-engine.sync.retry-attempts:5}") private val retryAttempts: Int,
    @Value("\${credential-engine.sync.retry-initial-delay-ms:5000}")
    private val retryInitialDelayMs: Long,
    @Value("\${credential-engine.sync.retry-delay-multiplier:1.5}")
    private val retryDelayMultiplier: Double,
    @Value("\${credential-engine.allow-unpublish-all:false}")
    private val allowUnpublishAll: Boolean,
) {
    private val txReadOnly =
        TransactionTemplate(transactionManager).apply {
            isReadOnly = true
        }
    private val txWrite = TransactionTemplate(transactionManager)

    fun syncRecord(
        recordType: String,
        uuid: String,
    ): Result<Unit> =
        syncTargetOpt
            .map { target -> doSyncRecord(target, recordType, uuid) }
            .orElse(Result.failure(IllegalStateException("Sync not configured")))

    private fun doSyncRecord(
        target: SyncTarget,
        recordType: String,
        uuid: String,
    ): Result<Unit> =
        when (recordType) {
            SyncRecordType.SKILL -> {
                syncSkill(target, uuid)
            }

            SyncRecordType.COLLECTION -> {
                syncCollection(target, uuid)
            }

            else -> {
                Result.failure(
                    IllegalArgumentException("Unknown recordType: $recordType"),
                )
            }
        }

    private fun syncSkill(
        target: SyncTarget,
        uuid: String,
    ): Result<Unit> {
        val rsd =
            txReadOnly.execute {
                richSkillRepository.findByUUID(uuid)?.toModel()
            } ?: return Result.failure(
                NoSuchElementException("Skill not found: $uuid"),
            )
        return publishSkillWithRetry(target, rsd)
    }

    private fun syncCollection(
        target: SyncTarget,
        uuid: String,
    ): Result<Unit> {
        val item =
            txReadOnly.execute {
                val dao =
                    collectionRepository.findByUUID(uuid)
                        ?: return@execute null
                CollectionItem(
                    dao.uuid,
                    dao.name,
                    dao.toModel(),
                    skillCtids(dao),
                )
            } ?: return Result.failure(
                NoSuchElementException("Collection not found: $uuid"),
            )
        return publishCollectionWithRetry(target, item.model, item.ctids)
    }

    private fun skillCtids(collectionDao: CollectionDao): List<String> =
        collectionDao.skills.map {
            ctidGeneratorOpt
                .map { g -> g.generate(it.uuid) }
                .orElse("ce-${it.uuid}")
        }

    /**
     * Marks both skill and collection integrations as in progress immediately.
     * Called before forking the sync job so GET /state returns inProgress=true
     * without waiting for the background job to start.
     * @return the correlation ID written to status, so callers can pass it to
     *   syncAllSinceWatermark/resyncAll for log continuity.
     */
    @Transactional
    fun markSyncInProgress(syncKey: String): String {
        val sessionCorrelationId = generateCorrelationId()
        for (recordType in listOf(SyncRecordType.SKILL, SyncRecordType.COLLECTION)) {
            syncStateRepository.getOrCreateRow(SYNC_TYPE, syncKey, recordType)
            syncStateRepository.updateStatusJson(
                SYNC_TYPE,
                syncKey,
                recordType,
                SyncStatusJson(
                    inProgress = true,
                    sessionCorrelationId = sessionCorrelationId,
                    lastUpdatedAt = nowIso(),
                ).toJsonString(),
            )
        }
        return sessionCorrelationId
    }

    fun syncSinceWatermark(
        syncKey: String,
        recordType: String,
    ): Result<Unit> {
        val sessionCorrelationId = generateCorrelationId()
        MDC.put(MDC_CORRELATION_ID, sessionCorrelationId)
        return try {
            syncTargetOpt
                .map { target ->
                    doSyncSinceWatermark(target, syncKey, recordType, sessionCorrelationId)
                }.orElse(Result.failure(IllegalStateException("Sync not configured")))
        } finally {
            MDC.remove(MDC_CORRELATION_ID)
        }
    }

    private fun doSyncSinceWatermark(
        target: SyncTarget,
        syncKey: String,
        recordType: String,
        sessionCorrelationId: String,
    ): Result<Unit> {
        val params =
            when (recordType) {
                SyncRecordType.SKILL -> {
                    SyncBatchParams(
                        fetchBatch = { d, id, limit ->
                            findSkillsUpdatedSince(d, id, limit)
                        },
                        itemId = { it.id.value },
                        itemUuid = { it.uuid },
                        itemUpdateDate = { it.updateDate },
                        processBatch = { t, b, k, r, i, s ->
                            processSkillBatch(t, b, k, r, i, s)
                        },
                        recordLabel = "skill",
                    )
                }

                SyncRecordType.COLLECTION -> {
                    SyncBatchParams(
                        fetchBatch = { d, id, limit ->
                            findCollectionsUpdatedSince(d, id, limit)
                        },
                        itemId = { it.id.value },
                        itemUuid = { it.uuid },
                        itemUpdateDate = { it.updateDate },
                        processBatch = { t, b, k, r, i, s ->
                            processCollectionBatch(t, b, k, r, i, s)
                        },
                        recordLabel = "collection",
                    )
                }

                else -> {
                    return Result.failure(IllegalArgumentException("Unknown: $recordType"))
                }
            }
        return syncBatchLoop(
            target,
            syncKey,
            recordType,
            sessionCorrelationId,
            params,
        )
    }

    private fun <T> syncBatchLoop(
        target: SyncTarget,
        syncKey: String,
        recordType: String,
        sessionCorrelationId: String,
        params: SyncBatchParams<T>,
    ): Result<Unit> {
        log.info(
            "[{}] Sync started recordType={} syncKey={}",
            sessionCorrelationId,
            recordType,
            syncKey,
        )
        syncStateRepository.getOrCreateRow(SYNC_TYPE, syncKey, recordType)
        syncStateRepository.updateStatusJson(
            SYNC_TYPE,
            syncKey,
            recordType,
            SyncStatusJson(
                inProgress = true,
                sessionCorrelationId = sessionCorrelationId,
                lastUpdatedAt = nowIso(),
            ).toJsonString(),
        )
        var watermarkDate = syncStateRepository.getWatermark(SYNC_TYPE, syncKey, recordType)
        var watermarkId = syncStateRepository.getLastRecordId(SYNC_TYPE, syncKey, recordType)
        var batchIndex = 0
        var consecutiveWatermarkRepeats = 0
        var lastMaxId: Long? = null

        while (true) {
            log.debug(
                "[{}] Batch {} fetch watermarkDate={} watermarkId={}",
                sessionCorrelationId,
                batchIndex,
                watermarkDate,
                watermarkId,
            )
            val batch =
                txReadOnly.execute {
                    params.fetchBatch(watermarkDate, watermarkId, batchSize)
                } ?: emptyList()
            if (batch.isEmpty()) break

            log.debug(
                "[{}] Batch {} raw from DB: size={} ids={} uuids={}",
                sessionCorrelationId,
                batchIndex,
                batch.size,
                batch.map { params.itemId(it) },
                batch.map { params.itemUuid(it) },
            )

            val wDate = watermarkDate
            val wId = watermarkId
            val cursorFilteredBatch =
                if (wDate != null && wId != null) {
                    batch.filter {
                        params.itemUpdateDate(it) > wDate ||
                            (
                                params.itemUpdateDate(it) == wDate &&
                                    params.itemId(it) > wId
                            )
                    }
                } else {
                    batch
                }
            if (cursorFilteredBatch.size < batch.size) {
                log.warn(
                    "[{}] Batch {} excluded {} records that were <= watermark " +
                        "(Exposed query bug workaround)",
                    sessionCorrelationId,
                    batchIndex,
                    batch.size - cursorFilteredBatch.size,
                )
            }
            if (cursorFilteredBatch.isEmpty()) {
                val m =
                    batch.maxWithOrNull(
                        compareBy<T> { params.itemUpdateDate(it) }.thenBy { params.itemId(it) },
                    )!!
                val maxD = params.itemUpdateDate(m)
                val maxI = params.itemId(m)
                if (lastMaxId == maxI) {
                    consecutiveWatermarkRepeats++
                    if (consecutiveWatermarkRepeats >= 3) {
                        log.error(
                            "[{}] Circuit breaker: watermark stuck at id={} for 3+ batches. " +
                                "Exposed id comparison may be broken. Sync aborted.",
                            sessionCorrelationId,
                            maxI,
                        )
                        return Result.failure(
                            IllegalStateException(
                                "Sync cursor stuck at id=$maxI - Exposed query returns " +
                                    "same record repeatedly. See diagnostic doc.",
                            ),
                        )
                    }
                } else {
                    consecutiveWatermarkRepeats = 0
                }
                lastMaxId = maxI
                syncStateRepository.updateWatermark(SYNC_TYPE, syncKey, recordType, maxD, maxI)
                watermarkDate = maxD
                watermarkId = maxI
                batchIndex++
                continue
            }
            consecutiveWatermarkRepeats = 0

            val seen = mutableSetOf<String>()
            val dedupedBatch = cursorFilteredBatch.filter { seen.add(params.itemUuid(it)) }
            if (dedupedBatch.size < cursorFilteredBatch.size) {
                log.warn(
                    "[{}] Batch {} dropped {} duplicate {}s",
                    sessionCorrelationId,
                    batchIndex,
                    cursorFilteredBatch.size - dedupedBatch.size,
                    params.recordLabel,
                )
            }

            val result =
                params.processBatch(
                    target,
                    dedupedBatch,
                    syncKey,
                    recordType,
                    batchIndex,
                    sessionCorrelationId,
                )
            result.fold(
                onSuccess = { },
                onFailure = { return Result.failure(it) },
            )

            val maxByDate =
                dedupedBatch.maxWithOrNull(
                    compareBy<T> { params.itemUpdateDate(it) }.thenBy { params.itemId(it) },
                )!!
            val maxDate = params.itemUpdateDate(maxByDate)
            val maxId = params.itemId(maxByDate)
            syncStateRepository.updateWatermark(
                SYNC_TYPE,
                syncKey,
                recordType,
                maxDate,
                maxId,
            )
            log.debug(
                "[{}] Batch {} done, watermark advanced to date={} id={}",
                sessionCorrelationId,
                batchIndex,
                maxDate,
                maxId,
            )
            watermarkDate = maxDate
            watermarkId = maxId
            batchIndex++
        }
        syncStateRepository.updateStatusJson(
            SYNC_TYPE,
            syncKey,
            recordType,
            SyncStatusJson(
                inProgress = false,
                batchesCompleted = batchIndex,
                sessionCorrelationId = sessionCorrelationId,
                lastUpdatedAt = nowIso(),
            ).toJsonString(),
        )
        log.info("[{}] Sync completed recordType={}", sessionCorrelationId, recordType)
        return Result.success(Unit)
    }

    private data class SkillItem(
        val uuid: String,
        val name: String,
        val model: RichSkillDescriptor,
    )

    private fun processSkillBatch(
        target: SyncTarget,
        batch: List<RichSkillDescriptorDao>,
        syncKey: String,
        recordType: String,
        batchIndex: Int,
        sessionCorrelationId: String,
    ): Result<Unit> {
        val items =
            txReadOnly.execute {
                batch.map { dao ->
                    SkillItem(dao.uuid, dao.name, dao.toModel())
                }
            } ?: return Result.success(Unit)

        var last: SkillItem? = null
        for (item in items) {
            log.debug(
                "[{}] Processing skill uuid={} batch={}",
                sessionCorrelationId,
                item.uuid,
                batchIndex,
            )
            last = item
            val result = publishSkillWithRetry(target, item.model)
            if (result.isFailure) {
                val err = result.exceptionOrNull()
                log.error(
                    "[{}] Sync failed skill={} batch={} error={}",
                    sessionCorrelationId,
                    item.uuid,
                    batchIndex,
                    err?.message,
                )
                syncStateRepository.updateStatusJson(
                    SYNC_TYPE,
                    syncKey,
                    recordType,
                    SyncStatusJson(
                        lastRecordUuid = item.uuid,
                        lastRecordName = item.name,
                        batchIndex = batchIndex,
                        batchesCompleted = batchIndex,
                        lastUpdatedAt = nowIso(),
                        sessionCorrelationId = sessionCorrelationId,
                        inProgress = false,
                        error =
                            SyncStatusError(
                                message =
                                    err?.message ?: "Unknown error",
                                correlationId =
                                    generateCorrelationId(),
                                recordUuid = item.uuid,
                                recordName = item.name,
                                occurredAt = nowIso(),
                            ),
                    ).toJsonString(),
                )
                return Result.failure(
                    err ?: IllegalStateException("Sync failed"),
                )
            }
        }
        last?.let { s ->
            syncStateRepository.updateStatusJson(
                SYNC_TYPE,
                syncKey,
                recordType,
                SyncStatusJson(
                    lastRecordUuid = s.uuid,
                    lastRecordName = s.name,
                    batchIndex = batchIndex,
                    batchesCompleted = batchIndex,
                    lastUpdatedAt = nowIso(),
                    sessionCorrelationId = sessionCorrelationId,
                    inProgress = true,
                ).toJsonString(),
            )
        }
        return Result.success(Unit)
    }

    private data class CollectionItem(
        val uuid: String,
        val name: String,
        val model: Collection,
        val ctids: List<String>,
    )

    private fun processCollectionBatch(
        target: SyncTarget,
        batch: List<CollectionDao>,
        syncKey: String,
        recordType: String,
        batchIndex: Int,
        sessionCorrelationId: String,
    ): Result<Unit> {
        val items =
            txReadOnly.execute {
                batch.map { dao ->
                    CollectionItem(
                        dao.uuid,
                        dao.name,
                        dao.toModel(),
                        skillCtids(dao),
                    )
                }
            } ?: return Result.success(Unit)

        var last: CollectionItem? = null
        for (item in items) {
            log.debug(
                "[{}] Processing collection uuid={} batch={}",
                sessionCorrelationId,
                item.uuid,
                batchIndex,
            )
            last = item
            val result =
                publishCollectionWithRetry(target, item.model, item.ctids)
            if (result.isFailure) {
                val err = result.exceptionOrNull()
                log.error(
                    "[{}] Sync failed collection={} batch={} error={}",
                    sessionCorrelationId,
                    item.uuid,
                    batchIndex,
                    err?.message,
                )
                syncStateRepository.updateStatusJson(
                    SYNC_TYPE,
                    syncKey,
                    recordType,
                    SyncStatusJson(
                        lastRecordUuid = item.uuid,
                        lastRecordName = item.name,
                        batchIndex = batchIndex,
                        batchesCompleted = batchIndex,
                        lastUpdatedAt = nowIso(),
                        sessionCorrelationId = sessionCorrelationId,
                        inProgress = false,
                        error =
                            SyncStatusError(
                                message =
                                    err?.message ?: "Unknown error",
                                correlationId =
                                    generateCorrelationId(),
                                recordUuid = item.uuid,
                                recordName = item.name,
                                occurredAt = nowIso(),
                            ),
                    ).toJsonString(),
                )
                return Result.failure(
                    err ?: IllegalStateException("Sync failed"),
                )
            }
        }
        last?.let { c ->
            syncStateRepository.updateStatusJson(
                SYNC_TYPE,
                syncKey,
                recordType,
                SyncStatusJson(
                    lastRecordUuid = c.uuid,
                    lastRecordName = c.name,
                    batchIndex = batchIndex,
                    batchesCompleted = batchIndex,
                    lastUpdatedAt = nowIso(),
                    sessionCorrelationId = sessionCorrelationId,
                    inProgress = true,
                ).toJsonString(),
            )
        }
        return Result.success(Unit)
    }

    private fun publishSkillWithRetry(
        target: SyncTarget,
        rsd: RichSkillDescriptor,
    ): Result<Unit> {
        val status = rsd.publishStatus()
        return when (status) {
            PublishStatus.Published, PublishStatus.Archived -> {
                syncRetryHelper.withRetry(
                    retryAttempts,
                    retryInitialDelayMs,
                    retryDelayMultiplier,
                ) {
                    when (status) {
                        PublishStatus.Published -> target.publishSkill(rsd)
                        PublishStatus.Archived -> target.deprecateSkill(rsd)
                        else -> Result.success(Unit)
                    }
                }
            }

            else -> {
                log.debug(
                    "CE sync skipping skill uuid={} status={}",
                    rsd.uuid,
                    status,
                )
                Result.success(Unit)
            }
        }
    }

    private fun publishCollectionWithRetry(
        target: SyncTarget,
        collection: Collection,
        ctids: List<String>,
    ): Result<Unit> =
        syncRetryHelper.withRetry(
            retryAttempts,
            retryInitialDelayMs,
            retryDelayMultiplier,
        ) {
            when (collection.status) {
                PublishStatus.Published -> {
                    target.publishCollection(collection, ctids)
                }

                PublishStatus.Archived -> {
                    target.deprecateCollection(collection)
                }

                else -> {
                    Result.success(Unit)
                }
            }
        }

    private fun nowIso(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    fun syncAllSinceWatermark(
        syncKey: String = SYNC_KEY_DEFAULT,
        sessionCorrelationId: String? = null,
    ): Result<Unit> {
        val id = sessionCorrelationId ?: generateCorrelationId()
        MDC.put(MDC_CORRELATION_ID, id)
        return try {
            syncTargetOpt
                .map { target ->
                    log.info("[{}] Sync all started syncKey={}", id, syncKey)
                    doSyncSinceWatermark(
                        target,
                        syncKey,
                        SyncRecordType.SKILL,
                        id,
                    ).fold(
                        onSuccess = {
                            doSyncSinceWatermark(
                                target,
                                syncKey,
                                SyncRecordType.COLLECTION,
                                id,
                            )
                        },
                        onFailure = { e ->
                            log.error(
                                "[{}] Sync all failed at skills error={}",
                                id,
                                e.message,
                            )
                            Result.failure(e)
                        },
                    ).also { result ->
                        result
                            .onSuccess {
                                log.info(
                                    "[{}] Sync all completed syncKey={}",
                                    id,
                                    syncKey,
                                )
                            }.onFailure { e ->
                                log.error(
                                    "[{}] Sync all failed syncKey={} error={}",
                                    id,
                                    syncKey,
                                    e.message,
                                )
                            }
                    }
                }.orElse(Result.failure(IllegalStateException("Sync not configured")))
        } finally {
            MDC.remove(MDC_CORRELATION_ID)
        }
    }

    @Transactional(readOnly = true)
    fun getSyncState(syncKey: String = SYNC_KEY_DEFAULT): List<SyncState> =
        syncStateRepository.findAllBySyncKey(SYNC_TYPE, syncKey)

    @Transactional(readOnly = true)
    fun getPendingCount(
        syncKey: String,
        recordType: String,
    ): Int {
        val watermark =
            syncStateRepository.getWatermark(SYNC_TYPE, syncKey, recordType)
        val lastRecordId =
            syncStateRepository.getLastRecordId(SYNC_TYPE, syncKey, recordType)
        return when (recordType) {
            SyncRecordType.SKILL -> {
                countSkillsUpdatedSince(watermark, lastRecordId)
            }

            SyncRecordType.COLLECTION -> {
                countCollectionsUpdatedSince(watermark, lastRecordId)
            }

            else -> {
                0
            }
        }
    }

    fun isConfigured(): Boolean = syncTargetOpt.isPresent

    fun isUnpublishAllAllowed(): Boolean = allowUnpublishAll

    fun unpublishAll(syncKey: String = SYNC_KEY_DEFAULT): Result<Unit> {
        if (!allowUnpublishAll) {
            return Result.failure(
                IllegalStateException("Unpublish all is not enabled"),
            )
        }
        val sessionCorrelationId = generateCorrelationId()
        MDC.put(MDC_CORRELATION_ID, sessionCorrelationId)
        return try {
            syncTargetOpt
                .map { target ->
                    val (colUuids, skillUuids) =
                        txReadOnly.execute {
                            findAllPublishedOrArchivedCollectionUuids() to
                                findAllPublishedOrArchivedSkillUuids()
                        }!!
                    log.info(
                        "[{}] Unpublish all started collections={} skills={}",
                        sessionCorrelationId,
                        colUuids.size,
                        skillUuids.size,
                    )
                    val result = target.unpublishAll(colUuids, skillUuids)
                    result.fold(
                        onSuccess = {
                            syncStateRepository.resetWatermark(
                                SYNC_TYPE,
                                syncKey,
                                SyncRecordType.SKILL,
                            )
                            syncStateRepository.resetWatermark(
                                SYNC_TYPE,
                                syncKey,
                                SyncRecordType.COLLECTION,
                            )
                            markUnpublishComplete(syncKey, sessionCorrelationId)
                            log.info(
                                "[{}] Unpublish all completed",
                                sessionCorrelationId,
                            )
                        },
                        onFailure = { e ->
                            log.error(
                                "[{}] Unpublish all failed: {}",
                                sessionCorrelationId,
                                e.message,
                            )
                            markUnpublishAborted(
                                syncKey,
                                sessionCorrelationId,
                                e,
                            )
                        },
                    )
                    result
                }.orElse(
                    Result.failure(IllegalStateException("Sync not configured")),
                )
        } finally {
            MDC.remove(MDC_CORRELATION_ID)
        }
    }

    private fun markUnpublishComplete(
        syncKey: String,
        sessionCorrelationId: String,
    ) {
        for (rt in listOf(SyncRecordType.SKILL, SyncRecordType.COLLECTION)) {
            syncStateRepository.updateStatusJson(
                SYNC_TYPE,
                syncKey,
                rt,
                SyncStatusJson(
                    inProgress = false,
                    batchesCompleted = 0,
                    sessionCorrelationId = sessionCorrelationId,
                    lastUpdatedAt = nowIso(),
                ).toJsonString(),
            )
        }
    }

    private fun markUnpublishAborted(
        syncKey: String,
        sessionCorrelationId: String,
        cause: Throwable,
    ) {
        for (rt in listOf(SyncRecordType.SKILL, SyncRecordType.COLLECTION)) {
            syncStateRepository.updateStatusJson(
                SYNC_TYPE,
                syncKey,
                rt,
                SyncStatusJson(
                    inProgress = false,
                    sessionCorrelationId = sessionCorrelationId,
                    lastUpdatedAt = nowIso(),
                    error =
                        SyncStatusError(
                            message = cause.message ?: "Unpublish failed",
                            correlationId = sessionCorrelationId,
                            occurredAt = nowIso(),
                        ),
                ).toJsonString(),
            )
        }
    }

    fun resyncAll(
        syncKey: String = SYNC_KEY_DEFAULT,
        sessionCorrelationId: String? = null,
    ): Result<Unit> {
        syncStateRepository.resetWatermark(SYNC_TYPE, syncKey, SyncRecordType.SKILL)
        syncStateRepository.resetWatermark(SYNC_TYPE, syncKey, SyncRecordType.COLLECTION)
        return syncAllSinceWatermark(syncKey, sessionCorrelationId)
    }
}
