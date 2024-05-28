CREATE TABLE User
(
    user_id    INT AUTO_INCREMENT,
    full_name  VARCHAR(64),
    username   VARCHAR(64),
    `password` VARCHAR(128),
    phone      VARCHAR(16),
    dob        DATE,
    gender     CHAR(1),
    avatar     TEXT,
    `role`     CHAR,
    email      varchar(96),
    PRIMARY KEY (user_id)
);

CREATE TABLE Organizer
(
    organizer_id   INT,
    founded_date   DATE,
    website        TEXT,
    is_active      Boolean,
    organizer_addr VARCHAR(256),
    organizer_type VARCHAR(64),
    PRIMARY KEY (organizer_id),
    FOREIGN KEY (organizer_id) REFERENCES User (user_id)
);

CREATE TABLE Event
(
    event_id         INT AUTO_INCREMENT,
    `name`           VARCHAR(64),
    `description`    TEXT,
    start_date       DATE,
    location         VARCHAR(225),
    `type`           VARCHAR(64),
    ticket_sale_date DATE,
    end_date         DATE,
    organizer_id     INT,
    seat_map         TEXT,
    poster           TEXT,
    banner           TEXT,
    PRIMARY KEY (event_id),
    FOREIGN KEY (organizer_id) REFERENCES Organizer (organizer_id)
);

CREATE TABLE `Order`
(
    order_id INT AUTO_INCREMENT,
    date     DATE,
    total    INT,
    user_id  INT,
    PRIMARY KEY (order_id),
    FOREIGN KEY (user_id) REFERENCES User (user_id)
);

CREATE TABLE Rating
(
    rating_id INT AUTO_INCREMENT,
    star      INT,
    event_id  INT,
    order_id  INT,
    PRIMARY KEY (rating_id),
    FOREIGN KEY (event_id) REFERENCES Event (event_id),
    FOREIGN KEY (order_id) REFERENCES `Order` (order_id)
);

CREATE TABLE Area
(
    area_id       INT AUTO_INCREMENT,
    name          VARCHAR(64),
    total_tickets INT,
    ticket_price  FLOAT,
    event_id      INT,
    PRIMARY KEY (area_id),
    FOREIGN KEY (event_id) REFERENCES Event (event_id)
);

CREATE TABLE `Row`
(
    row_name VARCHAR(16),
    row_id   INT AUTO_INCREMENT,
    area_id  INT,
    PRIMARY KEY (row_id),
    FOREIGN KEY (area_id) REFERENCES Area (area_id)
);

CREATE TABLE RefundOrder
(
    refundoder_id INT AUTO_INCREMENT,
    date          DATE,
    user_id       INT,
    order_id      INT,
    PRIMARY KEY (refundoder_id),
    FOREIGN KEY (user_id) REFERENCES User (user_id),
    FOREIGN KEY (order_id) REFERENCES `Order` (order_id)
);

CREATE TABLE Seat
(
    seat_id      INT AUTO_INCREMENT,
    number       VARCHAR(64),
    ticket_price FLOAT,
    is_buy       BOOLEAN,
    is_checkedin BOOLEAN,
    row_id       INT,
    PRIMARY KEY (seat_id),
    FOREIGN KEY (row_id) REFERENCES `Row` (row_id)
);

CREATE TABLE Ticket
(
    ticket_id     INT AUTO_INCREMENT,
    qr_code       TEXT,
    purchase_date DATE,
    order_id      INT,
    seat_id       INT,
    PRIMARY KEY (ticket_id),
    FOREIGN KEY (order_id) REFERENCES `Order` (order_id),
    FOREIGN KEY (seat_id) REFERENCES Seat (seat_id)
);
create table PasswordResetToken
(
    id          int primary key auto_increment,
    user_id     int,
    token       varchar(256),
    expiry_date timestamp,
    created_at  timestamp,
    FOREIGN KEY (user_id) REFERENCES `User` (user_id)
);
create table UserSecrets
(
    id         int primary key auto_increment,
    user_id    int,
    secret_key varchar(256),
    FOREIGN KEY (user_id) REFERENCES `User` (user_id)

);

create table googleOauth
(
    client_id     varchar(255),
    client_secret varchar(255),
    redirect_uri  varchar(255)
);

CREATE TABLE UnverifiedUsers
(
    userid     INT NOT NULL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE PROCEDURE InsertOrganizer(
    IN p_full_name VARCHAR(50),
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(50),
    IN p_phone VARCHAR(10),
    IN p_dob DATE,
    IN p_gender CHAR(1),
    IN p_avatar TEXT,
    IN p_role CHAR,
    IN p_email VARCHAR(100),
    IN p_founded_date DATE,
    IN p_website TEXT,
    IN p_is_active BOOLEAN,
    IN p_organizer_addr VARCHAR(225),
    IN p_organizer_type VARCHAR(50),
    OUT p_new_user_id INT
)
BEGIN
    -- Insert User attributes
    INSERT INTO User (full_name, username, `password`, phone, dob, gender, avatar, `role`, email)
    VALUES (p_full_name, p_username, p_password, p_phone, p_dob, p_gender, p_avatar, p_role, p_email);

    -- Get the newly created user_id
    SET p_new_user_id = LAST_INSERT_ID();

    -- Insert Organizer attributes
    INSERT INTO Organizer (organizer_id, founded_date, website, is_active, organizer_addr, organizer_type)
    VALUES (p_new_user_id, p_founded_date, p_website, p_is_active, p_organizer_addr, p_organizer_type);
END;


CREATE VIEW OrganizerDetails AS
SELECT o.*,
       u.full_name,
       u.username,
       u.`password`,
       u.phone,
       u.dob,
       u.gender,
       u.avatar,
       u.`role`,
       u.email
FROM Organizer o
         JOIN User u ON o.organizer_id = u.user_id;


CREATE VIEW ticket_with_user AS
SELECT Ticket.*,
       `Order`.user_id
FROM Ticket
         JOIN
     `Order` ON Ticket.order_id = `Order`.order_id;
