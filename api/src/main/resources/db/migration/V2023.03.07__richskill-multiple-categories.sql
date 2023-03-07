USE osmt_db;

--
-- Migration of RichSkillDescriptor.cat_id to RichSkillKeywords table.
--

INSERT INTO RichSkillKeywords (richskill_id, keyword_id)
SELECT id, cat_id
FROM RichSkillDescriptor
WHERE RichSkillDescriptor.cat_id is not null;

--
-- Removal of 'cat_id' from RichSkillDescriptor table.
--

ALTER TABLE `RichSkillDescriptor`
    DROP FOREIGN KEY `fk_RichSkillDescriptor_cat_id_id`,
    DROP COLUMN `cat_id`
;
