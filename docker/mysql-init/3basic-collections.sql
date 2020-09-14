USE osmt_db;
SET character_set_client = utf8mb3;
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

