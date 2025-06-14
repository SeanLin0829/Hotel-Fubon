SET NAMES utf8mb4;

DROP DATABASE IF EXISTS hotel_db;
CREATE DATABASE IF NOT EXISTS hotel_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hotel_db;

create table restaurant_table
(
    id       bigint auto_increment
        primary key,
    name     varchar(255)                                                 not null,
    capacity int                                                          not null,
    status   enum ('AVAILABLE', 'RESERVED', 'MAINTENANCE', 'UNAVAILABLE') not null
);

create table role
(
    id   bigint auto_increment
        primary key,
    name varchar(255) not null,
    constraint name
        unique (name)
);

create table room_type
(
    id         bigint auto_increment
        primary key,
    name       varchar(255)   null,
    base_price decimal(38, 2) null
);

create table room
(
    id          bigint auto_increment
        primary key,
    room_number varchar(255)         not null,
    type_id     bigint               null,
    is_active   tinyint(1) default 1 null,
    constraint room_number
        unique (room_number),
    constraint room_ibfk_1
        foreign key (type_id) references room_type (id)
);

create index type_id
    on room (type_id);

create table shift_template
(
    id           bigint auto_increment
        primary key,
    shift_type   enum ('morning', 'evening', 'night', 'off') not null,
    start_time   time                                        not null,
    end_time     time                                        not null,
    is_cross_day tinyint(1) default 0                        null,
    constraint shift_type
        unique (shift_type)
);

create table user
(
    id        bigint auto_increment
        primary key,
    username  varchar(255) not null,
    password  varchar(255) not null,
    full_name varchar(255) null,
    email     varchar(255) null,
    phone     varchar(255) null,
    role_id   bigint       null,
    constraint username
        unique (username),
    constraint FKn82ha3ccdebhokx3a8fgdqeyy
        foreign key (role_id) references role (id)
);

create table employee_schedule
(
    id          bigint auto_increment
        primary key,
    employee_id bigint                                                    not null,
    shift_date  date                                                      not null,
    shift_type  enum ('morning', 'evening', 'night', 'off')               not null,
    status      enum ('scheduled', 'cancelled') default 'scheduled'       null,
    note        varchar(255)                                              null,
    created_at  timestamp                       default CURRENT_TIMESTAMP null,
    constraint UK8mmuurbc00b9iihjiwjhn4nv3
        unique (employee_id, shift_date, shift_type),
    constraint uq_employee_shift
        unique (employee_id, shift_date, shift_type),
    constraint fk_schedule_employee
        foreign key (employee_id) references user (id),
    constraint fk_shift_type
        foreign key (shift_type) references shift_template (shift_type)
);

create table reservation
(
    id            bigint auto_increment
        primary key,
    user_id       bigint                                                                  null,
    checkin_date  date                                                                    null,
    checkout_date date                                                                    null,
    guest_count   int                                                                     null,
    total_price   decimal(38, 2)                                                          null,
    status        enum ('預約中', '已入住', '已退房', '已取消') default '預約中'          null,
    note          text                                                                    null,
    created_at    datetime                                      default CURRENT_TIMESTAMP null,
    updated_at    datetime                                      default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    guest_name    varchar(255)                                                            null,
    guest_phone   varchar(255)                                                            null,
    constraint reservation_ibfk_1
        foreign key (user_id) references user (id)
);

create index user_id
    on reservation (user_id);

create table reservation_room
(
    reservation_id bigint not null,
    room_id        bigint not null,
    primary key (reservation_id, room_id),
    constraint reservation_room_ibfk_1
        foreign key (reservation_id) references reservation (id),
    constraint reservation_room_ibfk_2
        foreign key (room_id) references room (id)
);

create index room_id
    on reservation_room (room_id);

create table restaurant_reservation
(
    id               bigint auto_increment
        primary key,
    user_id          bigint                                                              null,
    reservation_time datetime                                                            not null,
    end_time         datetime                                                            not null,
    number_of_guests int                                                                 not null,
    note             varchar(255)                                                        null,
    status           enum ('BOOKED', 'CANCELLED', 'COMPLETED') default 'BOOKED'          null,
    created_at       timestamp                                 default CURRENT_TIMESTAMP null,
    guest_name       varchar(255)                                                        null,
    guest_phone      varchar(255)                                                        null,
    constraint restaurant_reservation_ibfk_1
        foreign key (user_id) references user (id)
);

create table reservation_table
(
    reservation_id bigint not null,
    table_id       bigint not null,
    primary key (reservation_id, table_id),
    constraint reservation_table_ibfk_1
        foreign key (reservation_id) references restaurant_reservation (id),
    constraint reservation_table_ibfk_2
        foreign key (table_id) references restaurant_table (id)
);

create index table_id
    on reservation_table (table_id);

create index user_id
    on restaurant_reservation (user_id);


INSERT INTO hotel_db.role (id, name) VALUES (1, 'admin');
INSERT INTO hotel_db.role (id, name) VALUES (2, 'staff');
INSERT INTO hotel_db.role (id, name) VALUES (3, 'customer');




INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('admin01', '123456', 'Admin Account', 'admin@example.com', '0912345678', 1);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff01', '123456', 'Sean Lin', 'staff@example.com', '0912345678', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff03', '123456', 'Michael Hsu', 'staff2@example.com', '0912345678', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_john01', 'pass1234', 'John Wu', 'john01@example.com', '0912000001', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_amy02', 'qwerty5678', 'Amy Tsai', 'amy02@example.com', '0912000002', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_tom03', 'tompass99', 'Tom Lin', 'tom03@example.com', '0912000003', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_lily04', 'abcdef12', 'Lily Huang', 'lily04@example.com', '0912000004', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_mike05', 'm1kepass', 'Mike Chen', 'mike05@example.com', '0912000005', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_sue06', 'suepass77', 'Sue Hsu', 'sue06@example.com', '0912000006', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_ray07', 'rayray88', 'Ray Kao', 'ray07@example.com', '0912000007', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_ivy08', 'ivypw345', 'Ivy Lee', 'ivy08@example.com', '0912000008', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_ben09', 'benben11', 'Ben Wang', 'ben09@example.com', '0912000009', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('cust_yen10', 'yen2345', 'Yen Shih', 'yen10@example.com', '0912000010', 3);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_alex01', 'staff001', 'Alex Chiu', 'alex01@company.com', '0922000001', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_tina02', 'tinapw09', 'Tina Yeh', 'tina02@company.com', '0922000002', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_joe03', 'joepass33', 'Joe Kuo', 'joe03@company.com', '0922000003', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_judy04', 'judy88', 'Judy Chang', 'judy04@company.com', '0922000004', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_max05', 'maxxx123', 'Max Lai', 'max05@company.com', '0922000005', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_lan06', 'lanpw5566', 'Lan Chen', 'lan06@company.com', '0922000006', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_ron07', 'ron12345', 'Ron Lu', 'ron07@company.com', '0922000007', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_viv08', 'viv321', 'Vivian Fang', 'viv08@company.com', '0922000008', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_ken09', 'kenpass99', 'Ken Hsieh', 'ken09@company.com', '0922000009', 2);
INSERT INTO hotel_db.user (username, password, full_name, email, phone, role_id) VALUES ('staff_fay10', 'faypw001', 'Fay Lin', 'fay10@company.com', '0922000010', 2);




INSERT INTO hotel_db.room_type (name, base_price) VALUES ('單人房', 1500.00);
INSERT INTO hotel_db.room_type (name, base_price) VALUES ('雙人房', 2500.00);
INSERT INTO hotel_db.room_type (name, base_price) VALUES ('家庭房', 4000.00);
INSERT INTO hotel_db.room_type (name, base_price) VALUES ('套房', 6000.00);



INSERT INTO hotel_db.room (room_number, type_id, is_active) VALUES ('101', 1, 1);
INSERT INTO hotel_db.room (room_number, type_id, is_active) VALUES ('102', 1, 1);
INSERT INTO hotel_db.room (room_number, type_id, is_active) VALUES ('201', 2, 1);
INSERT INTO hotel_db.room (room_number, type_id, is_active) VALUES ('202', 2, 1);
INSERT INTO hotel_db.room (room_number, type_id, is_active) VALUES ('301', 3, 1);
INSERT INTO hotel_db.room (room_number, type_id, is_active) VALUES ('302', 3, 1);
INSERT INTO hotel_db.room (room_number, type_id, is_active) VALUES ('401', 4, 1);
INSERT INTO hotel_db.room (room_number, type_id, is_active) VALUES ('402', 4, 1);



INSERT INTO hotel_db.shift_template (shift_type, start_time, end_time, is_cross_day) VALUES ('morning', '06:00:00', '14:00:00', 0);
INSERT INTO hotel_db.shift_template (shift_type, start_time, end_time, is_cross_day) VALUES ('evening', '14:00:00', '22:00:00', 0);
INSERT INTO hotel_db.shift_template (shift_type, start_time, end_time, is_cross_day) VALUES ('night', '22:00:00', '06:00:00', 0);
INSERT INTO hotel_db.shift_template (shift_type, start_time, end_time, is_cross_day) VALUES ('off', '00:00:00', '00:00:00', 0);



INSERT INTO hotel_db.restaurant_table (name, capacity, status) VALUES ('A1', 2, 'AVAILABLE');
INSERT INTO hotel_db.restaurant_table (name, capacity, status) VALUES ('A2', 2, 'AVAILABLE');
INSERT INTO hotel_db.restaurant_table (name, capacity, status) VALUES ('B1', 4, 'AVAILABLE');
INSERT INTO hotel_db.restaurant_table (name, capacity, status) VALUES ('B2', 4, 'AVAILABLE');
INSERT INTO hotel_db.restaurant_table (name, capacity, status) VALUES ('C1', 6, 'AVAILABLE');
INSERT INTO hotel_db.restaurant_table (name, capacity, status) VALUES ('VIP1', 10, 'AVAILABLE');



