USE osmt_db;

ALTER TABLE `SyncState`
    ADD COLUMN `last_record_id` BIGINT NULL AFTER `sync_watermark`;
