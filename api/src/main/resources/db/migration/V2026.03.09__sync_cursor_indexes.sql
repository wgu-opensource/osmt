USE osmt_db;

CREATE INDEX idx_rsd_update_date_id
    ON RichSkillDescriptor (updateDate, id);

CREATE INDEX idx_collection_update_date_id
    ON Collection (updateDate, id);
