USE osmt_db;

-- Backfill last_record_id for sync state rows that have watermark but NULL last_record_id.
-- Uses max id among records with updateDate = watermark (exact match) so we don't overshoot.
UPDATE SyncState ss
SET ss.last_record_id = (
    SELECT MAX(id) FROM RichSkillDescriptor
    WHERE publishDate IS NOT NULL AND updateDate = ss.sync_watermark
)
WHERE ss.sync_type = 'credential-engine'
  AND ss.sync_key = 'default'
  AND ss.record_type = 'skill'
  AND ss.sync_watermark IS NOT NULL
  AND ss.last_record_id IS NULL;

UPDATE SyncState ss
SET ss.last_record_id = (
    SELECT MAX(id) FROM Collection
    WHERE status IN ('Published', 'Archived') AND updateDate = ss.sync_watermark
)
WHERE ss.sync_type = 'credential-engine'
  AND ss.sync_key = 'default'
  AND ss.record_type = 'collection'
  AND ss.sync_watermark IS NOT NULL
  AND ss.last_record_id IS NULL;
