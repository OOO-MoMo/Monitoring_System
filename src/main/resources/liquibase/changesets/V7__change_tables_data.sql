DELETE
FROM technics;
DELETE
FROM users_data;
DELETE
FROM users;

-- Удаляем устаревшую колонку organization
ALTER TABLE users_data
    DROP COLUMN IF EXISTS organization;

-- Вставка компаний
INSERT INTO companies (id, name, inn, head_office_address)
VALUES ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ООО "Транспорт+"', '7701234567', 'г. Казань, ул. Центральная, д. 1'),
       ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'АО "Грузовые перевозки"', '7707654321',
        'г. Екатеринбург, ул. Транспортная, д. 12'),
       ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ООО "Управление+"', '7711223344',
        'г. Санкт-Петербург, ул. Менеджерская, д. 7');

INSERT INTO users (id, email, password, role, is_confirmed, company_id)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', -- явный UUID
        'admin@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_ADMIN',
        true,
        'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa');

INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        '88005553535',
        'Admin',
        'Adminov',
        'Adminovich',
        '1985-01-15',
        '123456, Россия, г. Москва, ул. Примерная, д. 1');

-- Вставка водителей с привязкой к компании
INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
VALUES ('11111111-1111-1111-1111-111111111111', 'driver1@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_DRIVER', true, true, now(), 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),

       ('22222222-2222-2222-2222-222222222222', 'driver2@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_DRIVER', true, true, now(), 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),

       ('33333333-3333-3333-3333-333333333333', 'driver3@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_DRIVER', true, true, now(), 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa');

INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
VALUES ('11111111-1111-1111-1111-111111111111', '89001234567', 'Иван', 'Иванов', 'Иванович', '1990-05-10',
        'г. Казань, ул. Центральная, д. 5'),
       ('22222222-2222-2222-2222-222222222222', '89107654321', 'Пётр', 'Петров', 'Петрович', '1988-11-03',
        'г. Екатеринбург, ул. Техническая, д. 10'),
       ('33333333-3333-3333-3333-333333333333', '89209876543', 'Петр', 'Сидоров', 'Александрович', '1995-07-22',
        'г. Новосибирск, ул. Лесная, д. 3');

-- Вставка менеджера
INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
VALUES ('44444444-4444-4444-4444-444444444444', 'manager@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_MANAGER', true, true, now(), 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa');

INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
VALUES ('44444444-4444-4444-4444-444444444444', '89998887766', 'Марина', 'Менеджерова', 'Петровна', '1992-03-12',
        'г. Санкт-Петербург, ул. Менеджерская, д. 7');

-- Вставка техники с привязкой к пользователю и компании
INSERT INTO technics (id, user_id, model, brand, year, serial_number, vin, description,
                      is_active, last_service_date, next_service_date, company_id)
VALUES ('55555555-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111',
        'T-150', 'Беларус', 2015, 'SN-000001', 'VIN00000000000001', 'Гусеничный трактор',
        true, '2024-01-10 09:00:00', '2025-01-10 09:00:00', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),

       ('55555555-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
        'К-700', 'Кировец', 2018, 'SN-000002', 'VIN00000000000002', 'Колёсный тягач',
        true, '2024-02-15 10:30:00', '2025-02-15 10:30:00', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),

       ('55555555-3333-3333-3333-333333333333', '33333333-3333-3333-3333-333333333333',
        'JCB 3CX', 'JCB', 2020, 'SN-000003', 'VIN00000000000003', 'Экскаватор-погрузчик',
        true, '2024-03-20 14:45:00', '2025-03-20 14:45:00', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa');
