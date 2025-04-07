-- Вставка менеджера

INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at)
VALUES ('44444444-4444-4444-4444-444444444444',
        'manager@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_MANAGER',
        true,
        true,
        now());

INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address, organization)
VALUES ('44444444-4444-4444-4444-444444444444',
        '89998887766',
        'Марина',
        'Менеджерова',
        'Петровна',
        '1992-03-12',
        'г. Санкт-Петербург, ул. Менеджерская, д. 7',
        'ООО "Управление+"');
