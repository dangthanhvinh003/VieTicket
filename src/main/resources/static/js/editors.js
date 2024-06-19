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
    setEditorTitle(`<h4>${selectedShape.name}</h4>`);
    setEditorContent(`
      <label for="areaName">Area Name:</label>
      <input type="text" id="areaName" value="${shape.name || ""}">
      <br>
      <label for="areaColor">Area Color:</label>
      <input type="color" id="areaColor" value="${
        shape.color === "white" ? "#ffffff" : shape.color || "#ffffff"
      }">
      <br>
      <label for="ticketPrice">Ticket Price:</label>
      <input type="text" id="ticketPrice" value="${shape.ticketPrice || 0}">
      <br>
    `);

    document.getElementById("areaName").addEventListener("input", (e) => {
      shape.name = e.target.value;
      saveCanvasState();
      drawAll();
    });

    document.getElementById("areaColor").addEventListener("input", (e) => {
      shape.color = e.target.value;
      saveCanvasState();
      drawAll();
    });

    document.getElementById("ticketPrice").addEventListener("input", (e) => {
      shape.ticketPrice = e.target.value;
      saveCanvasState();
      drawAll();
    });

    if (selectedShape.isPointInsidePoints(mouseX, mouseY)) {
      canvas.addEventListener("mousedown", selectPoint);
    } else {
      canvas.addEventListener("mousemove", dragShape);
      canvas.addEventListener("mouseup", stopDragShape);
    }
  }
}

function rectangleStageEditor(shape, mouseX, mouseY) {
  if (shape.isPointInside(mouseX, mouseY)) {
    selectedShape = shape;

    offsetX = mouseX - shape.x;
    offsetY = mouseY - shape.y;
    setEditorTitle(`<h4>${selectedShape.name}</h4>`);
    setEditorContent(`
      <label for="areaName">Area Name:</label>
      <input type="text" id="areaName" value="${shape.name || ""}">
      <br>
      <label for="areaColor">Area Color:</label>
      <input type="color" id="areaColor" value="${
        shape.color == "white" ? "#ffffff" : shape.color || "#ffffff"
      }">
      <br>
      <label for="curveWidth">Width:</label>
      <input type="range" id="curveWidth" min="1" max="1000" step="1" value="${
        shape.width
      }">
      <br>
      <label for="curveHeight">Height:</label>
      <input type="range" id="curveHeight" min="1" max="1000" step="1" value="${
        shape.height
      }">
      <br>
      <label for="curveBorderRadius">Border Radius:</label>
      <input type="range" id="curveBorderRadius" min="0" max="${
        shape.height < shape.width ? shape.height / 2 : shape.width / 2
      }" step="1" value="${shape.topLeftBorderRadius}">
      <br>
      <label for="rotation">Rotation (degrees):</label>
      <input type="range" id="rotation" min="0" max="180" step="1" value="${
        shape.rotation
      }">
      <br>
      <div class="dropdown">
        <div class="dropdown-toggle" id="advancedOptionsDropdown" data-bs-toggle="dropdown" aria-expanded="false">
          Advanced
        </div>
        <div class="dropdown-menu" aria-labelledby="advancedOptionsDropdown" id="advancedOptionsMenu">
          <label for="topLeftBorderRadius" class="dropdown-item">Top Left Border Radius:</label>
          <input class="ms-3" type="range" id="topLeftBorderRadius" class="form-range" min="0" max="${
            shape.height < shape.width ? shape.height / 2 : shape.width / 2
          }" step="1" value="${shape.topLeftBorderRadius}">
          <br>
          <label for="topRightBorderRadius" class="dropdown-item">Top Right Border Radius:</label>
          <input class="ms-3" type="range" id="topRightBorderRadius" class="form-range" min="0" max="${
            shape.height < shape.width ? shape.height / 2 : shape.width / 2
          }" step="1" value="${shape.topRightBorderRadius}">
          <br>
          <label for="bottomLeftBorderRadius" class="dropdown-item">Bottom Left Border Radius:</label>
          <input class="ms-3" type="range" id="bottomLeftBorderRadius" class="form-range" min="0" max="${
            shape.height < shape.width ? shape.height / 2 : shape.width / 2
          }" step="1" value="${shape.bottomLeftBorderRadius}">
          <br>
          <label for="bottomRightBorderRadius" class="dropdown-item">Bottom Right Border Radius:</label>
          <input class="ms-3" type="range" id="bottomRightBorderRadius" class="form-range" min="0" max="${
            shape.height < shape.width ? shape.height / 2 : shape.width / 2
          }" step="1" value="${shape.bottomRightBorderRadius}">
        </div>
      </div>
      
    `);

    const dropdownToggle = document.getElementById("advancedOptionsDropdown");
    const dropdownMenu = document.getElementById("advancedOptionsMenu");

    dropdownToggle.addEventListener("click", () => {
      const expanded = dropdownToggle.getAttribute("aria-expanded") === "true";
      dropdownToggle.setAttribute("aria-expanded", !expanded);
      dropdownMenu.style.display = !expanded ? "block" : "none";
    });

    document.getElementById("areaName").addEventListener("input", (e) => {
      shape.name = e.target.value;
      saveCanvasState();
      drawAll();
    });

    document.getElementById("areaColor").addEventListener("input", (e) => {
      shape.color = e.target.value;
      saveCanvasState();
      drawAll();
    });

    document.getElementById("curveWidth").addEventListener("input", (e) => {
      shape.width = parseInt(e.target.value, 10);
      saveCanvasState();
      drawAll();
    });

    document.getElementById("curveHeight").addEventListener("input", (e) => {
      shape.height = parseInt(e.target.value, 10);
      saveCanvasState();
      drawAll();
    });

    document
      .getElementById("curveBorderRadius")
      .addEventListener("input", (e) => {
        shape.topLeftBorderRadius = parseInt(e.target.value, 10);
        shape.topRightBorderRadius = parseInt(e.target.value, 10);
        shape.bottomLeftBorderRadius = parseInt(e.target.value, 10);
        shape.bottomRightBorderRadius = parseInt(e.target.value, 10);
        saveCanvasState();
        drawAll();
      });

    document
      .getElementById("topLeftBorderRadius")
      .addEventListener("input", (e) => {
        shape.topLeftBorderRadius = parseInt(e.target.value, 10);
        saveCanvasState();
        drawAll();
      });

    document
      .getElementById("topRightBorderRadius")
      .addEventListener("input", (e) => {
        shape.topRightBorderRadius = parseInt(e.target.value, 10);
        saveCanvasState();
        drawAll();
      });

    document
      .getElementById("bottomLeftBorderRadius")
      .addEventListener("input", (e) => {
        shape.bottomLeftBorderRadius = parseInt(e.target.value, 10);
        saveCanvasState();
        drawAll();
      });

    document
      .getElementById("bottomRightBorderRadius")
      .addEventListener("input", (e) => {
        shape.bottomRightBorderRadius = parseInt(e.target.value, 10);
        saveCanvasState();
        drawAll();
      });

    document.getElementById("rotation").addEventListener("input", (e) => {
      shape.rotation = parseInt(e.target.value, 10);
      shape.updateChildren();
      saveCanvasState();
      drawAll();
    });

    canvas.addEventListener("mousemove", dragShape);
    canvas.addEventListener("mouseup", stopDragShape);
  }
}
function ellipseStageEditor(shape, mouseX, mouseY) {
  if (shape.isPointInside(mouseX, mouseY)) {
    selectedShape = shape;

    offsetX = mouseX - shape.x;
    offsetY = mouseY - shape.y;
    setEditorTitle("<h4>Select and Edit Ellipse Stage Options</h4>");
    setEditorContent(`
      <label for="shapeName">Stage Name:</label>
      <input type="text" id="shapeName" value="${shape.name || ""}">
      <br>
      <label for="stageWidth">Stage Width:</label>
      <input type="range" id="stageWidth" min="10" max="500" step="1" value="${
        shape.width || 100
      }">
      <br>
      <label for="stageHeight">Stage Height:</label>
      <input type="range" id="stageHeight" min="10" max="500" step="1" value="${
        shape.height || 100
      }">
      <br>
      <label for="rotation">Rotation (degrees):</label>
      <input type="range" id="rotation" min="0" max="360" step="1" value="${
        shape.rotation || 0
      }">
      <br>
      <label for="color">Color:</label>
      <input type="color" id="color" value="${shape.color || "#EFEFEF"}">
      <br>
    `);

    document.getElementById("shapeName").addEventListener("input", (e) => {
      shape.name = e.target.value;
      saveCanvasState();
      drawAll();
    });

    document.getElementById("stageWidth").addEventListener("input", (e) => {
      shape.width = parseInt(e.target.value, 10);
      saveCanvasState();
      drawAll();
    });

    document.getElementById("stageHeight").addEventListener("input", (e) => {
      shape.height = parseInt(e.target.value, 10);
      saveCanvasState();
      drawAll();
    });

    document.getElementById("rotation").addEventListener("input", (e) => {
      shape.rotation = parseInt(e.target.value, 10);
      saveCanvasState();
      drawAll();
    });

    document.getElementById("color").addEventListener("input", (e) => {
      shape.color = e.target.value;
      saveCanvasState();
      drawAll();
    });

    canvas.addEventListener("mousemove", dragShape);
    canvas.addEventListener("mouseup", stopDragShape);
  }
}
function areaEditor() {
  let areaListHtml = "";

  let seatCount = 0;
  let rowCount = 0;
  let errorMessages = [];
  zoomedArea.shapes
    .filter((shape) => shape.type === "Row")
    .forEach((row) => {
      rowCount++;
      seatCount += row.seats.length;
      // if (!isSeatInsideArea(row, zoomedArea)) {
      //   errorMessages.push(`Seats in Row ${row.name} is outside the area.`);
      // }
    });

  areaListHtml += `
    <div class="area-item" onclick="showAreaDetails('${zoomedArea.id}')">
      <span>${rowCount} rows, ${seatCount} seats</span>
      ${
        errorMessages.length > 0
          ? `<div class="error-messages">${errorMessages.join("<br>")}</div>`
          : ""
      }
    </div>
  `;

  setEditorTitle(`<h4>${zoomedArea.name}</h4>`);
  setEditorContent(`
    <div class="area-list">
      ${areaListHtml}
    </div>
  `);
}

function rowEditor(shape, mouseX, mouseY) {
  if (shape.isPointInside(mouseX, mouseY)) {
    selectedShape = shape;

    offsetX = mouseX - shape.startX;
    offsetY = mouseY - shape.startY;
    setEditorTitle("<h4>Select and Edit shape Options</h4>");
    setEditorContent(`
      <label for="shapeName">shape Name:</label>
      <input type="text" id="shapeName" value="${shape.name || ""}">
      <br>
      <label for="seatRadius">Seat Radius:</label>
      <input type="range" id="seatRadius" min="1" max="50" step="1" value="${
        shape.seatRadius || 10
      }">
      <br>
      <label for="seatSpacing">Seat Spacing:</label>
      <input type="range" id="seatSpacing" min="1" max="50" step="1" value="${
        shape.seatSpacing || 10
      }">
      <br>
      <label for="rotation">Rotation (degrees):</label>
      <input type="range" id="rotation" min="0" max="360" step="1" value="${
        (shape.rotation > 0 ? shape.rotation : 360 + shape.rotation) || 0
      }">
      <br>
    `);
    document.getElementById("shapeName").addEventListener("input", (e) => {
      shape.setRowName(e.target.value);
      saveAreaCanvasState();
      drawAll();
    });

    document.getElementById("seatRadius").addEventListener("input", (e) => {
      shape.setSeatRadius(parseInt(e.target.value, 10));
      saveAreaCanvasState();
      drawAll();
    });

    document.getElementById("seatSpacing").addEventListener("input", (e) => {
      shape.setSeatSpacing(parseInt(e.target.value, 10));
      saveAreaCanvasState();
      drawAll();
    });

    document.getElementById("rotation").addEventListener("input", (e) => {
      shape.rotation = parseInt(e.target.value, 10);
      saveAreaCanvasState();
      drawAll();
    });

    canvas.addEventListener("dblclick", selectSeat);

    canvas.addEventListener("mousemove", dragShape);
    canvas.addEventListener("mouseup", stopDragShape);
  }
}

function seatEditor(seat, mouseX, mouseY) {
  if (seat.isPointInside(mouseX, mouseY)) {
    selectedShape = seat;

    offsetX = mouseX - seat.x;
    offsetY = mouseY - seat.y;
    setEditorTitle("<h4>Select and Edit Seat Options</h4>");
    setEditorContent(`
      <label for="seatNumber">Seat Number:</label>
      <input type="text" id="seatNumber" value="${seat.number || ""}">
      <br>
      <label for="seatRadius">Seat Radius:</label>
      <input type="range" id="seatRadius" min="1" max="50" step="1" value="${
        seat.radius || 10
      }">
      <br>
    `);

    document.getElementById("seatNumber").addEventListener("input", (e) => {
      seat.number = e.target.value;
      saveAreaCanvasState();
      drawAll();
    });

    document.getElementById("seatRadius").addEventListener("input", (e) => {
      seat.radius = parseInt(e.target.value, 10);
      saveAreaCanvasState();
      drawAll();
    });
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
