-- Удаление существующих таблиц (для тестовой среды)
DROP TABLE IF EXISTS sensors CASCADE;
DROP TABLE IF EXISTS sensor_data CASCADE;
DROP TABLE IF EXISTS sensor_types CASCADE;

-- Создание таблицы типов сенсоров
CREATE TABLE IF NOT EXISTS sensor_types
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) UNIQUE NOT NULL,
    unit        VARCHAR(50)         NOT NULL,
    description TEXT,
    metadata    JSONB
);

-- Создание таблицы сенсоров
CREATE TABLE IF NOT EXISTS sensors
(
    id                   UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    type_id              UUID                NOT NULL REFERENCES sensor_types (id),
    serial_number        VARCHAR(255) UNIQUE NOT NULL,
    manufacturer         VARCHAR(255),
    min_value            VARCHAR(255),
    max_value            VARCHAR(255),
    production_date      DATE,
    installation_date    DATE,
    is_active            BOOLEAN             NOT NULL DEFAULT true,
    company_id           UUID                NOT NULL REFERENCES companies (id),
    calibration_due_date DATE
);

-- Создание таблицы данных сенсоров с ENUM для статуса
CREATE TYPE sensor_status AS ENUM ('NORMAL', 'WARNING', 'CRITICAL');

CREATE TABLE IF NOT EXISTS sensor_data
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sensor_id UUID         NOT NULL REFERENCES sensors (id),
    value     VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP    NOT NULL,
    status    sensor_status
);

-- Обновление таблицы связей техники и сенсоров
DROP TABLE IF EXISTS technics_sensors;
CREATE TABLE IF NOT EXISTS technics_sensors
(
    technic_id UUID NOT NULL REFERENCES technics (id) ON DELETE CASCADE,
    sensor_id  UUID NOT NULL REFERENCES sensors (id) ON DELETE CASCADE,
    PRIMARY KEY (technic_id, sensor_id)
);