UPDATE "Collection" SET status = 'published' WHERE "publishDate" IS NOT NULL AND "archiveDate" IS NULL;
UPDATE "Collection" SET status = 'archived' WHERE "archiveDate" IS NOT NULL;
