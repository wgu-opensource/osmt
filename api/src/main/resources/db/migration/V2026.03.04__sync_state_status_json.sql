USE osmt_db;

ALTER TABLE `SyncState`
ADD COLUMN `status_json` TEXT NULL
COMMENT 'Job-level sync status: last record, error, correlation ID, progress';
