ALTER TABLE sensors
    ALTER COLUMN data SET DATA TYPE varchar(255);

ALTER TABLE sensors
    ADD COLUMN data_type varchar(255) NOT NULL;

ALTER TABLE sensors
    ADD CONSTRAINT unique_type UNIQUE (type);