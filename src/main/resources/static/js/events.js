const insertArea = document.getElementById("insertArea");
const insertRectangleStage = document.getElementById("insertRectangleStage");
const insertEllipseStage = document.getElementById("insertEllipseStage");
const insertStageDropDown = document.getElementById("insertStageDropDown");
const insertSeats = document.getElementById("insertSeats");
const insertGridSeats = document.getElementById("insertGridSeats");
const insertSeatDropDown = document.getElementById("insertSeatDropDown");
// const insertCircleTable = document.getElementById("insertCircleTable");
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
const blankButton = document.getElementById("blankButton");
const loadButton = document.getElementById("loadButton");
const panningButton = document.getElementById("panningButton");
const removeButton = document.getElementById("removeButton");
const mainMenuBar = document.getElementById("mainMenuBar");
const areaMenuBar = document.getElementById("areaMenuBar");
const backButton = document.getElementById("backButton");
const dropdownMenuButton = document.getElementById("dropdownMenuButton");
const templatesPanel = document.getElementById("templatesPanel");
const mapDropDown = document.getElementById("mapDropDown");
const selectSeatsMode = document.getElementById("selectSeatsMode");
const seatUndoButton = document.getElementById("seatUndoButton");
const seatRedoButton = document.getElementById("seatRedoButton");
const seatRemoveButton = document.getElementById("seatRemoveButton");
const insertTextButton = document.getElementById("insertText");
const duplicateShapeInArea = document.getElementById("duplicateShapeInArea");

const loadingOverlay = document.getElementById("loadingOverlay");

const searchInInternet = document.getElementById("searchInInternet");
const modal = document.getElementById("modal");
const closeModalBtn = document.getElementById("closeModal");
const searchSeatMaps = document.getElementById("searchSeatMaps");

const dashboard = document.getElementById("dashboard");
const seatMapsMine = document.getElementById("seatMapsMine");

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
  // if (
  //   !event.target.matches("#insertCircleTable") &&
  //   !event.target.matches("#insertCircleTable *")
  // ) {
  //   if (insertTableSeatDropDown.classList.contains("show")) {
  //     insertTableSeatDropDown.classList.remove("show");
  //   }
  // }
  if (
    !event.target.matches("#loadButton") &&
    !event.target.matches("#loadButton *") &&
    !event.target.matches("#templatesPanel") &&
    !event.target.matches("#templatesPanel *")
  ) {
    const templatesPanel = document.getElementById("templatesPanel");
    if (templatesPanel.style.display === "flex") {
      templatesPanel.style.display = "none";
    }
  }
});

document.addEventListener("keydown", (event) => {
  if (event.ctrlKey && event.key === "z") {
    event.preventDefault(); // Prevent the default undo action
    if (zoomedArea) {
      if (currentAreaStateIndex > 0) {
        currentAreaStateIndex--;
        restoreAreaCanvasState(currentAreaStateIndex);
        validateRows();
        areaEditor();
      }
    } else {
      if (currentStateIndex > 0) {
        currentStateIndex--;
        restoreCanvasState(currentStateIndex);
        mainEditor();
      }
    }
  } else if (event.ctrlKey && event.key === "y") {
    event.preventDefault(); // Prevent the default redo action
    if (zoomedArea) {
      if (currentAreaStateIndex < canvasAreaStates.length - 1) {
        currentAreaStateIndex++;
        restoreAreaCanvasState(currentAreaStateIndex);
        areaEditor();
      }
    } else {
      if (currentStateIndex < canvasStates.length - 1) {
        currentStateIndex++;
        restoreCanvasState(currentStateIndex);
        mainEditor();
      }
    }
  } else if (event.ctrlKey && event.key === "v") {
    console.log("yes");
    event.preventDefault(); // Prevent the default paste action

    if (zoomedArea) {
      // Duplicate shape in zoomed area
      if (selectedShape == null) return;
      switch (selectedShape.type) {
        case "Row": {
          const newShape = new Row({ ...selectedShape, name: "R" });
          selectedShape.seats.map((seat) => {
            newShape.seats.push(new Seat(seat));
          });
          newShape.startX += 10;
          newShape.startY += 10;
          newShape.updateChildren();
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
      areaEditor();
    } else {
      // Duplicate shape on canvas
      if (selectedShape == null || selectedShape.type !== "Area") return;
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
  if (validateAreas()) {
    return;
  }
  sessionStorage.removeItem("lastCanvasState");
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
      window.location.href = response.url;
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
  if (
    templatesPanel.style.display === "none" ||
    templatesPanel.style.display === ""
  ) {
    templatesPanel.style.display = "flex";
  } else {
    templatesPanel.style.display = "none";
  }
});

blankButton.addEventListener("click", () => {
  shapes = [];
  mainMapReset();
  canvas.addEventListener("dblclick", zoomInArea);
  canvas.addEventListener("mousedown", selectShape);
});
panningButton.addEventListener("click", () => {
  mainMapReset();
  canvas.addEventListener("mousedown", startPanning);
});

removeButton.addEventListener("click", (event) => {
  removeShape();
  mainEditor();
});
// Open modal
searchInInternet.addEventListener("click", () => {
  searchInInternetHandler();
  modal.classList.remove("d-none");
  modal.classList.add("d-block");
});

searchSeatMaps.addEventListener("keydown", (e) => {
  if (e.key === "Enter") {
    searchInInternetHandler();
  }
});

dashboard.addEventListener("click", () => {
  searchInInternetHandler();
});

async function searchInInternetHandler() {
  const seatMapsContainer = document.getElementById("seatMapsContainer");
  seatMapsContainer.innerHTML = "";

  const response = await fetch(`/seatMaps?search=${searchSeatMaps.value}`);
  const data = await response.json();

  let html = '<div class="container"><div class="row g-3">';

  data.forEach((result) => {
    html += `
    <div class="col-6">
        <div class="card overflow-hidden" style="height: 10em;" onclick="handleCardClick(${result.eventId})">
            <div class="card-body p-0 position-relative">
                <div class="card-img-container" style="height:100%;">
                    <div class="card-img-overlay p-3" 
                        style="background-image: url('${result.img}'); background-size: cover; background-size: 200% 200%;">
                        <h5 class="card-title text-black">${result.location}</h5>
                    </div>
                </div>
            </div>
        </div>
    </div>
            `;
  });

  html += "</div></div>";
  seatMapsContainer.innerHTML = html;
}

async function handleCardClick(eventId) {
  const data = await fetch(`/seatMaps/${eventId}`);
  const canvasData = await data.json();
  shapes = [];
  shapes = canvasData.map((shapeData) => {
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
}

seatMapsMine.addEventListener("click", async () => {
  const seatMapsContainer = document.getElementById("seatMapsContainer");
  seatMapsContainer.innerHTML = "";

  const response = await fetch(`/seatMaps/mine`);
  const data = await response.json();

  let html = '<div class="container"><div class="row g-3">';

  data.forEach((result) => {
    html += `
    <div class="col-6">
        <div class="card overflow-hidden" style="height: 10em;" onclick="handleCardClick(${result.event_id})">
            <div class="card-body p-0 position-relative">
                <div class="card-img-container" style="height:100%;">
                    <div class="card-img-overlay p-3" 
                        style="background-image: url('${result.img}'); background-size: cover; background-size: 200% 200%;">
                        <h5 class="card-title text-black">${result.location}</h5>
                    </div>
                </div>
            </div>
        </div>
    </div>
            `;
  });

  html += "</div></div>";
  seatMapsContainer.innerHTML = html;
});
// Close modal
closeModalBtn.addEventListener("click", () => {
  modal.classList.remove("d-block");
  modal.classList.add("d-none");
});

let isDraggingModal = false;
let dragOffsetX, dragOffsetY;

modal.addEventListener("mousedown", (e) => {
  isDraggingModal = true;
  dragOffsetX = e.clientX - modal.getBoundingClientRect().left;
  dragOffsetY = e.clientY - modal.getBoundingClientRect().top;
  modal.addEventListener("mousemove", handleMouseMove);
  modal.addEventListener("mouseup", handleMouseUp);
});

function handleMouseUp() {
  isDraggingModal = false;
  modal.removeEventListener("mousemove", handleMouseMove);
  modal.removeEventListener("mouseup", handleMouseUp);
}

function handleMouseMove(e) {
  if (!isDraggingModal) return;
  requestAnimationFrame(() => {
    modal.style.left = e.clientX - dragOffsetX + "px";
    modal.style.top = e.clientY - dragOffsetY + "px";
  });
}
//-------Area Events---------

function removeAreaEventListeners() {
  canvas.removeEventListener("mousedown", startSeatDrawing);
  canvas.removeEventListener("mousedown", selectAreaShape);
  canvas.removeEventListener("mousemove", dragShape);
  canvas.removeEventListener("mouseup", stopDragShape);
  canvas.removeEventListener("mousedown", startPanning);
  canvas.removeEventListener("mousedown", insertText);
  canvas.removeEventListener("dblclick", selectSeat);
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
  if (validateRows()) {
    return;
  }
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
  canvas.addEventListener("dblclick", zoomInArea);
  canvas.addEventListener("mousedown", selectShape);
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
    areaEditor();
  }
});

seatRedoButton.addEventListener("click", () => {
  if (currentAreaStateIndex < canvasAreaStates.length - 1) {
    currentAreaStateIndex++;
    restoreAreaCanvasState(currentAreaStateIndex);
    areaEditor();
  }
});

seatRemoveButton.addEventListener("click", () => {
  removeAreaShape();
  areaEditor();
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
      const newShape = new Row({ ...selectedShape, name: "R" });
      selectedShape.seats.map((seat) => {
        newShape.seats.push(new Seat(seat));
      });
      newShape.startX += 10;
      newShape.startY += 10;
      newShape.updateChildren();
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
  areaEditor();
});
