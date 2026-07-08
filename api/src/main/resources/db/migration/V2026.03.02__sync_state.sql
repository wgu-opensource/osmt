USE osmt_db;

CREATE TABLE IF NOT EXISTS `SyncState` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `sync_type` VARCHAR(64) NOT NULL,
    `sync_key` VARCHAR(64) NOT NULL,
    `record_type` VARCHAR(64) NOT NULL,
    `sync_watermark` DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sync_state` (`sync_type`, `sync_key`, `record_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
