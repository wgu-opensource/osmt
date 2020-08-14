USE osmt_db;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
SET NAMES utf8;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

--
-- Table structure for table `AuditLog`
--

DROP TABLE IF EXISTS `AuditLog`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
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
  AUTO_INCREMENT = 5
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `JobCode`
--

DROP TABLE IF EXISTS `JobCode`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `JobCode`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT,
    `creationDate` datetime(6)  NOT NULL,
    `updateDate`   datetime(6)  NOT NULL,
    `code`         varchar(128) NOT NULL,
    `name`         varchar(128)  DEFAULT NULL,
    `description`  text,
    `source`       varchar(1024) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `JobCode`
--

LOCK TABLES `JobCode` WRITE;
/*!40000 ALTER TABLE `JobCode`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `JobCode`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `Keyword`
--

DROP TABLE IF EXISTS `Keyword`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `Keyword`
(
    `id`                bigint(20)                                                                                 NOT NULL AUTO_INCREMENT,
    `creationDate`      datetime(6)                                                                                NOT NULL,
    `updateDate`        datetime(6)                                                                                NOT NULL,
    `value`             varchar(1024)                                                                              NOT NULL,
    `uri`               text,
    `keyword_type_enum` enum ('Category','Certifications','Keyword','Other','ProfessionalStandards','Sel','Tools') NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `Keyword`
--

LOCK TABLES `Keyword` WRITE;
/*!40000 ALTER TABLE `Keyword`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `Keyword`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PublishStatus`
--

DROP TABLE IF EXISTS `PublishStatus`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `PublishStatus`
(
    `id`   bigint(20)  NOT NULL AUTO_INCREMENT,
    `name` varchar(64) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
-- Table structure for table `RichSkillDescriptor`
--

DROP TABLE IF EXISTS `RichSkillDescriptor`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `RichSkillDescriptor`
(
    `id`                bigint(20)  NOT NULL AUTO_INCREMENT,
    `creationDate`      datetime(6) NOT NULL,
    `updateDate`        datetime(6) NOT NULL,
    `uuid`              varchar(36) NOT NULL,
    `name`              text        NOT NULL,
    `statement`         text        NOT NULL,
    `cat_id`            bigint(20) DEFAULT NULL,
    `author`            text        NOT NULL,
    `publish_status_id` bigint(20)  NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `RichSkillDescriptor_uuid_unique` (`uuid`),
    KEY `fk_RichSkillDescriptor_cat_id_id` (`cat_id`),
    KEY `fk_RichSkillDescriptor_publish_status_id_id` (`publish_status_id`),
    CONSTRAINT `fk_RichSkillDescriptor_cat_id_id` FOREIGN KEY (`cat_id`) REFERENCES `Keyword` (`id`),
    CONSTRAINT `fk_RichSkillDescriptor_publish_status_id_id` FOREIGN KEY (`publish_status_id`) REFERENCES `PublishStatus` (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `RichSkillJobSkills`
--

DROP TABLE IF EXISTS `RichSkillJobSkills`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `RichSkillJobSkills`
(
    `id`           bigint(20) NOT NULL,
    `richskill_id` bigint(20) NOT NULL,
    `jobcode_id`   bigint(20) NOT NULL,
    PRIMARY KEY (`richskill_id`, `jobcode_id`),
    UNIQUE KEY `RichSkillJobSkills_id_unique` (`id`),
    KEY `fk_RichSkillJobSkills_jobcode_id_id` (`jobcode_id`),
    CONSTRAINT `fk_RichSkillJobSkills_jobcode_id_id` FOREIGN KEY (`jobcode_id`) REFERENCES `JobCode` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_RichSkillJobSkills_richskill_id_id` FOREIGN KEY (`richskill_id`) REFERENCES `RichSkillDescriptor` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RichSkillJobSkills`
--

LOCK TABLES `RichSkillJobSkills` WRITE;
/*!40000 ALTER TABLE `RichSkillJobSkills`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `RichSkillJobSkills`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RichSkillKeywords`
--

DROP TABLE IF EXISTS `RichSkillKeywords`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `RichSkillKeywords`
(
    `id`           bigint(20) NOT NULL,
    `richskill_id` bigint(20) NOT NULL,
    `keyword_id`   bigint(20) NOT NULL,
    UNIQUE KEY `RichSkillKeywords_id_unique` (`id`),
    KEY `fk_RichSkillKeywords_richskill_id_id` (`richskill_id`),
    KEY `fk_RichSkillKeywords_keyword_id_id` (`keyword_id`),
    CONSTRAINT `fk_RichSkillKeywords_keyword_id_id` FOREIGN KEY (`keyword_id`) REFERENCES `Keyword` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_RichSkillKeywords_richskill_id_id` FOREIGN KEY (`richskill_id`) REFERENCES `RichSkillDescriptor` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RichSkillKeywords`
--

LOCK TABLES `RichSkillKeywords` WRITE;
/*!40000 ALTER TABLE `RichSkillKeywords`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `RichSkillKeywords`
    ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
SET character_set_client = utf8mb4;
CREATE TABLE `flyway_schema_history`
(
    `installed_rank` int(11)       NOT NULL,
    `version`        varchar(50)            DEFAULT NULL,
    `description`    varchar(200)  NOT NULL,
    `type`           varchar(20)   NOT NULL,
    `script`         varchar(1000) NOT NULL,
    `checksum`       int(11)                DEFAULT NULL,
    `installed_by`   varchar(100)  NOT NULL,
    `installed_on`   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `execution_time` int(11)       NOT NULL,
    `success`        tinyint(1)    NOT NULL,
    PRIMARY KEY (`installed_rank`),
    KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history`
    DISABLE KEYS */;
/*!40000 ALTER TABLE `flyway_schema_history`
    ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2020-08-14 16:13:52
