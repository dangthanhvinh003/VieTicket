const insertArea = document.getElementById("insertArea");
const insertRectangleStage = document.getElementById("insertRectangleStage");
const insertEllipseStage = document.getElementById("insertEllipseStage");
const insertStageDropDown = document.getElementById("insertStageDropDown");
const insertSeats = document.getElementById("insertSeats");
const insertGridSeats = document.getElementById("insertGridSeats");
const insertSeatDropDown = document.getElementById("insertSeatDropDown");
const insertCircleTable = document.getElementById("insertCircleTable");
const insertTableSeatDropDown = document.getElementById(
  "insertTableSeatDropDown"
);
const selectButton = document.getElementById("selectArea");
const undoButton = document.getElementById("undoButton");
const redoButton = document.getElementById("redoButton");
const duplicateShape = document.getElementById("duplicateShape");
const mirrorHorizontally = document.getElementById("mirrorHorizontally");
const mirrorVertically = document.getElementById("mirrorVertically");
const saveButton = document.getElementById("saveButton");
const loadButton = document.getElementById("loadButton");
const panningButton = document.getElementById("panningButton");
const removeButton = document.getElementById("removeButton");
const mainMenuBar = document.getElementById("mainMenuBar");
const areaMenuBar = document.getElementById("areaMenuBar");
const backButton = document.getElementById("backButton");
const dropdownMenuButton = document.getElementById("dropdownMenuButton");
const mapDropDown = document.getElementById("mapDropDown");
const selectSeatsMode = document.getElementById("selectSeatsMode");
const seatUndoButton = document.getElementById("seatUndoButton");
const seatRedoButton = document.getElementById("seatRedoButton");
const seatRemoveButton = document.getElementById("seatRemoveButton");
const insertTextButton = document.getElementById("insertText");
const duplicateShapeInArea = document.getElementById("duplicateShapeInArea");

const loadingOverlay = document.getElementById("loadingOverlay");

const editorTitle = document.getElementById("editorTitle");
const editorContent = document.getElementById("editorContent");

function removeMainMapEventListeners() {
  canvas.removeEventListener("mousedown", startStageDrawing);
  canvas.removeEventListener("mousedown", selectShape);
  canvas.removeEventListener("dblclick", zoomInArea);
  canvas.removeEventListener("mousemove", dragShape);
  canvas.removeEventListener("mouseup", stopDragShape);
  canvas.removeEventListener("mousedown", startPanning);
  canvas.removeEventListener("mousemove", panCanvas);
  canvas.removeEventListener("mouseup", stopPanning);
  canvas.removeEventListener("mousedown", selectPoint);
  canvas.removeEventListener("mousemove", movePoint);
  canvas.removeEventListener("mouseup", stopEditingArea);

  canvas.removeEventListener("click", addNewArea);
}
canvas.addEventListener("wheel", (event) => {
  handleWheel(event);
});
function mainMapReset() {
  selectedShape = null;
  selectedType = "";
  removeMainMapEventListeners();
  drawAll();
}

window.addEventListener("click", (event) => {
  if (
    !event.target.matches("#dropdownMenuButton") &&
    !event.target.matches("#dropdownMenuButton *")
  ) {
    if (mapDropDown.classList.contains("show")) {
      mapDropDown.classList.remove("show");
    }
  }
  if (
    !event.target.matches("#insertRectangleStage") &&
    !event.target.matches("#insertRectangleStage *")
  ) {
    if (insertStageDropDown.classList.contains("show")) {
      insertStageDropDown.classList.remove("show");
    }
  }
  if (
    !event.target.matches("#insertSeats") &&
    !event.target.matches("#insertSeats *")
  ) {
    if (insertSeatDropDown.classList.contains("show")) {
      insertSeatDropDown.classList.remove("show");
    }
  }
  if (
    !event.target.matches("#insertCircleTable") &&
    !event.target.matches("#insertCircleTable *")
  ) {
    if (insertTableSeatDropDown.classList.contains("show")) {
      insertTableSeatDropDown.classList.remove("show");
    }
  }
});
dropdownMenuButton.addEventListener("click", (event) => {
  mapDropDown.classList.toggle("show");
});

insertRectangleStage.addEventListener("click", () => {
  mainMapReset();
  selectedType = "Rectangle";
  insertStageDropDown.classList.toggle("show");
  canvas.addEventListener("mousedown", startStageDrawing);
});
insertEllipseStage.addEventListener("click", () => {
  mainMapReset();
  selectedType = "Ellipse";
  canvas.addEventListener("mousedown", startStageDrawing);
});
insertArea.addEventListener("click", () => {
  mainMapReset();
  canvas.addEventListener("mousemove", handleCanvasDraw);
  canvas.addEventListener("click", addNewArea);
});

selectButton.addEventListener("click", () => {
  mainMapReset();
  canvas.addEventListener("dblclick", zoomInArea);
  canvas.addEventListener("mousedown", selectShape);
});

undoButton.addEventListener("click", () => {
  if (currentStateIndex > 0) {
    currentStateIndex--;
    restoreCanvasState(currentStateIndex);
    mainEditor();
  }
});

redoButton.addEventListener("click", () => {
  if (currentStateIndex < canvasStates.length - 1) {
    currentStateIndex++;
    restoreCanvasState(currentStateIndex);
    mainEditor();
  }
});

duplicateShape.addEventListener("click", () => {
  if (selectedShape == null && selectedShape.type !== "Area") return;
  const newShape = new PolygonArea({ ...selectedShape, name: "New Name" });

  // Update the new shape's coordinates
  newShape.x += 10;
  newShape.y += 10;
  // Push the new shape into the shapes array
  shapes.push(newShape);
  newShape.updatePoints();
  newShape.setOffsetPoints();
  selectedShape = newShape;
  saveCanvasState();
  drawAll();
  mainEditor();
});
mirrorHorizontally.addEventListener("click", () => {
  if (selectedShape == null) return;
  selectedShape.mirrorHorizontally();
  saveCanvasState();
  drawAll();
  mainEditor();
});
mirrorVertically.addEventListener("click", () => {
  if (selectedShape == null) return;
  selectedShape.mirrorVertically();
  saveCanvasState();
  drawAll();
  mainEditor();
});

saveButton.addEventListener("click", () => {
  loadingOverlay.style.display = "flex";
  const canvasImage = canvas.toDataURL();
  const blob = dataURLToBlob(canvasImage);
  const formData = new FormData();
  formData.append("file", blob, "canvasImage.png");

  const shapesData = shapes.map((shape) => ({
    data: shape.serialize(),
  }));
  formData.append("shapes", JSON.stringify(shapesData));
  fetch("/seatMap/SeatMapEditor", {
    method: "POST",
    body: formData,
  })
    .then((response) => {
      console.log(response);
    })
    .then(() => {
      window.location.href = "/createEventSuccess";
    })
    .catch((error) => {
      console.error("Error:", error);
    });
});

// Helper function to convert dataURL to Blob
function dataURLToBlob(dataURL) {
  const byteString = atob(dataURL.split(",")[1]);
  const mimeString = dataURL.split(",")[0].split(":")[1].split(";")[0];
  const ab = new ArrayBuffer(byteString.length);
  const ia = new Uint8Array(ab);
  for (let i = 0; i < byteString.length; i++) {
    ia[i] = byteString.charCodeAt(i);
  }
  return new Blob([ab], { type: mimeString });
}

loadButton.addEventListener("click", () => {
  document.getElementById("fileInput").click();
});
document.getElementById("fileInput").addEventListener("change", (event) => {
  const fileInput = event.target;
  const file = fileInput.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = function (e) {
    const json = e.target.result;
    const canvasData = JSON.parse(json);

    shapes = [];

    shapes = canvasData.shapes.map((shapeData) => {
      switch (shapeData.data.type) {
        case "EllipseStage":
          return EllipseStage.deserialize(shapeData.data);
        case "RectangleStage":
          return RectangleStage.deserialize(shapeData.data);
        case "Area":
          return PolygonArea.deserialize(shapeData.data);
        default:
          console.log(shapeData);
      }
    });

    drawAll();
  };

  reader.readAsText(file);
});

panningButton.addEventListener("click", () => {
  mainMapReset();
  canvas.addEventListener("mousedown", startPanning);
});

removeButton.addEventListener("click", (event) => {
  removeShape();
  mainEditor();
});

//-------Area Events---------

function removeAreaEventListeners() {
  canvas.removeEventListener("mousedown", startSeatDrawing);
  canvas.removeEventListener("mousedown", selectAreaShape);
  canvas.removeEventListener("mousemove", dragShape);
  canvas.removeEventListener("mouseup", stopDragShape);
  canvas.removeEventListener("mousedown", startPanning);
  canvas.removeEventListener("mousedown", insertText);
}

function preventDefault(event) {
  event.preventDefault();
}
function preventPanning() {
  canvas.addEventListener("mousedown", preventDefault);
  canvas.addEventListener("mousemove", preventDefault);
  canvas.addEventListener("mouseup", preventDefault);
  canvas.addEventListener("wheel", preventDefault);
}

function areaReset() {
  removeAreaEventListeners();
  selectedShape = null;
  selectedType = "";
  zoomedArea.draw(ctx, true);
}

backButton.addEventListener("click", () => {
  areaReset();
  updateCurrentCanvasState();
  canvasAreaStates = [];
  zoomedArea = null;
  shapes.forEach((s) => (s.isHidden = false));
  currentStateIndex--;
  restoreCanvasState(currentStateIndex);
  currentAreaStateIndex = 0;
  mainMenuBar.style.display = "flex";
  areaMenuBar.style.display = "none";
  mainEditor();
});

insertSeats.addEventListener("click", (event) => {
  areaReset();
  insertSeatDropDown.classList.toggle("show");
  selectedType = "row";
  canvas.addEventListener("click", startSeatDrawing);
});

insertGridSeats.addEventListener("click", () => {
  areaReset();
  selectedType = "grid";
  canvas.addEventListener("click", startSeatDrawing);
});

selectSeatsMode.addEventListener("click", () => {
  areaReset();
  canvas.addEventListener("mousedown", selectAreaShape);
});

seatUndoButton.addEventListener("click", () => {
  if (currentAreaStateIndex > 0) {
    currentAreaStateIndex--;
    restoreAreaCanvasState(currentAreaStateIndex);
    mainEditor();
  }
});

seatRedoButton.addEventListener("click", () => {
  if (currentAreaStateIndex < canvasAreaStates.length - 1) {
    currentAreaStateIndex++;
    restoreAreaCanvasState(currentAreaStateIndex);
    mainEditor();
  }
});

seatRemoveButton.addEventListener("click", () => {
  removeAreaShape();
  mainEditor();
});

insertTextButton.addEventListener("click", () => {
  areaReset();
  insertTextMode = true;
  canvas.addEventListener("mousedown", insertText);
});

duplicateShapeInArea.addEventListener("click", () => {
  if (selectedShape == null) return;
  switch (selectedShape.type) {
    case "Row": {
      const newShape = new Row({ ...selectedShape, name: "New Name" });
      selectedShape.seats.map((seat) => {
        newShape.seats.push(new Seat(seat));
      });
      newShape.startX += 10;
      newShape.startY += 10;
      zoomedArea.shapes.push(newShape);
      selectedShape = newShape;
      break;
    }
    case "Text": {
      const newShape = new Text({ ...selectedShape, name: "New Name" });
      newShape.x += 10;
      newShape.y += 10;
      zoomedArea.shapes.push(newShape);
      selectedShape = newShape;
      break;
    }
  }
  saveAreaCanvasState();
  drawAll();
  mainEditor();
});
