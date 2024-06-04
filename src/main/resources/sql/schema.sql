CREATE DATABASE VieTicket1;
USE VieTicket1;

CREATE TABLE User
(
    user_id    INT AUTO_INCREMENT
        PRIMARY KEY,
    full_name  VARCHAR(64),
    username   VARCHAR(64),
    `password` VARCHAR(128),
    phone      VARCHAR(16),
    dob        DATE,
    gender     CHAR,
    avatar     TEXT,
    `role`     CHAR,
    email      VARCHAR(96)
);

CREATE TABLE Organizer
(
    organizer_id   INT NOT NULL
        PRIMARY KEY,
    FOREIGN KEY (organizer_id) REFERENCES User (user_id),
    founded_date   DATE,
    website        TEXT,
    is_active      TINYINT,
    organizer_addr VARCHAR(256),
    organizer_type VARCHAR(64)
);

CREATE TABLE Event
(
    event_id         INT AUTO_INCREMENT
        PRIMARY KEY,
    `name`           VARCHAR(64),
    `description`    TEXT,
    start_date       DATETIME,
    location         VARCHAR(256),
    `type`           VARCHAR(64),
    ticket_sale_date DATETIME,
    end_date         DATETIME,
    organizer_id     INT,
    FOREIGN KEY (organizer_id) REFERENCES Organizer (organizer_id),
    poster           TEXT,
    banner           TEXT,
    is_approve       int
);

CREATE TABLE SeatMap
(
    seat_map_id INT AUTO_INCREMENT
        PRIMARY KEY,
    event_id    INT UNIQUE,
    FOREIGN KEY (event_id) REFERENCES Event (event_id) ON DELETE CASCADE,
    `name`      VARCHAR(64),
    img         TEXT
);

CREATE TABLE `Order`
(
    order_id   INT AUTO_INCREMENT
        PRIMARY KEY,
    date       DATETIME,
    total      INT,
    user_id    INT,
    FOREIGN KEY (user_id) REFERENCES User (user_id),
    vnpay_data TEXT
);

CREATE TABLE Rating
(
    rating_id INT AUTO_INCREMENT
        PRIMARY KEY,
    star      INT,
    event_id  INT,
    FOREIGN KEY (event_id) REFERENCES Event (event_id),
    order_id  INT,
    FOREIGN KEY (order_id) REFERENCES `Order` (order_id)
);

CREATE TABLE Area
(
    area_id       INT AUTO_INCREMENT
        PRIMARY KEY,
    name          VARCHAR(64),
    total_tickets INT,
    ticket_price  FLOAT,
    event_id      INT,
    FOREIGN KEY (event_id) REFERENCES Event (event_id) ON DELETE CASCADE,
    seat_map_id   INT,
    FOREIGN KEY (seat_map_id) REFERENCES SeatMap (seat_map_id) ON DELETE CASCADE
);

CREATE TABLE `Row`
(
    row_id   INT AUTO_INCREMENT
        PRIMARY KEY,
    row_name VARCHAR(16),
    area_id  INT,
    FOREIGN KEY (area_id) REFERENCES Area (area_id) ON DELETE CASCADE
);

CREATE TABLE RefundOrder
(
    refundoder_id INT AUTO_INCREMENT
        PRIMARY KEY,
    date          DATETIME,
    user_id       INT,
    FOREIGN KEY (user_id) REFERENCES User (user_id),
    order_id      INT,
    FOREIGN KEY (order_id) REFERENCES `Order` (order_id),
    vnpay_data    TEXT
);

CREATE TABLE Seat
(
    seat_id      INT AUTO_INCREMENT
        PRIMARY KEY,
    number       VARCHAR(64),
    ticket_price FLOAT,
    is_taken     BOOLEAN,
    row_id       INT,
    FOREIGN KEY (row_id) REFERENCES `Row` (row_id) ON DELETE CASCADE
);

CREATE TABLE Ticket
(
    ticket_id     INT AUTO_INCREMENT
        PRIMARY KEY,
    qr_code       VARCHAR(64) UNIQUE,
    INDEX qr_code_index (qr_code),
    purchase_date DATETIME,
    order_id      INT,
    FOREIGN KEY (order_id) REFERENCES `Order` (order_id),
    seat_id       INT,
    is_returned   BOOLEAN,
    is_checked_in BOOLEAN,
    FOREIGN KEY (seat_id) REFERENCES Seat (seat_id)
);

CREATE TABLE PasswordResetToken
(
    id          INT AUTO_INCREMENT
        PRIMARY KEY,
    user_id     INT,
    FOREIGN KEY (user_id) REFERENCES `User` (user_id),
    token       VARCHAR(256),
    expiry_date TIMESTAMP,
    created_at  TIMESTAMP
);

CREATE TABLE UserSecrets
(
    id         INT AUTO_INCREMENT
        PRIMARY KEY,
    user_id    INT,
    FOREIGN KEY (user_id) REFERENCES `User` (user_id),
    secret_key VARCHAR(256)

);

CREATE TABLE UnverifiedUsers
(
    userid     INT NOT NULL
        PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE OrganizerVerificationToken
(
    id            INT AUTO_INCREMENT
        PRIMARY KEY,
    organizer_id  INT,
    FOREIGN KEY (organizer_id) REFERENCES Organizer (organizer_id),
    token         VARCHAR(256),
    familiar_name VARCHAR(64) UNIQUE,
    expiry_date   TIMESTAMP,
    created_at    TIMESTAMP
);

CREATE TABLE googleOauth
(
    client_id     VARCHAR(255),
    client_secret VARCHAR(255),
    redirect_uri  VARCHAR(255)
);

CREATE VIEW OrganizerDetails AS
SELECT `o`.`organizer_id`   AS `organizer_id`,
       `o`.`founded_date`   AS `founded_date`,
       `o`.`website`        AS `website`,
       `o`.`is_active`      AS `is_active`,
       `o`.`organizer_addr` AS `organizer_addr`,
       `o`.`organizer_type` AS `organizer_type`,
       `u`.`full_name`      AS `full_name`,
       `u`.`username`       AS `username`,
       `u`.`password`       AS `password`,
       `u`.`phone`          AS `phone`,
       `u`.`dob`            AS `dob`,
       `u`.`gender`         AS `gender`,
       `u`.`avatar`         AS `avatar`,
       `u`.`role`           AS `role`,
       `u`.`email`          AS `email`
FROM (`VieTicket1`.`Organizer` `o` JOIN `VieTicket1`.`User` `u` ON ((`o`.`organizer_id` = `u`.`user_id`)));

CREATE VIEW ticket_with_user AS
SELECT `VieTicket1`.`Ticket`.`ticket_id`     AS `ticket_id`,
       `VieTicket1`.`Ticket`.`qr_code`       AS `qr_code`,
       `VieTicket1`.`Ticket`.`purchase_date` AS `purchase_date`,
       `VieTicket1`.`Ticket`.`order_id`      AS `order_id`,
       `VieTicket1`.`Ticket`.`seat_id`       AS `seat_id`,
       `VieTicket1`.`Ticket`.`is_returned`   AS `is_returned`,
       `VieTicket1`.`Ticket`.is_checked_in   AS `is_checked_in`,
       `VieTicket1`.`Order`.`user_id`        AS `user_id`
FROM (`VieTicket1`.`Ticket` JOIN `VieTicket1`.`Order`
      ON ((`VieTicket1`.`Ticket`.`order_id` = `VieTicket1`.`Order`.`order_id`)));

DELIMITER $$

CREATE PROCEDURE InsertOrganizer(IN p_full_name VARCHAR(64), IN p_username VARCHAR(64),
                                 IN p_password VARCHAR(128), IN p_phone VARCHAR(16), IN p_dob DATE,
                                 IN p_gender CHAR, IN p_avatar TEXT, IN p_role CHAR,
                                 IN p_email VARCHAR(96), IN p_founded_date DATE, IN p_website TEXT,
                                 IN p_is_active TINYINT, IN p_organizer_addr VARCHAR(256),
                                 IN p_organizer_type VARCHAR(64), OUT p_new_user_id INT)
BEGIN
    -- Insert User attributes
    INSERT INTO User (full_name, username, `password`, phone, dob, gender, avatar, `role`, email)
    VALUES (p_full_name, p_username, p_password, p_phone, p_dob, p_gender, p_avatar, p_role, p_email);

    -- Get the newly created user_id
    SET p_new_user_id = LAST_INSERT_ID();

    -- Insert Organizer attributes
    INSERT INTO Organizer (organizer_id, founded_date, website, is_active, organizer_addr, organizer_type)
    VALUES (p_new_user_id, p_founded_date, p_website, p_is_active, p_organizer_addr, p_organizer_type);
END $$

CREATE PROCEDURE UpdateOrganizer(IN p_organizer_id INT, IN p_full_name VARCHAR(64),
                                 IN p_username VARCHAR(64), IN p_password VARCHAR(128),
                                 IN p_phone VARCHAR(16), IN p_dob DATE, IN p_gender CHAR,
                                 IN p_avatar TEXT, IN p_role CHAR, IN p_email VARCHAR(96),
                                 IN p_founded_date DATE, IN p_website TEXT, IN p_is_active TINYINT,
                                 IN p_organizer_addr VARCHAR(256), IN p_organizer_type VARCHAR(64))
BEGIN
    -- Update User attributes
    UPDATE User
    SET full_name  = p_full_name,
        username   = p_username,
        `password` = p_password,
        phone      = p_phone,
        dob        = p_dob,
        gender     = p_gender,
        avatar     = p_avatar,
        `role`     = p_role,
        email      = p_email
    WHERE user_id = p_organizer_id;

    -- Update Organizer attributes
    UPDATE Organizer
    SET founded_date   = p_founded_date,
        website        = p_website,
        is_active      = p_is_active,
        organizer_addr = p_organizer_addr,
        organizer_type = p_organizer_type
    WHERE organizer_id = p_organizer_id;
END $$
DELIMITER ;

-- @block

-- Insert Data

-- Table User
-- All passwords: 123456
INSERT INTO User (full_name, username, password, phone, dob, gender, avatar, role, email)
VALUES ('Nguyễn Bảo Việt', 'admin', '$2a$10$sJw8p99iHalBmTkimlQUFuWRvG.wGqyhqk4ixMuBHIAp9R/O2.oTa', '0123456789',
        '1990-01-01', 'M', 'http://res.cloudinary.com/djcowpoua/image/upload/v1716728282/l64joxafrjbnonptizds.jpg', 'a',
        'vietnb6@hoangnpv.id.vn'),
       ('Nguyễn Văn A', 'nguyenvana', '$2a$10$0DYtcWrBruHHtvzDKGfNX.qZTbonjyZ6D1KvqELoli/DgXEwW0TAy', '0123456789',
        '1990-01-01', 'M', 'http://res.cloudinary.com/djcowpoua/image/upload/v1716339770/hla22jkantkibhjdtay2.jpg', 'u',
        'hoangnpv@hoangnpv.id.vn'),
       ('Trần Thị B', 'o1', '$2a$10$0DYtcWrBruHHtvzDKGfNX.qZTbonjyZ6D1KvqELoli/DgXEwW0TAy', '0987654321', '1995-05-15',
        'F', 'http://res.cloudinary.com/djcowpoua/image/upload/v1716339881/qxagzwjvk1hzuq1tgbis.jpg', 'o',
        'azzv8qn375d52608ff@gmail.com'),
       ('Ngô Trần Xuân H', 'o2', '$2a$10$0DYtcWrBruHHtvzDKGfNX.qZTbonjyZ6D1KvqELoli/DgXEwW0TAy', '0989999999',
        '1995-05-25', 'M', 'http://res.cloudinary.com/djcowpoua/image/upload/v1716339943/th4nqqufozfkvrfy8yoo.jpg', 'o',
        'o1@hoangnpv.id.vn');

-- Table Organizer
INSERT INTO Organizer (organizer_id, founded_date, website, is_active, organizer_addr, organizer_type)
VALUES (3, '2021-01-01', 'https://www.hoangnpv.id.vn', 1, 'Hà Nội', 'Music'),
       (4, '2021-01-01', 'https://www.hoangnpv.id.vn', 1, 'Hà Nội', 'Music');

-- Table Event
INSERT INTO Event (name, description, start_date, location, type, ticket_sale_date, end_date,
                   organizer_id, poster, banner, is_approve)
VALUES ('NTPMM', 'Những thành phố mơ màng', '2024-06-01', 'Hà Nội', 'Music', '2024-05-20', '2024-06-02', 3,
        'https://salt.tkbcdn.com/ts/ds/68/6f/ba/6f8121616c6643b2cbb3868b26bb4331.jpg',
        'https://salt.tkbcdn.com/ts/ds/68/6f/ba/6f8121616c6643b2cbb3868b26bb4331.jpg', 1),
       ('DIFF 2024', 'Lễ hội pháo bông', '2024-06-03', 'Hà Nội', 'Music', '2024-05-20', '2024-06-06', 4,
        'https://salt.tkbcdn.com/ts/ds/5d/61/dc/4ecd0c7c95090901b91c3cb2a62e2d85.jpg',
        'https://salt.tkbcdn.com/ts/ds/5d/61/dc/4ecd0c7c95090901b91c3cb2a62e2d85.jpg', 1),
       ('French Something something', 'Lễ hội pháo bông', '2024-06-03', 'Hà Nội', 'Music', '2024-05-20', '2024-06-06',
        3, 'https://salt.tkbcdn.com/ts/ds/75/d2/79/c9ead6b9c63b63dace78a75df05fa28c.png',
        'https://salt.tkbcdn.com/ts/ds/75/d2/79/c9ead6b9c63b63dace78a75df05fa28c.png', 1),
       ('Another Event', 'Lễ hội pháo bông', '2024-06-03', 'Hà Nội', 'Music', '2024-05-20', '2024-06-06', 4,
        'https://salt.tkbcdn.com/ts/ds/57/04/b1/821b5e71ce786b82aca489bfbce354e5.png',
        'https://salt.tkbcdn.com/ts/ds/57/04/b1/821b5e71ce786b82aca489bfbce354e5.png', 1),
       ('Rick Astley - Never Gonna Give You Up', 'Lễ hội pháo bông', '2024-06-03', 'Hà Nội', 'Music', '2024-05-20',
        '2024-06-06', 3, 'https://salt.tkbcdn.com/ts/ds/e8/60/2a/c80d33a955fc8f36a98fcbc1f120c750.jpg',
        'https://salt.tkbcdn.com/ts/ds/e8/60/2a/c80d33a955fc8f36a98fcbc1f120c750.jpg', 1);

