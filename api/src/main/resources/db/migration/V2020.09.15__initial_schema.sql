-- Initial schema for OSMT (PostgreSQL/YugabyteDB compatible)

--
-- Table structure for table "AuditLog"
--

CREATE TABLE IF NOT EXISTS "AuditLog"
(
    id            BIGSERIAL PRIMARY KEY,
    "creationDate"  TIMESTAMP(6) NOT NULL,
    "user"          VARCHAR(256) NOT NULL,
    "operationType" VARCHAR(128) NOT NULL,
    "tableName"     VARCHAR(128) NOT NULL,
    "entityId"      BIGINT       NOT NULL,
    "changedFields" TEXT         NOT NULL
);

--
-- Table structure for table "JobCode"
--

CREATE TABLE IF NOT EXISTS "JobCode"
(
    id           BIGSERIAL PRIMARY KEY,
    "creationDate" TIMESTAMP(6) NOT NULL,
    "updateDate"   TIMESTAMP(6) NOT NULL,
    code         VARCHAR(128) NOT NULL,
    name         VARCHAR(1024) DEFAULT NULL,
    major        VARCHAR(1024) DEFAULT NULL,
    minor        VARCHAR(1024) DEFAULT NULL,
    broad        VARCHAR(1024) DEFAULT NULL,
    detailed     VARCHAR(1024) DEFAULT NULL,
    description  TEXT,
    framework    VARCHAR(1024) DEFAULT NULL,
    url          VARCHAR(1024) DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_JobCode_code ON "JobCode" (code);

--
-- Table structure for table "Keyword"
--

CREATE TABLE IF NOT EXISTS "Keyword"
(
    id                BIGSERIAL PRIMARY KEY,
    "creationDate"    TIMESTAMP(6) NOT NULL,
    "updateDate"      TIMESTAMP(6) NOT NULL,
    value             VARCHAR(768),
    uri               VARCHAR(768),
    keyword_type_enum VARCHAR(64) NOT NULL,
    framework         VARCHAR(768) DEFAULT NULL
);

CREATE INDEX IF NOT EXISTS idx_Keyword_value ON "Keyword" (keyword_type_enum, value);
CREATE INDEX IF NOT EXISTS idx_Keyword_uri ON "Keyword" (keyword_type_enum, uri);

--
-- Table structure for table "RichSkillDescriptor"
--

CREATE TABLE IF NOT EXISTS "RichSkillDescriptor"
(
    id                BIGSERIAL PRIMARY KEY,
    "creationDate"    TIMESTAMP(6) NOT NULL,
    "updateDate"      TIMESTAMP(6) NOT NULL,
    uuid              VARCHAR(36) NOT NULL,
    name              TEXT        NOT NULL,
    statement         TEXT        NOT NULL,
    author_id         BIGINT DEFAULT NULL,
    CONSTRAINT "RichSkillDescriptor_uuid_unique" UNIQUE (uuid),
    CONSTRAINT "fk_RichSkillDescriptor_author_id_id" FOREIGN KEY (author_id) REFERENCES "Keyword" (id) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Table structure for table "RichSkillJobCodes"
--

CREATE TABLE IF NOT EXISTS "RichSkillJobCodes"
(
    richskill_id BIGINT NOT NULL,
    jobcode_id   BIGINT NOT NULL,
    PRIMARY KEY (richskill_id, jobcode_id),
    CONSTRAINT "fk_RichSkillJobCodes_jobcode_id_id" FOREIGN KEY (jobcode_id) REFERENCES "JobCode" (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT "fk_RichSkillJobCodes_richskill_id_id" FOREIGN KEY (richskill_id) REFERENCES "RichSkillDescriptor" (id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table "RichSkillKeywords"
--

CREATE TABLE IF NOT EXISTS "RichSkillKeywords"
(
    richskill_id BIGINT NOT NULL,
    keyword_id   BIGINT NOT NULL,
    PRIMARY KEY (richskill_id, keyword_id),
    CONSTRAINT "fk_RichSkillKeywords_keyword_id_id" FOREIGN KEY (keyword_id) REFERENCES "Keyword" (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT "fk_RichSkillKeywords_richskill_id_id" FOREIGN KEY (richskill_id) REFERENCES "RichSkillDescriptor" (id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table "Collection"
--

CREATE TABLE IF NOT EXISTS "Collection"
(
    id           BIGSERIAL PRIMARY KEY,
    "creationDate" TIMESTAMP(6) NOT NULL,
    "updateDate"   TIMESTAMP(6) NOT NULL,
    uuid         VARCHAR(36) NOT NULL,
    name         TEXT        NOT NULL,
    author_id    BIGINT      NULL,
    CONSTRAINT "Collection_uuid_unique" UNIQUE (uuid),
    CONSTRAINT "fk_Collection_author_id_id" FOREIGN KEY (author_id) REFERENCES "Keyword" (id) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Table structure for table "CollectionSkills"
--

CREATE TABLE IF NOT EXISTS "CollectionSkills"
(
    collection_id BIGINT NOT NULL,
    skill_id      BIGINT NOT NULL,
    CONSTRAINT "PK_CollectionSkills_c_rs" PRIMARY KEY (collection_id, skill_id),
    CONSTRAINT "fk_CollectionSkills_collection_id_id" FOREIGN KEY (collection_id) REFERENCES "Collection" (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT "fk_CollectionSkills_skill_id_id" FOREIGN KEY (skill_id) REFERENCES "RichSkillDescriptor" (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS "CollectionSkills_collection_id" ON "CollectionSkills" (collection_id);
CREATE INDEX IF NOT EXISTS "CollectionSkills_skill_id" ON "CollectionSkills" (skill_id);
