create table if not exists users
(
    user_id bigserial primary key,
    username varchar(255) not null unique,
    password varchar(255) not null
);

create table if not exists roles
(
    role_id bigserial primary key,
    role varchar(255) not null unique
);

create table if not exists users_roles
(
    user_id bigint not null,
    roles_role_id bigint not null,
    primary key (user_id, roles_role_id),
    constraint fk_users_roles_users foreign key (user_id) references users (user_id) on delete cascade on update no action,
    constraint fk_users_roles_roles foreign key (roles_role_id) references roles (role_id) on delete cascade on update no action
);

create table if not exists users_data
(
    users_data_id bigserial primary key,
    user_id bigint not null unique,
    phone_number varchar(255) not null,
    firstname varchar(255) not null,
    lastname varchar(255) not null,
    patronymic varchar(255),
    constraint fk_users_data_users foreign key (user_id) references users (user_id) on delete cascade on update no action
);

create table if not exists sensors
(
    sensor_id bigserial primary key,
    data double precision,
    type varchar(255) not null
);

create table if not exists technics
(
    technic_id bigserial primary key,
    user_id bigint not null,
    brand varchar(255) not null,
    model varchar(255),
    constraint fk_technics_users foreign key (user_id) references users (user_id) on delete cascade on update no action
);

create table if not exists technics_sensors
(
    technic_id bigint not null,
    sensors_sensor_id bigint not null,
    primary key (technic_id, sensors_sensor_id),
    constraint fk_technics_sensors_technics foreign key (technic_id) references technics (technic_id) on delete cascade on update no action,
    constraint fk_technics_sensors_sensors foreign key (sensors_sensor_id) references sensors (sensor_id) on delete cascade on update no action
);



