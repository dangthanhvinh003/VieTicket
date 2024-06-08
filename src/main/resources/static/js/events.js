const insertArea = document.getElementById("insertArea");
const insertStage = document.getElementById("insertStage");
const insertAreaDropDown = document.getElementById("insertAreaDropDown");
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

const editorTitle = document.getElementById("editorTitle");
const editorContent = document.getElementById("editorContent");

function removeMainMapEventListeners() {
  canvas.removeEventListener("mousedown", startStageDrawing);
  canvas.removeEventListener("mousedown", startAreaDrawing);
  canvas.removeEventListener("mousemove", drawAreaPreview);
  canvas.removeEventListener("mouseup", finishAreaDrawing);
  canvas.removeEventListener("mousedown", selectShape);
  canvas.removeEventListener("dblclick", zoomInArea);
  canvas.removeEventListener("mousemove", dragShape);
  canvas.removeEventListener("mouseup", stopDragShape);
  canvas.removeEventListener("mousedown", startPanning);
  canvas.removeEventListener("mousemove", panCanvas);
  canvas.removeEventListener("mouseup", stopPanning);
}
canvas.addEventListener("wheel", (event) => {
  handleWheel(event);
});
function mainMapReset() {
  selectedShape = null;
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
    !event.target.matches("#insertArea") &&
    !event.target.matches("#insertArea *")
  ) {
    if (insertAreaDropDown.classList.contains("show")) {
      insertAreaDropDown.classList.remove("show");
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

insertStage.addEventListener("click", () => {
  mainMapReset();
  canvas.addEventListener("mousedown", startStageDrawing);
});
insertArea.addEventListener("click", () => {
  mainMapReset();
  insertAreaDropDown.classList.toggle("show");
  canvas.addEventListener("mousedown", startAreaDrawing);
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

saveButton.addEventListener("click", () => {
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
      if (response.ok) {
        return response.json();
      } else {
        throw new Error("Network response was not ok.");
      }
    })
    .then((data) => {
      console.log("Success:", data);
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
    console.log(canvasData.shapes);

    shapes = [];

    shapes = canvasData.shapes.map((shapeData) => {
      switch (shapeData.data.type) {
        case "RoundedBorderRectangle":
          return RoundedBorderRectangle.deserialize(shapeData);
        case "Stage":
          return Stage.deserialize(shapeData.data);
        case "Area":
          return Area.deserialize(shapeData.data);
        default:
          console.log(shapeData);
      }
    });
    console.log(shapes);

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

// insertCircleTable.addEventListener("click", () => {
// insertTableSeatDropDown.classList.toggle("show");
// canvas.addEventListener("mousedown", startAreaDrawing);
// });

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
