select * from VieTicket1.seatmap;
select * from VieTicket1.area;
select * from VieTicket1.row;
select * from VieTicket1.seat;

select * from VieTicket1.event;
select * from VieTicket1.user;
ALTER TABLE SeatMap
ADD COLUMN map_file json;