package edu.wgu.osmt.credentialengine

import edu.wgu.osmt.RoutePaths
import edu.wgu.osmt.config.AppConfig
import edu.wgu.osmt.security.OAuthHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class SyncStateResponse(
    val integrations: List<SyncIntegrationDto>,
    val allowUnpublishAll: Boolean = false,
)

data class SyncIntegrationDto(
    val syncKey: String,
    val recordType: String,
    val syncWatermark: String?,
    val statusJson: String? = null,
    val pendingCount: Int? = null,
)

@Controller
@RequestMapping("${RoutePaths.API}")
class SyncController
    @Autowired
    constructor(
        private val syncService: SyncService,
        private val appConfig: AppConfig,
        private val oAuthHelper: OAuthHelper,
    ) {
        private val log = LoggerFactory.getLogger(SyncController::class.java)
        private val syncInProgress = AtomicBoolean(false)
        private val syncExecutor =
            Executors.newSingleThreadExecutor { r ->
                Thread(r, "ce-sync").apply { isDaemon = true }
            }

        private fun ensureAdmin() {
            if (!oAuthHelper.hasRole(appConfig.roleAdmin)) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
            }
        }

        private fun ensureCuratorOrAdmin() {
            if (!oAuthHelper.hasRole(appConfig.roleAdmin) &&
                !oAuthHelper.hasRole(appConfig.roleCurator)
            ) {
                throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
            }
        }

        private fun ensureConfigured() {
            if (!syncService.isConfigured()) {
                throw ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Credential Engine sync is not configured",
                )
            }
        }

        @GetMapping(RoutePaths.SYNC_STATE)
        fun getSyncState(): ResponseEntity<SyncStateResponse> {
            ensureAdmin()
            ensureConfigured()
            val states = syncService.getSyncState()
            val integrations =
                states.map {
                    val inProgress =
                        parseSyncStatusJson(it.statusJson)?.inProgress == true
                    val pendingCount =
                        if (inProgress) {
                            null
                        } else {
                            syncService.getPendingCount(it.syncKey, it.recordType)
                        }
                    SyncIntegrationDto(
                        syncKey = it.syncKey,
                        recordType = it.recordType,
                        syncWatermark = it.syncWatermark?.toString(),
                        statusJson = it.statusJson,
                        pendingCount = pendingCount,
                    )
                }
            return ResponseEntity.ok(
                SyncStateResponse(
                    integrations = integrations,
                    allowUnpublishAll = syncService.isUnpublishAllAllowed(),
                ),
            )
        }

        @PostMapping(RoutePaths.SYNC_SKILL_UUID)
        fun syncSkill(
            @PathVariable uuid: String,
        ): ResponseEntity<Unit> {
            ensureCuratorOrAdmin()
            ensureConfigured()
            return syncService
                .syncRecord(SyncRecordType.SKILL, uuid)
                .fold(
                    onSuccess = { ResponseEntity.ok().build() },
                    onFailure = { e ->
                        when (e) {
                            is NoSuchElementException -> {
                                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
                            }

                            is IllegalArgumentException -> {
                                throw ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    e.message,
                                )
                            }

                            else -> {
                                throw ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    e.message,
                                )
                            }
                        }
                    },
                )
        }

        @PostMapping(RoutePaths.SYNC_COLLECTION_UUID)
        fun syncCollection(
            @PathVariable uuid: String,
        ): ResponseEntity<Unit> {
            ensureCuratorOrAdmin()
            ensureConfigured()
            return syncService
                .syncRecord(SyncRecordType.COLLECTION, uuid)
                .fold(
                    onSuccess = { ResponseEntity.ok().build() },
                    onFailure = { e ->
                        when (e) {
                            is NoSuchElementException -> {
                                throw ResponseStatusException(HttpStatus.NOT_FOUND, e.message)
                            }

                            is IllegalArgumentException -> {
                                throw ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    e.message,
                                )
                            }

                            else -> {
                                throw ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    e.message,
                                )
                            }
                        }
                    },
                )
        }

        @PostMapping(RoutePaths.SYNC_ALL)
        fun syncAll(): ResponseEntity<String> {
            ensureAdmin()
            ensureConfigured()
            if (!syncInProgress.compareAndSet(false, true)) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sync already in progress",
                )
            }
            val correlationId = syncService.markSyncInProgress("default")
            syncExecutor.submit {
                try {
                    syncService.syncAllSinceWatermark("default", correlationId)
                } catch (e: Throwable) {
                    log.error(
                        "[{}] Sync all failed with exception",
                        correlationId,
                        e,
                    )
                } finally {
                    syncInProgress.set(false)
                }
            }
            return ResponseEntity(
                "Sync started. Check logs for progress.",
                HttpStatus.ACCEPTED,
            )
        }

        @PostMapping(RoutePaths.SYNC_RESYNC)
        fun resyncAll(): ResponseEntity<String> {
            ensureAdmin()
            ensureConfigured()
            if (!syncInProgress.compareAndSet(false, true)) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sync already in progress",
                )
            }
            val correlationId = syncService.markSyncInProgress("default")
            syncExecutor.submit {
                try {
                    syncService.resyncAll("default", correlationId)
                } catch (e: Throwable) {
                    log.error(
                        "[{}] Resync failed with exception",
                        correlationId,
                        e,
                    )
                } finally {
                    syncInProgress.set(false)
                }
            }
            return ResponseEntity(
                "Full resync started. Check logs for progress.",
                HttpStatus.ACCEPTED,
            )
        }

        @PostMapping(RoutePaths.SYNC_UNPUBLISH_ALL)
        fun unpublishAll(): ResponseEntity<String> {
            ensureAdmin()
            ensureConfigured()
            if (!syncService.isUnpublishAllAllowed()) {
                throw ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unpublish all is not enabled for this environment",
                )
            }
            if (!syncInProgress.compareAndSet(false, true)) {
                throw ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Sync or unpublish already in progress",
                )
            }
            val correlationId = syncService.markSyncInProgress("default")
            syncExecutor.submit {
                try {
                    syncService.unpublishAll("default")
                } catch (e: Throwable) {
                    log.error(
                        "[{}] Unpublish failed with exception",
                        correlationId,
                        e,
                    )
                } finally {
                    syncInProgress.set(false)
                }
            }
            return ResponseEntity(
                "Unpublish started. Check logs for progress.",
                HttpStatus.ACCEPTED,
            )
        }
    }
