function setEditorTitle(title) {
  editorTitle.innerHTML = `
    ${title}
  `;
}

function setEditorContent(content) {
  editorContent.innerHTML = `
    ${content}
  `;
}

function handleWheel(event) {
  if (event.ctrlKey) {
    return;
  }

  event.preventDefault();
  const deltaX = event.deltaX * touchpadScalingFactor;
  const deltaY = event.deltaY * touchpadScalingFactor;
  translateX += deltaX;
  translateY += deltaY;
  ctx.translate(deltaX, deltaY);
  drawAll();
}

function popHistory() {
  history.back();
}
function handleBack() {
  updateCurrentCanvasState();
  canvasAreaStates = [];
  zoomedArea = null;
  shapes.forEach((s) => (s.isHidden = false));
  currentStateIndex--;
  restoreCanvasState(currentStateIndex);
  currentAreaStateIndex = 0;
  backButton.innerHTML =
    '<i class="bi bi-box-arrow-left" style="color: white;"></i>';
  backButton.removeEventListener("click", handleBack);
  backButton.addEventListener("click", popHistory);
  canvas.removeEventListener("mousedown", selectAreaShape);
  canvas.addEventListener("click", zoomInArea);
  mainEditor();
}

function zoomInArea(event) {
  const mouseX = event.clientX - translateX;
  const mouseY = event.clientY - translateY;
  for (let i = shapes.length - 1; i >= 0; i--) {
    if (
      shapes[i].type === "EllipseStage" ||
      shapes[i].type === "RectangleStage"
    ) {
      continue;
    }
    if (
      shapes[i].isPointInside(mouseX, mouseY) &&
      !shapes[i].isPointInsidePoints(mouseX, mouseY)
    ) {
      zoomedArea = shapes[i];
      zoomInOnShape(shapes[i]);
      break;
    }
  }
  if (selectedShape === null) {
    mainEditor();
  }
  drawAll();
}
function zoomInOnShape(polygon) {
  backButton.removeEventListener("click", popHistory);
  saveCanvasState();
  backButton.innerHTML =
    '<i class="bi bi-chevron-left" style="color: white;"></i>';
  backButton.addEventListener("click", handleBack);

  shapes.forEach((s) => (s.isHidden = s !== polygon));

  canvas.removeEventListener("click", zoomInArea);

  canvas.addEventListener("mousedown", selectAreaShape);

  let zoomedRatio;

  const furthestX = polygon.furthestX;
  const furthestY = polygon.furthestY;

  if (furthestY > furthestX) {
    zoomedRatio = window.innerHeight / (polygon.furthestY * 2);
  } else {
    zoomedRatio = window.innerWidth / (polygon.furthestX * 2);
  }

  polygon.x = window.innerWidth / 3 - translateX;
  polygon.y = window.innerHeight / 2 - translateY;
  polygon.zoomShape(zoomedRatio * 0.7);
  polygon.calculateFurthestCoordinates();
  polygon.color = "#fff";

  drawAll();
  saveAreaCanvasState();
}
function selectAreaShape(event) {
  const mouseX = event.clientX - translateX;
  const mouseY = event.clientY - translateY;

  selectedShape = null;
  for (let i = zoomedArea.shapes.length - 1; i >= 0; i--) {
    if (zoomedArea.shapes[i] instanceof Row) {
      selectSeat(zoomedArea.shapes[i], mouseX, mouseY);
      if (selectedShape == null) {
        continue;
      } else {
        break;
      }
    }
  }
  drawAll();
}

function selectSeat(row, x, y) {
  if (!row.isPointInside(x, y)) return;
  selectedShape = row;
  const selectedRow = row;
  for (let i = selectedRow.seats.length - 1; i >= 0; i--) {
    if (selectedRow.seats[i].isPointInside(x, y)) {
      selectedShape = selectedRow.seats[i];
      if (isInSelectedSeats(selectedShape)) {
        selectedShape.status = "available";
        selectedSeats = selectedSeats.filter((seat) => {
          return (
            seat.number !== selectedShape.number ||
            seat.row.name !== selectedShape.row.name ||
            seat.row.area.name !== selectedShape.row.area.name
          );
        });
      } else {
        selectedShape.status = "reserved";
        selectedSeats.push(selectedRow.seats[i]);
      }
      submitEditor();
    }
    if (selectedRow != selectedShape) {
      break;
    }
  }
  drawAll();
}

function isInSelectedSeats(theSeat) {
  let theSeats = selectedSeats.filter(
    (seat) => seat.row.area.name === theSeat.row.area.name
  );
  if (theSeats.length === 0) return false;
  theSeats = theSeats.filter((seat) => seat.row.name === theSeat.row.name);
  if (theSeats.length === 0) return false;
  theSeats = theSeats.filter((seat) => seat.number === theSeat.number);
  if (theSeats.length === 0) return false;
  return true;
}

function getSelectedAreasWithSeats() {
  // Create a Map for quick lookup and grouping of selectedSeats by area name
  const selectedSeatsMap = new Map();
  for (const seat of selectedSeats) {
    const areaName = seat.row.area.name;
    if (!selectedSeatsMap.has(areaName)) {
      selectedSeatsMap.set(areaName, []);
    }
    selectedSeatsMap.get(areaName).push(seat);
  }

  // Initialize the result array
  const result = [];

  // Iterate through eventData areas and build the result structure
  for (const area of eventData.areas) {
    const areaName = area.area.name;
    if (selectedSeatsMap.has(areaName)) {
      result.push({
        areaName: areaName,
        seats: selectedSeatsMap.get(areaName).map((seat) => ({
          number: seat.number,
          rowName: seat.row.name,
        })),
      });
    }
    // Break if all areas in selectedSeats are found
    if (result.length === selectedSeatsMap.size) {
      break;
    }
  }

  return result;
}

function handleBuyTickets() {
  const seatIds = [];

  for (const selectedArea of getSelectedAreasWithSeats()) {
    const areaName = selectedArea.areaName;

    const areaData = eventData.areas.find(
      (area) => area.area.name === areaName
    );
    if (!areaData) continue;

    for (const selectedSeat of selectedArea.seats) {
      const rowName = selectedSeat.rowName;
      const seatNumber = selectedSeat.number;

      const rowData = areaData.rows.find((row) => row.row.rowName === rowName);
      if (!rowData) continue;
      const seatData = rowData.seats.find(
        (seat) => seat.number === seatNumber.toString()
      );
      if (seatData) {
        seatIds.push(seatData.seatId);
      }
    }
  }
  console.log(seatIds, eventData.event.eventId);
}
