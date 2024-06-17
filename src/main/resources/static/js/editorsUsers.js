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

function mainEditor() {
  let areaListHtml = "";
  shapes
    .filter((shape) => shape.type === "Area")
    .forEach((area) => {
      let seatCount = 0;
      area.shapes
        .filter((shape) => shape.type === "Row")
        .forEach((row) => {
          seatCount += row.seats.length;
        });
      areaListHtml += `
        <div class="area-item" onclick="showAreaDetails('${area.id}')">
          <span>${area.name}</span>
          <span>${seatCount} seats</span>
        </div>
      `;
    });

  setEditorTitle("<h4>Areas</h4>");
  setEditorContent(`
    <div class="area-list">
      ${areaListHtml}
    </div>
  `);
}

function polygonAreaEditor(shape, mouseX, mouseY) {
  if (shape.isPointInside(mouseX, mouseY)) {
    selectedShape = shape;
    offsetX = mouseX - shape.x;
    offsetY = mouseY - shape.y;

    let seatCount = 0;
    let seatAvailable = 0;
    selectedShape.shapes
      .filter((shape) => shape.type === "Row")
      .forEach((row) => {
        seatCount += row.seats.length;
        seatAvailable = row.seats.filter((seat) => !seat.isBuyed).length;
      });

    setEditorTitle(`<h4>${selectedShape.name}</h4>`);
    setEditorContent(`
      <div class="area-item" onclick="showAreaDetails('${selectedShape.id}')">
        <div>Total: ${seatCount} seats</div>
        <div>Available: ${seatCount} seats</div>
      </div>
      <div class="area-item">
        Ticket Price: ${shape.ticketPrice || 0}
      </div>
    `);
  }
}

function areaEditor() {
  setEditorTitle(`<h1>Please choose the seat</h1>`);
  setEditorContent(``);
}

function seatEditor(seat, mouseX, mouseY) {
  if (seat.isPointInside(mouseX, mouseY)) {
    selectedShape = seat;
    selectedSeats.push(seat);
  }
}

function textEditor(shape, mouseX, mouseY) {
  if (shape.isPointInside(mouseX, mouseY)) {
    selectedShape = shape;
    offsetX = mouseX - shape.x;
    offsetY = mouseY - shape.y;

    setEditorTitle("<h4>Edit Text Properties</h4>");
    setEditorContent(`
    <label for="textContent">Content:</label>
    <input type="text" id="textContent" value="${shape.content || ""}">
    <br>
    <label for="fontSize">Font Size:</label>
    <input type="range" id="fontSize" min="10" max="100" step="1" value="${
      shape.fontSize || 30
    }">
    <br>
    <label for="fontColor">Color:</label>
    <input type="color" id="fontColor" value="${shape.color || "#000000"}">
    <br>
    <label for="rotation">Rotation (degrees):</label>
    <input type="range" id="rotation" min="0" max="360" step="1" value="${
      (shape.rotation > 0 ? shape.rotation : 360 + shape.rotation) || 0
    }">
    <br>
    <label for="fontFamily">Font Family:</label>
    <select id="fontFamily">
      <option value="Arial" ${
        shape.fontFamily === "Arial" ? "selected" : ""
      }>Arial</option>
      <option value="Verdana" ${
        shape.fontFamily === "Verdana" ? "selected" : ""
      }>Verdana</option>
      <option value="Times New Roman" ${
        shape.fontFamily === "Times New Roman" ? "selected" : ""
      }>Times New Roman</option>
    </select>
    <br>
  `);

    document.getElementById("textContent").addEventListener("input", (e) => {
      shape.content = e.target.value;
      saveAreaCanvasState();
      drawAll();
    });

    document.getElementById("fontSize").addEventListener("input", (e) => {
      shape.fontSize = parseInt(e.target.value, 10);
      saveAreaCanvasState();
      drawAll();
    });

    document.getElementById("fontColor").addEventListener("input", (e) => {
      shape.color = e.target.value;
      saveAreaCanvasState();
      drawAll();
    });

    document.getElementById("rotation").addEventListener("input", (e) => {
      shape.rotation = parseInt(e.target.value, 10);
      saveAreaCanvasState();
      drawAll();
    });

    document.getElementById("fontFamily").addEventListener("change", (e) => {
      shape.fontFamily = e.target.value;
      saveAreaCanvasState();
      drawAll();
    });

    canvas.addEventListener("mousemove", dragShape);
    canvas.addEventListener("mouseup", stopDragShape);
  }
}
