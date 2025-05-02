-- Создание таблицы техники
CREATE TABLE IF NOT EXISTS technics
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID         REFERENCES users (id) ON DELETE SET NULL,
    company_id        UUID         REFERENCES companies (id) ON DELETE SET NULL,
    model             VARCHAR(255),
    brand             VARCHAR(255) NOT NULL,
    year              INTEGER,
    serial_number     VARCHAR(255) UNIQUE,
    vin               VARCHAR(255) UNIQUE,
    description       TEXT,
    is_active         BOOLEAN          DEFAULT true,
    last_service_date TIMESTAMP,
    next_service_date TIMESTAMP
);