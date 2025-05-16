-- Создание таблицы компаний
CREATE TABLE IF NOT EXISTS companies
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    name VARCHAR
(
    255
) NOT NULL UNIQUE,
    inn VARCHAR
(
    12
) NOT NULL UNIQUE,
    head_office_address TEXT NOT NULL
    );

-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    email VARCHAR
(
    255
) NOT NULL UNIQUE,
    password VARCHAR
(
    255
) NOT NULL,
    role VARCHAR
(
    20
) NOT NULL CHECK
(
    role
    IN
(
    'ROLE_ADMIN',
    'ROLE_MANAGER',
    'ROLE_DRIVER'
)),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_confirmed BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    company_id UUID REFERENCES companies
(
    id
) ON DELETE SET NULL
    );

-- Создание таблицы данных пользователей
CREATE TABLE IF NOT EXISTS users_data
(
    user_id
    UUID
    PRIMARY
    KEY
    REFERENCES
    users
(
    id
) ON DELETE CASCADE,
    phone_number VARCHAR
(
    255
) UNIQUE,
    firstname VARCHAR
(
    255
),
    lastname VARCHAR
(
    255
),
    patronymic VARCHAR
(
    255
),
    date_of_birth DATE,
    address TEXT
    );