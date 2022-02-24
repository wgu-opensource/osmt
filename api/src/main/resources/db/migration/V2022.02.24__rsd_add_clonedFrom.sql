USE osmt_db;

ALTER TABLE `RichSkillDescriptor`
    ADD COLUMN `cloned_from` varchar(768)
;
