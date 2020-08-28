CREATE DATABASE IF NOT EXISTS osmt_db character set UTF8mb3 collate utf8mb3_unicode_ci;

USE osmt_db;

DROP TABLE IF EXISTS `Keyword`;
CREATE TABLE `Keyword`
(
    `id`                bigint(20)                                                                                 NOT NULL AUTO_INCREMENT,
    `creationDate`      datetime(6)                                                                                NOT NULL,
    `updateDate`        datetime(6)                                                                                NOT NULL,
    `value`             varchar(1024)                                                                              NOT NULL,
    `uri`               text,
    `keyword_type_enum` enum ('Category','Certifications','Keyword','Other','ProfessionalStandards','Sel','Tools') NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;
