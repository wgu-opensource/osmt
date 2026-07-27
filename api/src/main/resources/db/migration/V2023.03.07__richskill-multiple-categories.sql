-- Migration of RichSkillDescriptor.cat_id to RichSkillKeywords table.

INSERT INTO "RichSkillKeywords" (richskill_id, keyword_id)
SELECT id, cat_id
FROM "RichSkillDescriptor"
WHERE cat_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Removal of 'cat_id' from RichSkillDescriptor table.

ALTER TABLE "RichSkillDescriptor"
    DROP CONSTRAINT IF EXISTS "fk_RichSkillDescriptor_cat_id_id",
    DROP COLUMN cat_id;
