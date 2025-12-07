CREATE TABLE IF NOT EXISTS backups_data (
    id BIGINT PRIMARY KEY,
    backup_data varchar(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );