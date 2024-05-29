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


DELIMITER $$

CREATE PROCEDURE UpdateOrganizer(
    IN p_organizer_id INT,
    IN p_full_name VARCHAR(64),
    IN p_username VARCHAR(64),
    IN p_password VARCHAR(128),
    IN p_phone VARCHAR(16),
    IN p_dob DATE,
    IN p_gender CHAR(1),
    IN p_avatar TEXT,
    IN p_role CHAR,
    IN p_email VARCHAR(96),
    IN p_founded_date DATE,
    IN p_website TEXT,
    IN p_is_active BOOLEAN,
    IN p_organizer_addr VARCHAR(256),
    IN p_organizer_type VARCHAR(64)
)
BEGIN
    -- Update User attributes
    UPDATE User 
    SET full_name = p_full_name, 
        username = p_username, 
        `password` = p_password, 
        phone = p_phone, 
        dob = p_dob, 
        gender = p_gender, 
        avatar = p_avatar, 
        `role` = p_role, 
        email = p_email
    WHERE user_id = p_organizer_id;

    -- Update Organizer attributes
    UPDATE Organizer 
    SET founded_date = p_founded_date, 
        website = p_website, 
        is_active = p_is_active, 
        organizer_addr = p_organizer_addr, 
        organizer_type = p_organizer_type
    WHERE organizer_id = p_organizer_id;
END $$

DELIMITER ;

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