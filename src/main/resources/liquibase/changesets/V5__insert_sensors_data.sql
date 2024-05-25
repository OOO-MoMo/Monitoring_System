insert into sensors(type, data_type)
values ('speedometer', 'km/h'),
       ('fuel_sensor', '%'),
       ('pressure_sensor', 'MPa');

insert into technics_sensors(technic_id, sensors_sensor_id)
values (1, 1),
       (1, 2),
       (1, 3),
       (2, 2),
       (3, 2),
       (3, 3);