ALTER TABLE Collection ADD workspace_owner VARCHAR(64) NOT NULL;
ALTER TABLE Collection ADD status enum ('Draft','Published','Archived','Workspace') NOT NULL;
CREATE INDEX Collection_workspace_owner ON Collection (workspace_owner);