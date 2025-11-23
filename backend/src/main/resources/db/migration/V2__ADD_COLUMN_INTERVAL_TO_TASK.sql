CREATE TYPE frequency_enum AS ENUM ('DAILY', 'WEEKLY', 'MONTHLY');

ALTER TABLE tasks
ADD COLUMN "interval" frequency_enum;