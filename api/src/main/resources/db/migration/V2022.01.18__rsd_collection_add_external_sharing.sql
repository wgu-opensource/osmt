USE osmt_db;

ALTER TABLE `RichSkillDescriptor`
    ADD COLUMN `externally_shared` BOOLEAN NOT NULL
;

ALTER TABLE `Collection`
    ADD COLUMN `externally_shared` BOOLEAN NOT NULL
;