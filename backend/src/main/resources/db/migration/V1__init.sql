CREATE TYPE backup_state AS ENUM ('COMPLETED', 'FAILED', 'RUNNING', 'CANCELLED', 'QUEUED');

CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS "groups" (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT groups_name_ck CHECK (name IN ('Superuser','Administrators','Operators','Restore Users','Backup Users'))
    );

CREATE TABLE IF NOT EXISTS user_group_relations (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES "groups"(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, group_id)
    );

CREATE TABLE IF NOT EXISTS clients (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name_or_ip VARCHAR(255) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    source TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS backups (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,
    task_id BIGINT REFERENCES tasks(id) ON DELETE SET NULL,
    start_time TIMESTAMP NOT NULL,
    stop_time TIMESTAMP,
    size_bytes BIGINT NOT NULL DEFAULT 0,
    state backup_state NOT NULL,
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT backups_time_ck CHECK (stop_time IS NULL OR stop_time >= start_time)
    );


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

