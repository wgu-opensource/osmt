ALTER TABLE "RichSkillDescriptor"
    DROP CONSTRAINT IF EXISTS "fk_RichSkillDescriptor_publish_status_id_id",
    ADD COLUMN "archiveDate" TIMESTAMP(6) NULL,
    ADD COLUMN "publishDate" TIMESTAMP(6) NULL;

DROP TABLE IF EXISTS "PublishStatus";

ALTER TABLE "RichSkillDescriptor" DROP COLUMN IF EXISTS publish_status_id;

ALTER TABLE "Collection"
    ADD COLUMN "archiveDate" TIMESTAMP(6) NULL,
    ADD COLUMN "publishDate" TIMESTAMP(6) NULL;
