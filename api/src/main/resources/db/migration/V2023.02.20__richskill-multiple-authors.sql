USE osmt_db;

--
-- Migration of RichSkillDescriptor.author_id to RichSkillKeywords table.
--

INSERT INTO RichSkillKeywords (richskill_id, keyword_id)
SELECT id, author_id
FROM RichSkillDescriptor
WHERE RichSkillDescriptor.author_id is not null;

--
-- Removal of 'author_id' from RichSkillDescriptor table.
--

ALTER TABLE `RichSkillDescriptor`
    DROP FOREIGN KEY `fk_RichSkillDescriptor_author_id_id`,
    DROP COLUMN `author_id`
;
