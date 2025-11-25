INSERT INTO clients (name_or_ip)
VALUES ('7.0.93.105'),
       ('192.168.10.15'),
       ('172.168.3.0'),
       ('10.0.0.42'),
       ('10.0.5.79');

INSERT INTO tasks (name, client_id, source)
VALUES ('Task 1', 1, '/data/source1'),
       ('Task 2', 2, '/data/source2'),
       ('Task 3', 3, '/data/source3'),
       ('Task 4', 4, '/data/source4'),
       ('Task 5', 5, '/data/source5');