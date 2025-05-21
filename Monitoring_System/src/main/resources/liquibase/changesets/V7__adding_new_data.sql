-- Новая миграция для добавления менеджеров, водителей, техники и датчиков с генерацией UUID

-- Вставка новых типов сенсоров (если нужны для новых датчиков)
INSERT INTO sensor_types (id, name, unit, description, metadata)
VALUES (gen_random_uuid(), 'Датчик уровня топлива', '%', 'Емкостной датчик уровня топлива',
        '{
          "precision": 1,
          "interface": [
            "RS-485"
          ],
          "length_options": [
            700,
            1000,
            1500
          ]
        }'),
       (gen_random_uuid(), 'Датчик скорости (GPS)', 'км/ч',
        'Определение скорости по GPS/ГЛОНАСС',
        '{
          "accuracy": "2.5m CEP",
          "update_rate": "1Hz"
        }'),
       (gen_random_uuid(), 'Датчик оборотов двигателя', 'об/мин', 'Считывание оборотов двигателя',
        '{
          "interface": [
            "CAN",
            "Analog"
          ],
          "range": "0-8000"
        }')
ON CONFLICT (name) DO NOTHING;

DO
$$
    DECLARE
        manager_id_1            UUID;
        manager_id_2            UUID;
        manager_id_3            UUID;
        driver_id_1             UUID;
        driver_id_2             UUID;
        driver_id_3             UUID;
        driver_id_4             UUID;
        driver_id_5             UUID;
        technic_id_1            UUID;
        technic_id_2            UUID;
        technic_id_3            UUID;
        technic_id_4            UUID;
        technic_id_5            UUID;
        technic_id_6            UUID;
        technic_id_7            UUID;
        fuel_sensor_type_id     UUID;
        gps_sensor_type_id      UUID;
        rpm_sensor_type_id      UUID;
        temp_sensor_type_id     UUID;
        pressure_sensor_type_id UUID;
    BEGIN

        SELECT id INTO fuel_sensor_type_id FROM sensor_types WHERE name = 'Датчик уровня топлива';
        SELECT id INTO gps_sensor_type_id FROM sensor_types WHERE name = 'Датчик скорости (GPS)';
        SELECT id INTO rpm_sensor_type_id FROM sensor_types WHERE name = 'Датчик оборотов двигателя';
        SELECT id INTO temp_sensor_type_id FROM sensor_types WHERE name = 'Датчик температуры';
        SELECT id INTO pressure_sensor_type_id FROM sensor_types WHERE name = 'Датчик давления';


        -- Вставка 3 новых Менеджеров
        INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
        VALUES (gen_random_uuid(), 'newmanager1@example.com',
                '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK', 'ROLE_MANAGER', true, true, NOW(),
                'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
        RETURNING id INTO manager_id_1;
        INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
        VALUES (manager_id_1, '89011112233', 'Сергей', 'Управляев', 'Викторович', '1980-06-20',
                'г. Казань, ул. Управленческая, д. 10');

        INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
        VALUES (gen_random_uuid(), 'newmanager2@example.com',
                '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK', 'ROLE_MANAGER', true, true, NOW(),
                'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
        RETURNING id INTO manager_id_2;
        INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
        VALUES (manager_id_2, '89022223344', 'Ольга', 'Менеджеркина', 'Игоревна', '1987-09-05',
                'г. Екатеринбург, ул. Деловая, д. 15');

        INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
        VALUES (gen_random_uuid(), 'newmanager3@example.com',
                '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK', 'ROLE_MANAGER', true, true, NOW(),
                'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
        RETURNING id INTO manager_id_3;
        INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
        VALUES (manager_id_3, '89033334455', 'Константин', 'Организаторов', 'Андреевич', '1991-12-18',
                'г. Санкт-Петербург, пр. Инновационный, д. 22');

        -- Вставка 5 новых Водителей
        INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
        VALUES (gen_random_uuid(), 'newdriver1@example.com',
                '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK', 'ROLE_DRIVER', true, true, NOW(),
                'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
        RETURNING id INTO driver_id_1;
        INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
        VALUES (driver_id_1, '89055556677', 'Алексей', 'Водичкин', 'Сергеевич', '1993-02-11',
                'г. Казань, ул. Дорожная, д. 25');

        INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
        VALUES (gen_random_uuid(), 'newdriver2@example.com',
                '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK', 'ROLE_DRIVER', true, true, NOW(),
                'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
        RETURNING id INTO driver_id_2;
        INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
        VALUES (driver_id_2, '89066667788', 'Дмитрий', 'Рулев', 'Анатольевич', '1989-07-14',
                'г. Казань, ул. Автомобильная, д. 3');

        INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
        VALUES (gen_random_uuid(), 'newdriver3@example.com',
                '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK', 'ROLE_DRIVER', true, true, NOW(),
                'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
        RETURNING id INTO driver_id_3;
        INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
        VALUES (driver_id_3, '89077778899', 'Виктор', 'Перевозов', 'Дмитриевич', '1995-11-30',
                'г. Екатеринбург, ул. Магистральная, д. 8');

        INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
        VALUES (gen_random_uuid(), 'newdriver4@example.com',
                '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK', 'ROLE_DRIVER', true, true, NOW(),
                'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
        RETURNING id INTO driver_id_4;
        INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
        VALUES (driver_id_4, '89088889900', 'Николай', 'Трассов', 'Ильич', '1990-01-25',
                'г. Екатеринбург, ул. Скоростная, д. 11');

        INSERT INTO users (id, email, password, role, is_confirmed, is_active, created_at, company_id)
        VALUES (gen_random_uuid(), 'newdriver5@example.com',
                '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK', 'ROLE_DRIVER', true, true, NOW(),
                'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa')
        RETURNING id INTO driver_id_5;
        INSERT INTO users_data (user_id, phone_number, firstname, lastname, patronymic, date_of_birth, address)
        VALUES (driver_id_5, '89099990011', 'Елена', 'Доставкина', 'Юрьевна', '1996-04-08',
                'г. Санкт-Петербург, ул. Грузовая, д. 19');

        -- Вставка 7 единиц Техники
        INSERT INTO technics (id, user_id, company_id, model, brand, year, serial_number, vin, description, is_active,
                              last_service_date, next_service_date)
        VALUES (gen_random_uuid(), driver_id_1, 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Sprinter', 'Mercedes-Benz',
                2019, 'SN-TECH-N001', 'VINTECHN0000001', 'Грузовой фургон', true, '2024-04-01 09:00:00',
                '2025-04-01 09:00:00')
        RETURNING id INTO technic_id_1;

        INSERT INTO technics (id, user_id, company_id, model, brand, year, serial_number, vin, description, is_active,
                              last_service_date, next_service_date)
        VALUES (gen_random_uuid(), driver_id_2, 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Gazelle Next', 'GAZ', 2021,
                'SN-TECH-N002', 'VINTECHN0000002', 'Малотоннажный грузовик', true, '2024-03-15 10:00:00',
                '2025-03-15 10:00:00')
        RETURNING id INTO technic_id_2;

        INSERT INTO technics (id, user_id, company_id, model, brand, year, serial_number, vin, description, is_active,
                              last_service_date, next_service_date)
        VALUES (gen_random_uuid(), driver_id_3, 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Actros', 'Mercedes-Benz',
                2020, 'SN-TECH-N003', 'VINTECHN0000003', 'Седельный тягач', true, '2024-05-01 11:00:00',
                '2025-05-01 11:00:00')
        RETURNING id INTO technic_id_3;

        INSERT INTO technics (id, user_id, company_id, model, brand, year, serial_number, vin, description, is_active,
                              last_service_date, next_service_date)
        VALUES (gen_random_uuid(), driver_id_4, 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '5490', 'KAMAZ', 2022,
                'SN-TECH-N004', 'VINTECHN0000004', 'Магистральный тягач', true, '2024-06-10 14:00:00',
                '2025-06-10 14:00:00')
        RETURNING id INTO technic_id_4;

        INSERT INTO technics (id, user_id, company_id, model, brand, year, serial_number, vin, description, is_active,
                              last_service_date, next_service_date)
        VALUES (gen_random_uuid(), NULL, 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'XF', 'DAF', 2017, 'SN-TECH-N005',
                'VINTECHN0000005', 'Тягач (свободен)', true, '2023-12-01 09:00:00', '2024-12-01 09:00:00')
        RETURNING id INTO technic_id_5; -- Техника без водителя

        INSERT INTO technics (id, user_id, company_id, model, brand, year, serial_number, vin, description, is_active,
                              last_service_date, next_service_date)
        VALUES (gen_random_uuid(), driver_id_5, 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Transit', 'Ford', 2023,
                'SN-TECH-N006', 'VINTECHN0000006', 'Коммерческий фургон', true, '2024-07-01 16:00:00',
                '2025-07-01 16:00:00')
        RETURNING id INTO technic_id_6;

        INSERT INTO technics (id, user_id, company_id, model, brand, year, serial_number, vin, description, is_active,
                              last_service_date, next_service_date)
        VALUES (gen_random_uuid(), NULL, 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Bobcat S70', 'Bobcat', 2019,
                'SN-TECH-N007', 'VINTECHN0000007', 'Мини-погрузчик (свободен)', false, '2023-10-01 10:00:00',
                '2024-10-01 10:00:00')
        RETURNING id INTO technic_id_7;
        -- Неактивная техника

        -- Вставка 10 Датчиков
        INSERT INTO sensors (id, type_id, company_id, technic_id, serial_number, manufacturer, min_value, max_value,
                             production_date, installation_date, calibration_due_date, is_active)
        VALUES
            -- Датчики для technic_id_1 (Mercedes Sprinter)
            (gen_random_uuid(), temp_sensor_type_id, 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             technic_id_1, 'SN-SENS-N001', 'Bosch', '-30', '70', '2023-05-01', '2023-06-01', '2024-06-01', true),
            (gen_random_uuid(), fuel_sensor_type_id, 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             technic_id_1, 'SN-SENS-N002', 'Omnicomm', '0', '100', '2023-05-05', '2023-06-01', '2024-06-01', true),

            -- Датчики для technic_id_2 (Gazelle Next)
            (gen_random_uuid(), gps_sensor_type_id, 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             technic_id_2, 'SN-SENS-N003', 'Teltonika', NULL, NULL, '2023-07-01', '2023-08-01', '2024-08-01', true),

            -- Датчики для technic_id_3 (Mercedes Actros)
            (gen_random_uuid(), temp_sensor_type_id, 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             technic_id_3, 'SN-SENS-N004', 'Sensirion', '-20', '60', '2023-08-10', '2023-09-01', '2024-09-01', true),
            (gen_random_uuid(), rpm_sensor_type_id, 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             technic_id_3, 'SN-SENS-N005', 'Continental', '500', '3000', '2023-08-15', '2023-09-01', '2024-09-01',
             true),

            -- Датчики для technic_id_4 (KAMAZ 5490)
            (gen_random_uuid(), fuel_sensor_type_id, 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             technic_id_4, 'SN-SENS-N006', 'Escort', '0', '100', '2023-10-01', '2023-11-01', '2024-11-01', true),
            (gen_random_uuid(), pressure_sensor_type_id, 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             technic_id_4, 'SN-SENS-N007', 'WIKA', '0', '2500', '2023-10-05', '2023-11-01', '2024-11-01', true),

            -- Датчики для technic_id_6 (Ford Transit)
            (gen_random_uuid(), temp_sensor_type_id, 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             technic_id_6, 'SN-SENS-N008', 'SensCorp', '-40', '85', '2024-01-10', '2024-02-01', '2025-02-01', true),

            -- "Свободные" датчики или для другой техники
            (gen_random_uuid(), gps_sensor_type_id, 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NULL,
             'SN-SENS-N009', 'Navtelecom', NULL, NULL, '2024-02-15', '2024-03-01', '2025-03-01', true),
            (gen_random_uuid(), pressure_sensor_type_id, 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             '55555555-3333-3333-3333-333333333333', 'SN-SENS-N010', 'PressTech', '0', '1500', '2024-03-10',
             '2024-04-01',
             '2025-04-01', false);
    END
$$;