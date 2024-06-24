select * from VieTicket1.seatmap;
select * from VieTicket1.area;
select * from VieTicket1.row;
select * from VieTicket1.seat;

select * from VieTicket1.event;
select * from VieTicket1.user;


-- Table SeatMap
INSERT INTO SeatMap (event_id, `name`, img, map_file)
VALUES 
(null, 'Template', 'http://res.cloudinary.com/djcowpoua/image/upload/v1718549618/1b1a86de-da97-480c-aebc-c82015cf59f7.png', LOAD_FILE('C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/small_stadium.json')),
(null, 'Template', 'http://res.cloudinary.com/djcowpoua/image/upload/v1719149641/b8918e4c-5a09-4c21-8699-456a7b513e81.png', LOAD_FILE('C:/ProgramData/MySQL/MySQL Server 8.0/Uploads/stadium.json'));
       SHOW VARIABLES LIKE 'secure_file_priv';

ALTER TABLE SeatMap
ADD COLUMN map_file json;