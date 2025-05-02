ALTER TABLE sensor_types
    ALTER COLUMN metadata TYPE JSONB USING metadata::jsonb;