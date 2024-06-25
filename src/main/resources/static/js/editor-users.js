function isSeatInsideArea(row, area) {
  for (let i = 0; i < row.seats; i++) {
    const seat = row.seats[i];
    const seatRadius = seat.radius || 5;
    return (
      seat.x - seatRadius >= area.x &&
      seat.x + seatRadius <= area.x + area.width &&
      seat.y - seatRadius >= area.y &&
      seat.y + seatRadius <= area.y + area.height
    );
  }
}
function pad(number) {
  if (number < 10) {
    return "0" + number;
  }
  return number;
}
function mainEditor() {
  const startDate = new Date(eventData.event.startDate);

  var hours = pad(startDate.getHours());
  var minutes = pad(startDate.getMinutes());
  var day = pad(startDate.getDate());
  var month = pad(startDate.getMonth() + 1); // getMonth() returns 0-indexed

  // Construct formatted date string
  var formattedDateStr =
    hours +
    ":" +
    minutes +
    ", " +
    day +
    "-" +
    month +
    "-" +
    startDate.getFullYear();

  setEditorTitle(`<h2>${eventData.event.name}</h2>`);
  setEditorContent(`
    <div class="d-flex gap-2">
      <div><i style="color:#FFA3FD" class="bi bi-briefcase-fill"></i></div>
      <div>${formattedDateStr}</div>
    </div>
    <div class="d-flex gap-2">
      <div><i style="color:#FFA3FD" class="bi bi-geo-alt-fill"></i></div>
      <div>${eventData.event.location}</div>
    </div>
    <div class="pt-5">
      <h4>Seat status</h4>
      <div class="pt-1 d-flex gap-3">
        <div class="d-flex gap-1 align-items-center">
          <div class="rounded-circle" style="width: 1rem; height: 1rem; background-color: white; border-radius: 50%;"></div>
          <div>Available</div>
        </div>
        <div class="d-flex gap-1 align-items-center">
          <div class="rounded-circle" style="width: 1rem; height: 1rem; background-color: green; border-radius: 50%;"></div>
          <div>Selected</div>
        </div>
        <div class="d-flex gap-1 align-items-center">
          <div class="rounded-circle" style="width: 1rem; height: 1rem; background-color: red; border-radius: 50%;"></div>
          <div>Taken</div>
        </div>
      </div>
    </div>
    <div class="pt-5">
      <h4>Ticket price</h4>
      ${eventData.areas
        .map((area) => {
          return `<div class="d-flex justify-content-between">
            <div>${area.area.name}</div>
            <div style="color:#FFA3FD">${area.area.ticketPrice}</div>
          </div>`;
        })
        .join("")}
    </div>
  `);
}

function submitEditor() {
  const seats = selectedSeats
    .map((seat) => `${seat.row.name}${seat.number}`)
    .join(", ");
  if (selectedSeats.length > 0) {
    editorSubmitButton.style =
      "background-color: #FFA3FD; color:white;width: 100%;";
  } else {
    editorSubmitButton.style = "color: #FFA3FD; width: 100%;";
  }
  editorSubmitContent.innerHTML = `${seats}`;
}
