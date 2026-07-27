-- Migration of RichSkillDescriptor.author_id to RichSkillKeywords table.

INSERT INTO "RichSkillKeywords" (richskill_id, keyword_id)
SELECT id, author_id
FROM "RichSkillDescriptor"
WHERE author_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Removal of 'author_id' from RichSkillDescriptor table.

ALTER TABLE "RichSkillDescriptor"
    DROP CONSTRAINT IF EXISTS "fk_RichSkillDescriptor_author_id_id",
    DROP COLUMN author_id;
