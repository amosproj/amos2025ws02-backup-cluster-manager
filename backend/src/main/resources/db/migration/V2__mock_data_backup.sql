-- Mock-Daten für Entwicklung/Testing
INSERT INTO clients (name_or_ip, enabled) VALUES
                                              ('192.168.1.100', true),
                                              ('server01.example.com', true),
                                              ('client-dev-01', true),
                                              ('backup-server-prod', false);

INSERT INTO tasks (name, client_id, source, enabled) VALUES
                                                         ('Daily Backup', 1, '/var/data', true),
                                                         ('Weekly Full Backup', 2, '/home/users', true),
                                                         ('Database Backup', 1, '/var/lib/postgresql', true),
                                                         ('Config Backup', 3, '/etc', false);

INSERT INTO "groups" (name, enabled) VALUES
                                         ('Superuser', true),
                                         ('Administrators', true),
                                         ('Operators', true),
                                         ('Restore Users', true),
                                         ('Backup Users', true);

INSERT INTO users (name, password_hash, enabled) VALUES
                                                     ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', true), -- Passwort: admin
                                                     ('operator', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', true), -- Passwort: password
                                                     ('testuser', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', true);

INSERT INTO user_group_relations (user_id, group_id) VALUES
                                                         (1, 1), -- admin -> Superuser
                                                         (2, 3), -- operator -> Operators
                                                         (3, 5); -- testuser -> Backup Users

