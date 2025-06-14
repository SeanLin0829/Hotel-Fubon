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
    user_id          bigint                                                                            null,
    reservation_time datetime                                                                          not null,
    end_time         datetime                                                                          not null,
    number_of_guests int                                                                               not null,
    note             varchar(255)                                                                      null,
    status           enum ('BOOKED', 'CANCELLED', 'COMPLETED', 'CHECKED_IN') default 'BOOKED'          null,
    created_at       timestamp                                               default CURRENT_TIMESTAMP null,
    guest_name       varchar(255)                                                                      null,
    guest_phone      varchar(255)                                                                      null,
    constraint restaurant_reservation_ibfk_1
        foreign key (user_id) references user (id)
);

create index user_id
    on restaurant_reservation (user_id);

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


INSERT INTO hotel_db.role (name) VALUES ('admin');
INSERT INTO hotel_db.role (name) VALUES ('staff');
INSERT INTO hotel_db.role (name) VALUES ('customer');




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



INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (5, '2025-06-14 11:30:00', '2025-06-14 13:00:00', 4, '', 'COMPLETED', '2025-06-14 17:22:26', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (4, '2025-06-14 16:30:00', '2025-06-14 18:00:00', 2, '慶生', 'COMPLETED', '2025-06-14 18:58:58', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (null, '2025-06-14 19:30:00', '2025-06-14 21:00:00', 4, '甲殼類過敏', 'COMPLETED', '2025-06-14 18:59:41', '王小明', '0912345678');
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (4, '2025-06-17 17:00:00', '2025-06-17 18:30:00', 3, '慶生', 'BOOKED', '2025-06-14 19:09:02', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (null, '2025-06-17 18:00:00', '2025-06-17 19:30:00', 2, '', 'BOOKED', '2025-06-14 19:09:29', '孫小美', '0987654321');
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (9, '2025-06-18 12:00:00', '2025-06-18 13:30:00', 10, '同學會', 'BOOKED', '2025-06-14 19:10:12', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (4, '2025-06-18 18:00:00', '2025-06-18 19:30:00', 4, '會員預約 - 生日慶祝', 'BOOKED', '2025-06-14 20:15:38', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (5, '2025-06-18 18:00:00', '2025-06-18 19:30:00', 4, '', 'BOOKED', '2025-06-14 20:16:06', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (5, '2025-06-19 18:00:00', '2025-06-19 19:30:00', 4, '會員預約 - 生日慶祝', 'BOOKED', '2025-06-14 20:16:38', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (5, '2025-06-20 18:00:00', '2025-06-20 19:30:00', 4, '會員預約 - 生日慶祝', 'BOOKED', '2025-06-14 20:16:42', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (5, '2025-06-21 18:00:00', '2025-06-21 19:30:00', 4, '會員預約 - 生日慶祝', 'BOOKED', '2025-06-14 20:16:46', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (5, '2025-06-22 18:00:00', '2025-06-22 19:30:00', 4, '會員預約 - 生日慶祝', 'BOOKED', '2025-06-14 20:16:49', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (5, '2025-06-23 18:00:00', '2025-06-23 19:30:00', 4, '會員預約 - 生日慶祝', 'BOOKED', '2025-06-14 20:16:53', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (6, '2025-06-19 18:30:00', '2025-06-19 20:00:00', 4, '', 'BOOKED', '2025-06-14 20:17:53', null, null);
INSERT INTO hotel_db.restaurant_reservation (user_id, reservation_time, end_time, number_of_guests, note, status, created_at, guest_name, guest_phone) VALUES (7, '2025-06-20 18:30:00', '2025-06-20 20:00:00', 4, '', 'BOOKED', '2025-06-14 20:18:13', null, null);




INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (5, 1);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (7, 1);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (9, 1);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (10, 1);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (11, 1);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (12, 1);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (13, 1);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (2, 2);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (7, 2);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (9, 2);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (10, 2);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (11, 2);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (12, 2);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (13, 2);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (1, 3);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (4, 3);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (8, 3);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (3, 4);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (8, 4);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (14, 5);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (15, 5);
INSERT INTO hotel_db.reservation_table (reservation_id, table_id) VALUES (6, 6);


INSERT INTO hotel_db.reservation (user_id, checkin_date, checkout_date, guest_count, total_price, status, note, created_at, updated_at, guest_name, guest_phone) VALUES (4, '2025-06-14', '2025-06-15', 2, 6500.00, '已退房', '', '2025-06-14 17:03:07', '2025-06-14 18:47:55', null, null);
INSERT INTO hotel_db.reservation (user_id, checkin_date, checkout_date, guest_count, total_price, status, note, created_at, updated_at, guest_name, guest_phone) VALUES (5, '2025-06-14', '2025-06-20', 2, 36000.00, '已入住', '高樓層', '2025-06-14 18:47:18', '2025-06-14 18:47:58', null, null);
INSERT INTO hotel_db.reservation (user_id, checkin_date, checkout_date, guest_count, total_price, status, note, created_at, updated_at, guest_name, guest_phone) VALUES (null, '2025-06-17', '2025-06-27', 4, 40000.00, '預約中', '無菸房', '2025-06-14 18:54:19', '2025-06-14 18:54:19', '王小明', '0912345678');
INSERT INTO hotel_db.reservation (user_id, checkin_date, checkout_date, guest_count, total_price, status, note, created_at, updated_at, guest_name, guest_phone) VALUES (9, '2025-06-18', '2025-06-25', 4, 35000.00, '預約中', '', '2025-06-14 18:55:22', '2025-06-14 18:55:22', null, null);
INSERT INTO hotel_db.reservation (user_id, checkin_date, checkout_date, guest_count, total_price, status, note, created_at, updated_at, guest_name, guest_phone) VALUES (11, '2025-06-19', '2025-06-30', 4, 44000.00, '預約中', '', '2025-06-14 18:55:49', '2025-06-14 18:55:49', null, null);

INSERT INTO hotel_db.reservation_room (reservation_id, room_id) VALUES (4, 3);
INSERT INTO hotel_db.reservation_room (reservation_id, room_id) VALUES (1, 4);
INSERT INTO hotel_db.reservation_room (reservation_id, room_id) VALUES (4, 4);
INSERT INTO hotel_db.reservation_room (reservation_id, room_id) VALUES (1, 5);
INSERT INTO hotel_db.reservation_room (reservation_id, room_id) VALUES (5, 5);
INSERT INTO hotel_db.reservation_room (reservation_id, room_id) VALUES (3, 6);
INSERT INTO hotel_db.reservation_room (reservation_id, room_id) VALUES (2, 7);



INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (2, '2025-06-05', 'morning', 'scheduled', '', '2025-06-14 16:42:01');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (2, '2025-06-06', 'morning', 'scheduled', '', '2025-06-14 16:45:41');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (2, '2025-06-07', 'morning', 'scheduled', '', '2025-06-14 16:45:42');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (2, '2025-06-08', 'morning', 'scheduled', '', '2025-06-14 16:45:43');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (2, '2025-06-09', 'morning', 'scheduled', '', '2025-06-14 16:45:44');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (2, '2025-06-10', 'morning', 'scheduled', '', '2025-06-14 16:45:45');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (3, '2025-06-05', 'morning', 'scheduled', '', '2025-06-14 18:28:43');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (3, '2025-06-06', 'morning', 'scheduled', '', '2025-06-14 18:28:44');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (3, '2025-06-07', 'morning', 'scheduled', '', '2025-06-14 18:28:45');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (3, '2025-06-08', 'morning', 'scheduled', '', '2025-06-14 18:28:47');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (3, '2025-06-09', 'off', 'scheduled', '', '2025-06-14 18:28:48');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (3, '2025-06-10', 'off', 'scheduled', '', '2025-06-14 18:28:50');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (14, '2025-06-05', 'evening', 'scheduled', '', '2025-06-14 18:28:51');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (14, '2025-06-06', 'evening', 'scheduled', '', '2025-06-14 18:28:53');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (14, '2025-06-07', 'off', 'scheduled', '', '2025-06-14 18:28:54');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (14, '2025-06-08', 'evening', 'scheduled', '', '2025-06-14 18:28:56');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (14, '2025-06-09', 'evening', 'scheduled', '', '2025-06-14 18:28:57');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (14, '2025-06-10', 'evening', 'scheduled', '', '2025-06-14 18:28:58');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (15, '2025-06-05', 'evening', 'scheduled', '', '2025-06-14 18:29:00');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (15, '2025-06-06', 'off', 'scheduled', '', '2025-06-14 18:29:02');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (15, '2025-06-07', 'evening', 'scheduled', '', '2025-06-14 18:29:04');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (15, '2025-06-08', 'evening', 'scheduled', '', '2025-06-14 18:29:05');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (15, '2025-06-09', 'evening', 'scheduled', '', '2025-06-14 18:29:07');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (15, '2025-06-10', 'evening', 'scheduled', '', '2025-06-14 18:29:08');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (16, '2025-06-08', 'evening', 'scheduled', '', '2025-06-14 18:29:10');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (16, '2025-06-07', 'evening', 'scheduled', '', '2025-06-14 18:29:11');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (16, '2025-06-06', 'evening', 'scheduled', '', '2025-06-14 18:29:15');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (16, '2025-06-05', 'off', 'scheduled', '', '2025-06-14 18:29:17');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (16, '2025-06-09', 'off', 'scheduled', '', '2025-06-14 18:29:20');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (16, '2025-06-10', 'morning', 'scheduled', '', '2025-06-14 18:29:23');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (17, '2025-06-05', 'night', 'scheduled', '', '2025-06-14 18:29:24');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (17, '2025-06-06', 'night', 'scheduled', '', '2025-06-14 18:29:26');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (17, '2025-06-07', 'night', 'scheduled', '', '2025-06-14 18:29:27');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (17, '2025-06-08', 'night', 'scheduled', '', '2025-06-14 18:29:29');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (17, '2025-06-09', 'night', 'scheduled', '', '2025-06-14 18:29:30');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (17, '2025-06-10', 'off', 'scheduled', '', '2025-06-14 18:29:31');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (18, '2025-06-05', 'night', 'scheduled', '', '2025-06-14 18:29:32');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (18, '2025-06-06', 'night', 'scheduled', '', '2025-06-14 18:29:34');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (18, '2025-06-07', 'night', 'scheduled', '', '2025-06-14 18:29:35');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (18, '2025-06-08', 'off', 'scheduled', '', '2025-06-14 18:29:37');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (18, '2025-06-09', 'morning', 'scheduled', '', '2025-06-14 18:29:39');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (18, '2025-06-10', 'night', 'scheduled', '', '2025-06-14 18:29:40');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (19, '2025-06-05', 'off', 'scheduled', '', '2025-06-14 18:29:42');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (19, '2025-06-06', 'off', 'scheduled', '', '2025-06-14 18:29:43');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (19, '2025-06-07', 'off', 'scheduled', '', '2025-06-14 18:29:45');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (19, '2025-06-08', 'night', 'scheduled', '', '2025-06-14 18:29:48');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (19, '2025-06-09', 'night', 'scheduled', '', '2025-06-14 18:29:53');
INSERT INTO hotel_db.employee_schedule (employee_id, shift_date, shift_type, status, note, created_at) VALUES (19, '2025-06-10', 'night', 'scheduled', '', '2025-06-14 18:29:54');





