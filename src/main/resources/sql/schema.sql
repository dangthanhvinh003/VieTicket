
CREATE TABLE User
(
  user_id INT AUTO_INCREMENT,
  full_name VARCHAR(64),
  username VARCHAR(64),
  `password` VARCHAR(128),
  phone VARCHAR(16),
  dob DATE,
  gender CHAR(1),
  avatar TEXT,
  `role` CHAR,
  email varchar(96),
  PRIMARY KEY (user_id)
);

CREATE TABLE Organizer
(
  organizer_id INT,
  founded_date DATE,
  website TEXT,
  is_active Boolean,
  organizer_addr VARCHAR(256),
  organizer_type VARCHAR(64),
  PRIMARY KEY (organizer_id),
  FOREIGN KEY (organizer_id) REFERENCES User(user_id)
);

CREATE TABLE Event
(
  event_id INT AUTO_INCREMENT,
  `name` VARCHAR(64),
  `description` TEXT,
  start_date DATE,
  location VARCHAR(225),
  `type` VARCHAR(64),
  ticket_sale_date DATE,
  end_date DATE,
  organizer_id INT,
  seat_map TEXT,
  PRIMARY KEY (event_id),
  FOREIGN KEY (organizer_id) REFERENCES Organizer(organizer_id)
);

CREATE TABLE `Order`
(
  order_id INT AUTO_INCREMENT,
  date DATE,
  total INT,
  user_id INT,
  PRIMARY KEY (order_id),
  FOREIGN KEY (user_id) REFERENCES User(user_id)
);

CREATE TABLE Rating
(
  rating_id INT AUTO_INCREMENT,
  star INT,
  event_id INT,
  order_id INT,
  PRIMARY KEY (rating_id),
  FOREIGN KEY (event_id) REFERENCES Event(event_id),
  FOREIGN KEY (order_id) REFERENCES `Order`(order_id)
);

CREATE TABLE Area
(
  area_id INT AUTO_INCREMENT,
  name VARCHAR(64),
  total_tickets INT,
  ticket_price FLOAT,
  event_id INT,
  PRIMARY KEY (area_id),
  FOREIGN KEY (event_id) REFERENCES Event(event_id)
);

CREATE TABLE `Row`
(
  row_name VARCHAR(16),
  row_id INT AUTO_INCREMENT,
  area_id INT,
  PRIMARY KEY (row_id),
  FOREIGN KEY (area_id) REFERENCES Area(area_id)
);

CREATE TABLE RefundOrder
(
  refundoder_id INT AUTO_INCREMENT,
  date DATE,
  user_id INT,
  order_id INT,
  PRIMARY KEY (refundoder_id),
  FOREIGN KEY (user_id) REFERENCES User(user_id),
  FOREIGN KEY (order_id) REFERENCES `Order`(order_id)
);

CREATE TABLE Seat
(
  seat_id INT AUTO_INCREMENT,
  number VARCHAR(64),
  ticket_price FLOAT,
  is_buy BOOLEAN,
  is_checkedin BOOLEAN,
  row_id INT,
  PRIMARY KEY (seat_id),
  FOREIGN KEY (row_id) REFERENCES `Row`(row_id)
);

CREATE TABLE Ticket
(
  ticket_id INT AUTO_INCREMENT,
  qr_code TEXT,
  purchase_date DATE,
  order_id INT,
  seat_id INT,
  PRIMARY KEY (ticket_id),
  FOREIGN KEY (order_id) REFERENCES `Order`(order_id),
  FOREIGN KEY (seat_id) REFERENCES Seat(seat_id)
);
create table PasswordResetToken(
 id int primary key auto_increment,
 user_id int,
 token varchar(256),
 expiry_date timestamp,
 created_at timestamp,
 FOREIGN KEY (user_id) REFERENCES `User`(user_id)
);
create table UserSecrets(
 id int primary key auto_increment,
 user_id int,
 secret_key varchar(256),
 FOREIGN KEY (user_id) REFERENCES `User`(user_id)
 
);

INSERT INTO User (full_name, username, `password`, phone, dob, gender, avatar, `role`,email)
VALUES 
('Nguyễn Văn A', 'nguyenvana', '123456', '0123456789', '1990-01-01', 'M', 'avatar1.jpg', 'o','hoangnpvde180297@fpt.edu.vn'),
('Trần Thị B', 'tranthib', 'abcdef', '0987654321', '1995-05-15', 'F', 'avatar2.jpg', 'o','vinh1132@gmail.com'),
('Lê Văn C', 'levanc', 'qwerty', '0369852147', '1988-08-20', 'M', 'avatar3.jpg', 'o','vinh1132@gmail.com'),
('Phạm Thị D', 'phamthid', 'zxcvbn', '0912345678', '1998-12-10', 'F', 'avatar4.jpg', 'o','vinh1132@gmail.com'),
('Hoàng Văn E', 'hoangvane', 'asdfgh', '0847563219', '1993-04-05', 'M', 'avatar5.jpg', 'o','vinh1132@gmail.com'),
('Hoàng Tấn E', 'hoangvane', 'asdf43', '0843424219', '1995-04-05', 'M', 'avatar6.jpg', 'u','vinh1132@gmail.com');

INSERT INTO Organizer (organizer_id, founded_date, website, is_active ,organizer_addr, organizer_type)
VALUES 
(1, '2010-01-01', 'www.organizer1.com', true,'123 ABC Street, HCM City', 'Business'),
(2, '2015-06-20', 'www.organizer2.com', true,'456 XYZ Street, Hanoi', 'Non-profit'),
(3, '2005-03-10', 'www.organizer3.com', true,'789 DEF Street, Da Nang', 'Community'),
(4, '2012-09-15', 'www.organizer4.com', true,'246 GHI Street, Hue', 'Business'),
(5, '2018-11-25', 'www.organizer5.com', true,'135 JKL Street, Can Tho', 'Non-profit');

INSERT INTO Event (`name`, `description`, start_date, location, `type`, ticket_sale_date, end_date, organizer_id)
VALUES 
('Event A', 'Description for Event A', '2024-06-01', 'ABC Stadium', 'Music', '2024-05-20', '2024-06-02', 1),
('Event B', 'Description for Event B', '2024-07-15', 'XYZ Hall', 'Conference', '2024-07-01', '2024-07-17', 2),
('Event C', 'Description for Event C', '2024-08-10', 'DEF Arena', 'Sports', '2024-07-25', '2024-08-12', 3),
('Event D', 'Description for Event D', '2024-09-05', 'GHI Center', 'Exhibition', '2024-08-20', '2024-09-07', 4),
('Event E', 'Description for Event E', '2024-10-20', 'JKL Pavilion', 'Arts', '2024-10-01', '2024-10-25', 5);

INSERT INTO `Order` (date, total, user_id)
VALUES 
('2024-05-25', 200, 1),
('2024-06-02', 150, 2),
('2024-07-10', 300, 3),
('2024-08-05', 250, 4),
('2024-09-15', 180, 5);

INSERT INTO Rating (star, event_id, order_id)
VALUES 
(5, 1, 1),
(4, 2, 2),
(3, 3, 3),
(4, 4, 4),
(5, 5, 5);

INSERT INTO Area (name, total_tickets, ticket_price, event_id)
VALUES 
('VIP', 100, 500000, 1),
('General', 500, 200000, 1),
('Zone A', 200, 150000, 2),
('Zone B', 300, 100000, 2),
('Premium', 150, 300000, 3);

INSERT INTO `Row` (row_name, area_id)
VALUES 
('A', 1),
('B', 1),
('C', 2),
('D', 3),
('E', 3);

INSERT INTO RefundOrder (date, user_id, order_id)
VALUES 
('2024-05-30', 1, 1),
('2024-06-10', 2, 2),
('2024-07-20', 3, 3),
('2024-08-15', 4, 4),
('2024-09-25', 5, 5);

INSERT INTO Seat (number, ticket_price, is_buy, is_checkedin, row_id)
VALUES 
('A1', 500000, 1, 1, 1),
('A2', 500000, 1, 1, 1),
('B1', 500000, 1, 1, 2),
('B2', 500000, 1, 1, 2),
('C1', 200000, 0, 0, 3),
('C2', 200000, 0, 0, 3),
('D1', 150000, 1, 0, 4),
('D2', 150000, 1, 0, 4),
('E1', 150000, 1, 0, 5),
('E2', 150000, 1, 0, 5);

INSERT INTO Ticket (qr_code, purchase_date, order_id, seat_id)
VALUES 
('QR0001', '2024-05-25', 1, 1),
('QR0002', '2024-05-25', 1, 2),
('QR0003', '2024-06-02', 2, 3),
('QR0004', '2024-06-02', 2, 4),
('QR0005', '2024-07-10', 3, 5),
('QR0006', '2024-07-10', 3, 6),
('QR0007', '2024-08-05', 4, 7),
('QR0008', '2024-08-05', 4, 8),
('QR0009', '2024-09-15', 5, 9),
('QR0010', '2024-09-15', 5, 10);

-- select * from organizer;
-- UPDATE `user` set avatar = "https://scontent.fsgn5-13.fna.fbcdn.net/v/t39.30808-1/362619067_3390825254514467_2268458930563726293_n.jpg?stp=dst-jpg_s480x480&_nc_cat=106&ccb=1-7&_nc_sid=5f2048&_nc_ohc=sQ0ep36X-6UQ7kNvgEH4JZQ&_nc_ht=scontent.fsgn5-13.fna&oh=00_AYAxo_hXsnJycOiA6UXP0IrVo9zTRO-NKMLBKrRg9_Tghg&oe=664D40B2"
-- where user_id = 1;

--@block

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
    IN p_organizer_type VARCHAR(50)
)
BEGIN
    DECLARE new_user_id INT;

    -- Insert User attributes
    INSERT INTO User (full_name, username, `password`, phone, dob, gender, avatar, `role`, email)
    VALUES (p_full_name, p_username, p_password, p_phone, p_dob, p_gender, p_avatar, p_role, p_email);

    -- Get the newly created user_id
    SET new_user_id = LAST_INSERT_ID();

    -- Insert Organizer attributes
    INSERT INTO Organizer (organizer_id, founded_date, website, is_active, organizer_addr, organizer_type)
    VALUES (new_user_id, p_founded_date, p_website, p_is_active, p_organizer_addr, p_organizer_type);
END;

--@block
CREATE VIEW OrganizerDetails AS
SELECT 
    o.*, 
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
