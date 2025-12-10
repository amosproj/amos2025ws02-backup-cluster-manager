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