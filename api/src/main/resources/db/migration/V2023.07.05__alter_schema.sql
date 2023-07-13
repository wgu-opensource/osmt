USE osmt_db;

ALTER SCHEMA osmt_db
DEFAULT CHARACTER SET = utf8mb4;

ALTER SCHEMA osmt_db
DEFAULT COLLATE = utf8mb4_unicode_ci;

-- AuditLog: table
ALTER TABLE `AuditLog`
    DEFAULT CHARSET=utf8mb4,
    MODIFY `user` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `operationType` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `tableName` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `changedFields` text COLLATE utf8mb4_unicode_ci NOT NULL;

-- Collection: table
ALTER TABLE `Collection`
    DEFAULT CHARSET=utf8mb4,
    MODIFY `uuid` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `name` text COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `workspace_owner` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
    MODIFY `status` enum('unarchived','deleted','workspace','published','archived','draft') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft',
    MODIFY `description` text COLLATE utf8mb4_unicode_ci;

-- CollectionSkills: table
ALTER TABLE `CollectionSkills`
    DEFAULT CHARSET=utf8mb4,
    DEFAULT COLLATE = utf8mb4_unicode_ci;

-- JobCode: table
ALTER TABLE `JobCode`
    DEFAULT CHARSET=utf8mb4,
    MODIFY `code` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `name` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    MODIFY `major` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    MODIFY `minor` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    MODIFY `broad` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    MODIFY `detailed` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    MODIFY `description` text COLLATE utf8mb4_unicode_ci,
    MODIFY `framework` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    MODIFY `url` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL;

-- Keyword: table
ALTER TABLE `Keyword`
    DEFAULT CHARSET=utf8mb4,
    MODIFY `value` varchar(767) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    MODIFY `uri` varchar(767) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    MODIFY `keyword_type_enum` enum('Category','Keyword','Standard','Certification','Alignment','Employer','Author') COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `framework` varchar(767) COLLATE utf8mb4_unicode_ci DEFAULT NULL;

-- RichSkillDescriptor: table
ALTER TABLE `RichSkillDescriptor`
    DEFAULT CHARSET=utf8mb4,
    MODIFY `uuid` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `name` text COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `statement` text COLLATE utf8mb4_unicode_ci NOT NULL;

-- RichSkillJobCodes: table
ALTER TABLE `RichSkillJobCodes`
    DEFAULT CHARSET=utf8mb4,
    DEFAULT COLLATE=utf8mb4_unicode_ci;

-- RichSkillKeywords: table
ALTER TABLE `RichSkillKeywords`
    DEFAULT CHARSET=utf8mb4,
    DEFAULT COLLATE=utf8mb4_unicode_ci;

-- flyway_schema_history: table
ALTER TABLE `flyway_schema_history`
    DEFAULT CHARSET=utf8mb4,
    MODIFY `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    MODIFY `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
    MODIFY `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL;
