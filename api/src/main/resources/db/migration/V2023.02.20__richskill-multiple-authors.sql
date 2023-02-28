USE osmt_db;

--
-- Table structure for table `RichSkillAuthors`
--

CREATE TABLE IF NOT EXISTS `RichSkillAuthors`
(
    `richskill_id` bigint(20) NOT NULL,
    `author_id`   bigint(20) NOT NULL,
    PRIMARY KEY (`richskill_id`, `author_id`),
    KEY `fk_RichSkillAuthors_richskill_id_id` (`richskill_id`),
    KEY `fk_RichSkillAuthors_keyword_id_id` (`author_id`),
    CONSTRAINT `fk_RichSkillAuthors_richskill_id_id` FOREIGN KEY (`richskill_id`) REFERENCES `RichSkillDescriptor` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_RichSkillAuthors_keyword_id_id` FOREIGN KEY (`author_id`) REFERENCES `Keyword` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

--
-- Migration of RichSkillDescriptor.author_id to RichSkillAuthors table.
--

INSERT INTO RichSkillAuthors (richskill_id, author_id)
SELECT id, author_id
FROM RichSkillDescriptor;

--
-- Removal of 'author_id' from RichSkillDescriptor table.
--

ALTER TABLE `RichSkillDescriptor`
    DROP FOREIGN KEY `fk_RichSkillDescriptor_author_id_id`,
    DROP COLUMN `author_id`
;
