-- Вставка администратора
INSERT INTO users (id, email, password, role, is_confirmed)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', -- явный UUID
        'admin@example.com',
        '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK',
        'ROLE_ADMIN',
        true);

INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
        '88005553535',
        'Admin',
        'Adminov',
        'Adminovich');