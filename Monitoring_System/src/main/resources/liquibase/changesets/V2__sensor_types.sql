-- Создание таблицы типов сенсоров
CREATE TABLE IF NOT EXISTS sensor_types
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) UNIQUE NOT NULL,
    unit        VARCHAR(50)         NOT NULL,
    description TEXT,
    metadata    JSONB
);