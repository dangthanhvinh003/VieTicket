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

function selectShape(event) {
  const mouseX = event.clientX - translateX;
  const mouseY = event.clientY - translateY;

  selectedShape = null;
  for (let i = shapes.length - 1; i >= 0; i--) {
    if (shapes[i].type === "Area") {
      polygonAreaEditor(shapes[i], mouseX, mouseY);
      if (selectedShape == null) {
        continue;
      } else {
        break;
      }
    } else if (shapes[i] instanceof RectangleStage) {
      rectangleStageEditor(shapes[i], mouseX, mouseY);
      if (selectedShape == null) {
        continue;
      } else {
        canvas.removeEventListener("mousedown", selectPoint);
        canvas.removeEventListener("mousemove", movePoint);
        canvas.removeEventListener("mouseup", stopEditingArea);
        break;
      }
    } else if (shapes[i] instanceof EllipseStage) {
      ellipseStageEditor(shapes[i], mouseX, mouseY);
      if (selectedShape == null) {
        continue;
      } else {
        canvas.removeEventListener("mousedown", selectPoint);
        canvas.removeEventListener("mousemove", movePoint);
        canvas.removeEventListener("mouseup", stopEditingArea);
        break;
      }
    }
  }
  if (selectedShape === null) {
    mainEditor();
    canvas.removeEventListener("mousedown", selectPoint);
    canvas.removeEventListener("mousemove", movePoint);
    canvas.removeEventListener("mouseup", stopEditingArea);
  }
  drawAll();
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
  canvas.addEventListener("dblclick", zoomInArea);
  canvas.addEventListener("mousedown", selectShape);
}

function zoomInArea(event) {
  const mouseX = event.clientX - translateX;
  const mouseY = event.clientY - translateY;
  for (let i = shapes.length - 1; i >= 0; i--) {
    if (shapes[i].type === "Stage") continue;
    if (
      shapes[i].isPointInside(mouseX, mouseY) &&
      !shapes[i].isPointInsidePoints(mouseX, mouseY)
    ) {
      zoomedArea = shapes[i];
      zoomInOnShape(shapes[i]);
      break;
    }
  }
}

function zoomInOnShape(polygon) {
  backButton.removeEventListener("click", popHistory);
  saveCanvasState();
  backButton.innerHTML =
    '<i class="bi bi-chevron-left" style="color: white;"></i>';
  backButton.addEventListener("click", handleBack);

  shapes.forEach((s) => (s.isHidden = s !== polygon));

  canvas.removeEventListener("dblclick", zoomInArea);
  canvas.removeEventListener("mousedown", selectShape);

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

  // Scale the offset values

  areaEditor();
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

  if (selectedShape === null) {
    areaEditor();
  }
  drawAll();
}

function selectSeat(row, x, y) {
  if (!row.isPointInside(x, y)) return;
  selectedShape = row;
  const selectedRow = row;
  for (let i = selectedRow.seats.length - 1; i >= 0; i--) {
    seatEditor(selectedRow.seats[i], x, y);
    if (selectedRow != selectedShape) {
      break;
    }
  }
  drawAll();
}
