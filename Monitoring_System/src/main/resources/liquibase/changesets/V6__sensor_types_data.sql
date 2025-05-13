-- Вставка типов сенсоров
INSERT INTO sensor_types (id, name, unit, description, metadata)
VALUES ('11111111-0000-0000-0000-000000000001', 'Датчик температуры', '°C', 'Диапазон: -40°C до 85°C', '{
  "precision": 0.1,
  "interface": [
    "I2C",
    "RS-485"
  ],
  "resolution": "16bit"
}'),
       ('11111111-0000-0000-0000-000000000002', 'Датчик давления', 'kPa', 'Диапазон: 0-1000 kPa', '{
         "precision": 0.5,
         "interface": [
           "RS-485"
         ],
         "max_response_time": "100ms"
       }');

-- Вставка сенсоров
INSERT INTO sensors (id, type_id, company_id, serial_number, manufacturer, min_value, max_value, production_date,
                     installation_date, calibration_due_date)
VALUES ('22222222-0000-0000-0000-000000000001', '11111111-0000-0000-0000-000000000001',
        'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SN-TEMP-001', 'SensCorp', '-40', '85', '2023-01-01', '2023-02-01',
        '2024-02-01'),
       ('22222222-0000-0000-0000-000000000002', '11111111-0000-0000-0000-000000000002',
        'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SN-PRESS-001', 'PressTech', '0', '1000', '2023-03-01', '2023-04-01',
        '2024-04-01');

-- Привязка сенсоров к технике
UPDATE sensors
SET technic_id = '55555555-1111-1111-1111-111111111111'
WHERE id = '22222222-0000-0000-0000-000000000001';
UPDATE sensors
SET technic_id = '55555555-2222-2222-2222-222222222222'
WHERE id = '22222222-0000-0000-0000-000000000002';