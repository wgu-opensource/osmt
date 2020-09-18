USE osmt_db;

ALTER TABLE `RichSkillDescriptor`
DROP FOREIGN KEY `fk_RichSkillDescriptor_publish_status_id_id`,
ADD COLUMN `archiveDate` datetime(6) null,
ADD COLUMN `publishDate` datetime(6) null;

DROP TABLE IF EXISTS `PublishStatus`;

ALTER TABLE `RichSkillDescriptor` DROP COLUMN publish_status_id;

ALTER TABLE `Collection`
ADD COLUMN `archiveDate` datetime(6) null,
ADD COLUMN `publishDate` datetime(6) null;


