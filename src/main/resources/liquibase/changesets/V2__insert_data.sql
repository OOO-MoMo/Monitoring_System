insert into users (username, password)
values ('Admin', '$2y$10$06FlDAJkw4hFQ5Wj500BPuFbIPjwfdrI1PESN/xvj1T9SU9VtnEJK'),
       ('Ivan101', '$2y$10$aHWVdEVohb7EH9qpXHLJauprSxdPKaABjkhKSoegZTmkv7puVmaRC');

insert into roles(role)
values ('ROLE_ADMIN'),
       ('ROLE_USER');

insert into users_roles(user_id, roles_role_id)
values (1, 1),
       (1, 2),
       (2, 2);

insert into users_data(user_id, phone_number, firstname, lastname, patronymic)
values (1, '88005553535', 'Admin', 'Adminov', 'Adminovich'),
       (2, '81234567890', 'Ivan', 'Ivanov', 'Ivanovich');