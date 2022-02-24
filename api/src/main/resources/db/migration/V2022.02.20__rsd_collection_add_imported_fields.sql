USE osmt_db;

ALTER TABLE `RichSkillDescriptor`
    ADD COLUMN `imported_from` varchar(768),
    ADD COLUMN `library_name` text
;

ALTER TABLE `Collection`
    ADD COLUMN `imported_from` varchar(768),
    ADD COLUMN `library_name` text
;