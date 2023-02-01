ALTER TABLE Collection ADD workspace_owner VARCHAR(64) NOT NULL DEFAULT '';
ALTER TABLE Collection ADD status enum ('unarchived','deleted','workspace','published','archived','draft') NOT NULL DEFAULT 'draft';
CREATE INDEX Collection_workspace_owner ON Collection (workspace_owner);