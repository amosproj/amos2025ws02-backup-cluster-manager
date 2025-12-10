INSERT INTO clients (name_or_ip)
VALUES ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int),
       ((floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int || '.' || (floor(random() * 256)):: int);

INSERT INTO tasks (name, client_id, source, "interval")
VALUES ('Daily Backup', 1, '/dev', 'DAILY'),
       ('Weekly Backup', 2, '/dev', 'WEEKLY'),
       ('Monthly Backup', 3, '/dev', 'MONTHLY'),
       ('Nightly Backup', 4, '/dev', 'DAILY'),
       ('Incremental Backup', 5, '/dev', 'DAILY'),
       ('Full System Backup', 6, '/dev', 'WEEKLY'),
       ('Database Backup', 7, '/dev', 'DAILY'),
       ('Log Files Backup', 8, '/dev', 'DAILY'),
       ('Config Files Backup', 9, '/dev', 'MONTHLY'),
       ('User Data Backup', 10, '/dev', 'DAILY'),
       ('Temp Files Backup', 11, '/dev', 'WEEKLY'),
       ('Security Backup', 12, '/dev', 'MONTHLY'),
       ('Archive Backup', 13, '/dev', 'MONTHLY'),
       ('VM Snapshot Backup', 14, '/dev', 'WEEKLY'),
       ('Application Backup', 15, '/dev', 'DAILY'),
       ('Critical Files Backup', 16, '/dev', 'DAILY'),
       ('Service Backup', 17, '/dev', 'WEEKLY'),
       ('Cache Backup', 18, '/dev', 'DAILY'),
       ('Network Config Backup', 19, '/dev', 'MONTHLY'),
       ('Email Backup', 20, '/dev', 'DAILY'),
       ('System Logs Backup', 21, '/dev', 'WEEKLY'),
       ('User Profiles Backup', 22, '/dev', 'DAILY'),
       ('Database Snapshot Backup', 23, '/dev', 'MONTHLY'),
       ('Temp Directory Backup', 24, '/dev', 'WEEKLY'),
       ('Performance Data Backup', 25, '/dev', 'MONTHLY');


INSERT INTO backups (client_id, task_id, start_time, stop_time, size_bytes, state, message)
SELECT (gs % 25) + 1 AS client_id, -- cycle through clients 1..25
       ((gs % 25) + 1) AS task_id,   -- cycle through tasks 1..25
       start_time,
       start_time + (random() * interval '2 hours') AS stop_time,
       (random() * 5000)::bigint AS size_bytes, (ARRAY['COMPLETED', 'FAILED'])[ceil(random()*2)::int]::backup_state AS state,
    'Backup generated for testing' AS message
FROM generate_series(1, 1875) gs, LATERAL (SELECT NOW() - (random() * interval '7 days') AS start_time) s;