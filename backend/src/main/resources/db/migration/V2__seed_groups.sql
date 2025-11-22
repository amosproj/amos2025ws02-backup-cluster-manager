INSERT INTO "groups" (name, enabled)
SELECT v.name, TRUE
FROM (VALUES
  ('Superuser'),
  ('Administrators'),
  ('Operators'),
  ('Restore Users'),
  ('Backup Users')
) AS v(name)
WHERE NOT EXISTS (SELECT 1 FROM "groups" g WHERE g.name = v.name);