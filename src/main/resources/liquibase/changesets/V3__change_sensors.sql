ALTER TABLE sensors
DROP
COLUMN data;

ALTER TABLE sensors
    ADD COLUMN data_type varchar(255) NOT NULL;

ALTER TABLE sensors
    ADD CONSTRAINT unique_type UNIQUE (type);