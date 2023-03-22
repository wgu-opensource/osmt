USE osmt_db;

UPDATE Collection set status = 'published' where publishDate is not null and archiveDate is null;
UPDATE Collection set status = 'archived' where archiveDate is not null;
