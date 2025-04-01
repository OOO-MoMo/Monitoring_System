-- Создаем таблицы заново с учетом новых требований
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
    is_active BOOLEAN NOT NULL DEFAULT true
    );

CREATE TABLE IF NOT EXISTS users_data
(
    user_id
    UUID
    PRIMARY
    KEY,
    phone_number
    VARCHAR
(
    255
) NOT NULL UNIQUE,
    firstname VARCHAR
(
    255
) NOT NULL,
    lastname VARCHAR
(
    255
) NOT NULL,
    patronymic VARCHAR
(
    255
),
    date_of_birth DATE,
    address TEXT,
    organization VARCHAR
(
    255
),
    CONSTRAINT fk_users_data_users FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS sensors
(
    sensor_id
    BIGSERIAL
    PRIMARY
    KEY,
    type
    VARCHAR
(
    255
) NOT NULL,
    data_type VARCHAR
(
    255
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS technics
(
    technic_id
    BIGSERIAL
    PRIMARY
    KEY,
    user_id
    UUID
    NOT
    NULL,
    brand
    VARCHAR
(
    255
) NOT NULL,
    model VARCHAR
(
    255
),
    CONSTRAINT fk_technics_users FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
) ON DELETE CASCADE
    );

-- Таблица для связи ManyToMany между Technic и Sensor
CREATE TABLE IF NOT EXISTS technics_sensors
(
    technic_id
    BIGINT
    NOT
    NULL,
    sensor_id
    BIGINT
    NOT
    NULL,
    PRIMARY
    KEY
(
    technic_id,
    sensor_id
),
    CONSTRAINT fk_technics_sensors_technics FOREIGN KEY
(
    technic_id
) REFERENCES technics
(
    technic_id
) ON DELETE CASCADE,
    CONSTRAINT fk_technics_sensors_sensors FOREIGN KEY
(
    sensor_id
) REFERENCES sensors
(
    sensor_id
)
  ON DELETE CASCADE
    );