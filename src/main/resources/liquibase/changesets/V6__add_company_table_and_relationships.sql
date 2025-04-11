-- Создание таблицы компаний
CREATE TABLE IF NOT EXISTS companies
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL UNIQUE,
    inn                 VARCHAR(12)  NOT NULL UNIQUE,
    head_office_address TEXT         NOT NULL
);

-- Добавляем колонку company_id в таблицу users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS company_id UUID;

-- Добавляем внешний ключ для связи с companies
ALTER TABLE users
    ADD CONSTRAINT fk_users_company FOREIGN KEY (company_id)
        REFERENCES companies (id) ON DELETE SET NULL;

-- Добавляем колонку company_id в таблицу technics
ALTER TABLE technics
    ADD COLUMN IF NOT EXISTS company_id UUID;

-- Добавляем внешний ключ для связи с companies
ALTER TABLE technics
    ADD CONSTRAINT fk_technics_company FOREIGN KEY (company_id)
        REFERENCES companies (id) ON DELETE SET NULL;