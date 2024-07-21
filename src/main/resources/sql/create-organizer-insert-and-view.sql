SELECT *
FROM VieTicket1.seatmap;
SELECT *
FROM VieTicket1.area;
SELECT s.*
FROM VieTicket1.row r
         JOIN area a ON r.area_id = a.area_id
         JOIN seat s ON r.row_id = s.row_id
WHERE a.area_id = 530
  AND r.row_name = 'R8';
SELECT *
FROM VieTicket1.seat
WHERE is_taken = 1;

SELECT *
FROM VieTicket1.ticket;
SELECT *
FROM VieTicket1.user;
SELECT *
FROM VieTicket1.event;
SELECT *
FROM VieTicket1.event e
WHERE e.is_approve = 1;
SELECT *
FROM VieTicket1.organizer;

SELECT *
FROM rating

UPDATE VieTicket1.event
SET is_approve = 5
WHERE event_id = 1;

SELECT *
FROM googleOauth
-- Table SeatMap
INSERT INTO SeatMap (event_id, `name`, img, map_file)
VALUES (NULL, 'Template',
        'http://res.cloudinary.com/djcowpoua/image/upload/v1718549618/1b1a86de-da97-480c-aebc-c82015cf59f7.png',
        LOAD_FILE('C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/small_stadium.json')),
       (NULL, 'Template',
        'http://res.cloudinary.com/djcowpoua/image/upload/v1719149641/b8918e4c-5a09-4c21-8699-456a7b513e81.png',
        LOAD_FILE('C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/stadium.json'));
SHOW VARIABLES LIKE 'secure_file_priv';

ALTER TABLE SeatMap
    ADD COLUMN map_file JSON;

SELECT *
FROM User

INSERT INTO googleOauth
VALUES ( '640078988681-kmojc9p5oqoc79flah45qhojn1q3l827.apps.googleusercontent.com'
       , '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', 'http://localhost:8080/auth/login/oauth2/google')

DROP DATABASE vieticket1

USE vieticket1