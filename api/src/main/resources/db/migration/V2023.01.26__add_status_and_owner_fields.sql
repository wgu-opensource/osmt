ALTER TABLE "Collection" ADD COLUMN workspace_owner VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE "Collection" ADD COLUMN status VARCHAR(64) NOT NULL DEFAULT 'draft';
CREATE INDEX IF NOT EXISTS "Collection_workspace_owner" ON "Collection" (workspace_owner);
