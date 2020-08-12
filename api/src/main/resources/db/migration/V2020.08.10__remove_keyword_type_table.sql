USE osmt_db;

ALTER TABLE osmt_db.Keyword
    DROP FOREIGN KEY fk_Keyword_keyword_type_id_id;

ALTER TABLE osmt_db.Keyword
    DROP COLUMN keyword_type_id;

DROP TABLE osmt_db.KeywordType;
