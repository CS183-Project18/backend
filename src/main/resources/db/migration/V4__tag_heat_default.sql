-- Owner: Kaijie Zhu. Purpose: Tag heat default schema migration.
UPDATE tags
SET heat_score = 0
WHERE heat_score IS NULL;
