const canvas = document.getElementById("myCanvas");
canvas.width = window.innerWidth * 2;
canvas.height = window.innerHeight * 2;
const ctx = canvas.getContext("2d");

let selectedType = "";
let clickCount = 0;
let startX, startY;
let secondX, secondY;
let translateX = 0;
let translateY = 0;
const touchpadScalingFactor = 1.5;
const seatRadius = 10;
const seatSpacing = 10;
let currentPolygon = null;
let selectedShape = null;
let zoomedArea = null;
let offsetX, offsetY;
let canvasStates = [];
let currentStateIndex = -1;

let shapes = [];
function drawAll() {
  ctx.clearRect(-600, -600, canvas.width * 1.5, canvas.height * 1.5);
  shapes.forEach((shape) => {
    if (!shape.isHidden) {
      if (zoomedArea != null) {
        ctx.fillStyle = "lightgrey";
        ctx.fillRect(-600, -600, canvas.width * 1.5, canvas.height * 1.5);
        zoomedArea.draw(true);
      } else if (shape === selectedShape) {
        if (shape.type === "Area") {
          ctx.strokeStyle = "red";
          shape.draw();
        } else {
          ctx.strokeStyle = "red";
          shape.draw();
        }
      } else {
        ctx.strokeStyle = "black";
        shape.draw();
      }

      if (zoomedArea == null && shape.type === "Area") {
        shape.updateChildren();
      }
    }
  });
}
window.addEventListener("resize", resizeCanvas, false);

function resizeCanvas() {
  canvas.width = window.innerWidth * 2;
  canvas.height = window.innerHeight * 2;
  drawAll();
}

function saveCanvasState() {
  const state = {
    canvasImage: canvas.toDataURL(),
    shapes: shapes.map((shape) => shape.serialize()),
  };

  if (currentStateIndex < canvasStates.length - 1) {
    canvasStates.splice(currentStateIndex + 1);
  }

  canvasStates.push(state);
  currentStateIndex++;
}

function updateCurrentCanvasState() {
  if (currentStateIndex < 0) return;

  const currentState = { ...canvasStates[currentStateIndex - 1] };
  const updatedShapes = [...currentState.shapes];

  for (let i = 0; i < updatedShapes.length; i++) {
    if (updatedShapes[i].name === zoomedArea.name) {
      updatedShapes[i].shapes = zoomedArea.shapes;
      break;
    }
  }
  const updatedState = {
    ...currentState,
    shapes: updatedShapes,
  };

  canvasStates[currentStateIndex - 1] = updatedState;
}

function restoreCanvasState(index) {
  const state = canvasStates[index];

  shapes = state.shapes.map((shapeData) => {
    switch (shapeData.type) {
      case "RectangleStage":
        return RectangleStage.deserialize(shapeData);
      case "EllipseStage":
        return EllipseStage.deserialize(shapeData);
      case "Area":
        return PolygonArea.deserialize(shapeData);
      default:
        throw new Error("Unknown shape type: " + shapeData.type);
    }
  });

  const image = new Image();
  image.onload = function () {
    ctx.clearRect(-600, -600, canvas.width * 1.5, canvas.height * 1.5);
    ctx.drawImage(image, 0, 0);
    drawAll();
  };
  image.src = state.canvasImage;
}
