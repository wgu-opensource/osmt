USE osmt_db;

--
-- Table structure for table `RichSkillCategories`
--

CREATE TABLE IF NOT EXISTS `RichSkillCategories`
(
    `richskill_id` bigint(20) NOT NULL,
    `category_id`   bigint(20) NOT NULL,
    PRIMARY KEY (`richskill_id`, `category_id`),
    KEY `fk_RichSkillCategories_richskill_id_id` (`richskill_id`),
    KEY `fk_RichSkillCategories_keyword_id_id` (`category_id`),
    CONSTRAINT `fk_RichSkillCategories_richskill_id_id` FOREIGN KEY (`richskill_id`) REFERENCES `RichSkillDescriptor` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_RichSkillCategories_keyword_id_id` FOREIGN KEY (`category_id`) REFERENCES `Keyword` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

--
-- Migration of RichSkillDescriptor.cat_id to RichSkillCategories table.
--

INSERT INTO RichSkillCategories (richskill_id, category_id)
SELECT id, cat_id
FROM RichSkillDescriptor
WHERE cat_id is not null;

--
-- Removal of 'cat_id' from RichSkillDescriptor table.
--

ALTER TABLE `RichSkillDescriptor`
    DROP FOREIGN KEY `fk_RichSkillDescriptor_cat_id_id`,
    DROP COLUMN `cat_id`
;
