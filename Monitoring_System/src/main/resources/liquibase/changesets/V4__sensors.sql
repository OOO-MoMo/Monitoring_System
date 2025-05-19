-- Создание таблицы сенсоров
CREATE TABLE IF NOT EXISTS sensors
(
    id                   UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    type_id              UUID                NOT NULL REFERENCES sensor_types (id),
    company_id UUID REFERENCES companies (id) ON DELETE SET NULL,
    technic_id           UUID REFERENCES technics (id),
    serial_number        VARCHAR(255) UNIQUE NOT NULL,
    manufacturer         VARCHAR(255),
    min_value            VARCHAR(255),
    max_value            VARCHAR(255),
    production_date      DATE,
    installation_date    DATE                         DEFAULT CURRENT_DATE,
    is_active            BOOLEAN             NOT NULL DEFAULT true,
    calibration_due_date DATE
);

-- Условное создание типа статуса (если не существует)
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'sensor_status') THEN
            CREATE TYPE sensor_status AS ENUM ('NORMAL', 'WARNING', 'CRITICAL', 'UNDEFINED');
        END IF;
    END
$$;

-- Создание таблицы данных сенсоров
CREATE TABLE IF NOT EXISTS sensor_data
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sensor_id UUID REFERENCES sensors (id) ON DELETE SET NULL,
    technic_id UUID REFERENCES technics (id) ON DELETE SET NULL,
    value     VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP    NOT NULL,
    status    sensor_status
);