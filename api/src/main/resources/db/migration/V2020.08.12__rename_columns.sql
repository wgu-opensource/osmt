USE osmt_db;

ALTER TABLE osmt_db.RichSkillDescriptor
    CHANGE title name text;
ALTER TABLE osmt_db.RichSkillDescriptor
    CHANGE description statement text;
