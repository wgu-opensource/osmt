USE osmt_db;

SET NAMES utf8mb3;
SET character_set_client = utf8mb3;

--
-- Table structure for table `AuditLog`
--

CREATE TABLE `AuditLog`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
    `creationDate`  datetime(6)  NOT NULL,
    `user`          varchar(256) NOT NULL,
    `operationType` varchar(128) NOT NULL,
    `tableName`     varchar(128) NOT NULL,
    `entityId`      bigint(20)   NOT NULL,
    `changedFields` text         NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 5;

--
-- Table structure for table `JobCode`
--

CREATE TABLE `JobCode`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT,
    `creationDate` datetime(6)  NOT NULL,
    `updateDate`   datetime(6)  NOT NULL,
    `code`         varchar(128) NOT NULL,
    `name`         varchar(1024) DEFAULT NULL,
    `major`        varchar(1024) DEFAULT NULL,
    `minor`        varchar(1024) DEFAULT NULL,
    `broad`        varchar(1024) DEFAULT NULL,
    `detailed`     varchar(1024) DEFAULT NULL,
    `description`  text,
    `framework`    varchar(1024) DEFAULT NULL,
    `url`          varchar(1024) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_JobCode_code` (`code`)
) ENGINE = InnoDB;

--
-- Table structure for table `Keyword`
--

CREATE TABLE `Keyword`
(
    `id`                bigint(20)                                                                             NOT NULL AUTO_INCREMENT,
    `creationDate`      datetime(6)                                                                            NOT NULL,
    `updateDate`        datetime(6)                                                                            NOT NULL,
    `value`             varchar(768),
    `uri`               varchar(768),
    `keyword_type_enum` enum ('Category','Keyword','Standard','Certification','Alignment','Employer','Author') NOT NULL,
    KEY `idx_Keyword_value` (`keyword_type_enum`, `value`),
    KEY `idx_Keyword_uri` (`keyword_type_enum`, `uri`),

    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `PublishStatus`
(
    `id`   bigint(20)  NOT NULL,
    `name` varchar(64) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1;

--
-- Dumping data for table `PublishStatus`
--

LOCK TABLES `PublishStatus` WRITE;
/*!40000 ALTER TABLE `PublishStatus`
    DISABLE KEYS */;
INSERT INTO `PublishStatus`
VALUES (0, 'Unpublished'),
       (1, 'Published'),
       (2, 'Archived');
/*!40000 ALTER TABLE `PublishStatus`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RichSkillDescriptor`xf
--

CREATE TABLE `RichSkillDescriptor`
(
    `id`                bigint(20)  NOT NULL AUTO_INCREMENT,
    `creationDate`      datetime(6) NOT NULL,
    `updateDate`        datetime(6) NOT NULL,
    `uuid`              varchar(36) NOT NULL,
    `name`              text        NOT NULL,
    `statement`         text        NOT NULL,
    `cat_id`            bigint(20) DEFAULT NULL,
    `publish_status_id` bigint(20)  NOT NULL,
    `author_id`         bigint(20) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `RichSkillDescriptor_uuid_unique` (`uuid`),
    KEY `fk_RichSkillDescriptor_cat_id_id` (`cat_id`),
    KEY `fk_RichSkillDescriptor_author_id_id` (`author_id`),
    KEY `fk_RichSkillDescriptor_publish_status_id_id` (`publish_status_id`),
    CONSTRAINT `fk_RichSkillDescriptor_cat_id_id` FOREIGN KEY (`cat_id`) REFERENCES `Keyword` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_RichSkillDescriptor_author_id_id` FOREIGN KEY (`author_id`) REFERENCES `Keyword` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_RichSkillDescriptor_publish_status_id_id` FOREIGN KEY (`publish_status_id`) REFERENCES `PublishStatus` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3;


--
-- Table structure for table `RichSkillJobCodes`
--

CREATE TABLE `RichSkillJobCodes`
(
    `richskill_id` bigint(20) NOT NULL,
    `jobcode_id`   bigint(20) NOT NULL,
    PRIMARY KEY (`richskill_id`, `jobcode_id`),
    KEY `fk_RichSkillKeywords_richskill_id_id` (`richskill_id`),
    KEY `fk_RichSkillJobCodes_jobcode_id_id` (`jobcode_id`),
    CONSTRAINT `fk_RichSkillJobCodes_jobcode_id_id` FOREIGN KEY (`jobcode_id`) REFERENCES `JobCode` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_RichSkillJobCodes_richskill_id_id` FOREIGN KEY (`richskill_id`) REFERENCES `RichSkillDescriptor` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

--
-- Table structure for table `RichSkillKeywords`
--

CREATE TABLE `RichSkillKeywords`
(
    `richskill_id` bigint(20) NOT NULL,
    `keyword_id`   bigint(20) NOT NULL,
    PRIMARY KEY (`richskill_id`, `keyword_id`),
    KEY `fk_RichSkillKeywords_richskill_id_id` (`richskill_id`),
    KEY `fk_RichSkillKeywords_keyword_id_id` (`keyword_id`),
    CONSTRAINT `fk_RichSkillKeywords_keyword_id_id` FOREIGN KEY (`keyword_id`) REFERENCES `Keyword` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_RichSkillKeywords_richskill_id_id` FOREIGN KEY (`richskill_id`) REFERENCES `RichSkillDescriptor` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `Collection`
(
    `id`           BIGINT(20) AUTO_INCREMENT PRIMARY KEY,
    `creationDate` DATETIME(6) NOT NULL,
    `updateDate`   DATETIME(6) NOT NULL,
    `uuid`         VARCHAR(36) NOT NULL,
    `name`         TEXT        NOT NULL,
    `author_id`    BIGINT      NULL,
    CONSTRAINT fk_Collection_author_id_id
        FOREIGN KEY (author_id) REFERENCES Keyword (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS CollectionSkills
(
    collection_id BIGINT(20) NOT NULL,
    skill_id      BIGINT(20) NOT NULL,
    CONSTRAINT PK_CollectionSkills_c_rs PRIMARY KEY (collection_id, skill_id),
    CONSTRAINT fk_CollectionSkills_collection_id_id FOREIGN KEY (collection_id) REFERENCES Collection (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_CollectionSkills_skill_id_id FOREIGN KEY (skill_id) REFERENCES RichSkillDescriptor (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;
CREATE INDEX CollectionSkills_collection_id ON CollectionSkills (collection_id);
CREATE INDEX CollectionSkills_skill_id ON CollectionSkills (skill_id);
