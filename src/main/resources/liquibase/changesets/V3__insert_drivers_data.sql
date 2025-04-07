-- Вставка водителей

-- Водитель 1
INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at)
VALUES ('11111111-1111-1111-1111-111111111111',
        'driver1@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_DRIVER',
        true,
        true,
        now());

INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address, organization)
VALUES ('11111111-1111-1111-1111-111111111111',
        '89001234567',
        'Иван',
        'Иванов',
        'Иванович',
        '1990-05-10',
        'г. Казань, ул. Центральная, д. 5',
        'ООО "Транспорт+"');

-- Водитель 2
INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at)
VALUES ('22222222-2222-2222-2222-222222222222',
        'driver2@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_DRIVER',
        true,
        true,
        now());

INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address, organization)
VALUES ('22222222-2222-2222-2222-222222222222',
        '89107654321',
        'Пётр',
        'Петров',
        'Петрович',
        '1988-11-03',
        'г. Екатеринбург, ул. Техническая, д. 10',
        'АО "Грузовые перевозки"');

-- Водитель 3
INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at)
VALUES ('33333333-3333-3333-3333-333333333333',
        'driver3@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_DRIVER',
        true,
        true,
        now());

INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address, organization)
VALUES ('33333333-3333-3333-3333-333333333333',
        '89209876543',
        'Петр',
        'Сидоров',
        'Александрович',
        '1995-07-22',
        'г. Новосибирск, ул. Лесная, д. 3',
        'АО "Грузовые перевозки"');
