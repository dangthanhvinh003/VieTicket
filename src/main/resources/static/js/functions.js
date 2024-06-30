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
//--------Main menu functions---------
//Area

function addNewArea(e) {
  startX = e.clientX - translateX;
  startY = e.clientY - translateY;
  if (!currentPolygon) {
    // Start a new polygon
    currentPolygon = new PolygonArea("New Name");
    currentPolygon.addPoint(startX, startY);
    isDrawing = true;
  } else {
    currentPolygon.addPoint(startX, startY);
    // Check if the polygon should close
    if (currentPolygon.closePolygon()) {
      startX = 0;
      startY = 0;
      secondX = 0;
      secondY = 0;
      shapes.push(currentPolygon);
      saveCanvasState();
      currentPolygon = null;
      isDrawing = false;
      canvas.removeEventListener("click", addNewArea);
      canvas.removeEventListener("mousemove", handleCanvasDraw);
    }
  }
  drawAll();
  validateAreas();
  if (isDrawing) {
    currentPolygon.drawPreview(secondX, secondY);
  }
}
function handleCanvasDraw(e) {
  if (!isDrawing || !currentPolygon || currentPolygon.points.length < 1) return;
  secondX = e.clientX - translateX;
  secondY = e.clientY - translateY;

  drawAll();
  currentPolygon.drawPreview(secondX, secondY);
}

//Stage
function startStageDrawing(event) {
  startX = event.clientX - translateX;
  startY = event.clientY - translateY;
  isDrawing = true;
  canvas.addEventListener("mousemove", drawStagePreview);
  canvas.addEventListener("mouseup", finishStageDrawing);
}

function drawStagePreview(event) {
  if (!isDrawing) return;

  const currentX = event.clientX - translateX;
  const currentY = event.clientY - translateY;
  const width = currentX - startX;
  const height = currentY - startY;

  drawAll();
  if (selectedType === "Rectangle") {
    const tempRect = new RectangleStage({
      x: startX,
      y: startY,
      width: width,
      height: height,
    });
    tempRect.draw();
  } else if (selectedType === "Ellipse") {
    const tempRect = new EllipseStage({
      x: startX,
      y: startY,
      width: width,
      height: height,
    });
    tempRect.draw();
  }
}

function finishStageDrawing(event) {
  if (!isDrawing) return;

  const endX = event.clientX - translateX;
  const endY = event.clientY - translateY;
  const width = endX - startX;
  const height = endY - startY;

  isDrawing = false;
  canvas.removeEventListener("mousemove", drawStagePreview);
  canvas.removeEventListener("mouseup", finishStageDrawing);

  if (selectedType === "Rectangle") {
    const finalRect = new RectangleStage({
      x: startX,
      y: startY,
      width: width,
      height: height,
    });
    if (finalRect.width < 10 || finalRect.height < 10) return;
    shapes.push(finalRect);
  } else if (selectedType === "Ellipse") {
    const finalRect = new EllipseStage({
      x: startX,
      y: startY,
      width: width,
      height: height,
    });
    if (finalRect.width < 10 || finalRect.height < 10) return;
    shapes.push(finalRect);
  }
  saveCanvasState();
  drawAll();
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

function selectPoint(event) {
  const x = event.clientX - translateX;
  const y = event.clientY - translateY;
  selectedShape.selectedPointIndex = selectedShape.points.findIndex((point) => {
    const distance = Math.sqrt((point.x - x) ** 2 + (point.y - y) ** 2);
    return distance < 4;
  });
  if (selectedShape.selectedPointIndex !== null) {
    canvas.addEventListener("mousemove", movePoint);
    canvas.addEventListener("mouseup", stopEditingArea);
  }
}
function movePoint(event) {
  const x = event.clientX - translateX;
  const y = event.clientY - translateY;
  selectedShape.points[selectedShape.selectedPointIndex] = { x, y };
  selectedShape.setOffsetPoints();
  selectedShape.calculateFurthestCoordinates();
  drawAll();
}

function stopEditingArea() {
  selectedShape.selectedPointIndex = null;
}

function dragShape(event) {
  if (selectedShape) {
    if (selectedShape.type === "Row") {
      const mouseX = event.clientX - translateX;
      const mouseY = event.clientY - translateY;
      selectedShape.setSeatsCoor(mouseX - offsetX, mouseY - offsetY);
    } else if (selectedShape.type === "Text") {
      const mouseX = event.clientX - translateX;
      const mouseY = event.clientY - translateY;
      selectedShape.x = mouseX - offsetX;
      selectedShape.y = mouseY - offsetY;
    } else if (selectedShape.type === "Area") {
      const mouseX = event.clientX - translateX;
      const mouseY = event.clientY - translateY;
      selectedShape.x = mouseX - offsetX;
      selectedShape.y = mouseY - offsetY;
      selectedShape.updatePoints();
      selectedShape.setOffsetPoints();
    } else {
      const mouseX = event.clientX - translateX;
      const mouseY = event.clientY - translateY;
      selectedShape.x = mouseX - offsetX;
      selectedShape.y = mouseY - offsetY;
    }
    drawAll();
  }
}

function stopDragShape() {
  if (selectedShape.type === "Row") {
    canvas.removeEventListener("mousemove", dragShape);
    canvas.removeEventListener("mouseup", stopDragShape);
    saveAreaCanvasState();
  } else if (selectedShape.type === "Text") {
    canvas.removeEventListener("mousemove", dragShape);
    canvas.removeEventListener("mouseup", stopDragShape);
    saveAreaCanvasState();
  } else {
    canvas.removeEventListener("mousemove", dragShape);
    canvas.removeEventListener("mouseup", stopDragShape);
    saveCanvasState();
  }
}

function startPanning(e) {
  isPanning = true;
  if (!isPanning) return;
  startX = e.clientX;
  startY = e.clientY;
  canvas.style.cursor = "move";
  canvas.addEventListener("mousemove", panCanvas);
  canvas.addEventListener("mouseup", stopPanning);
}

function panCanvas(e) {
  if (!isPanning) return;
  const deltaX = e.clientX - startX;
  const deltaY = e.clientY - startY;
  startX = e.clientX;
  startY = e.clientY;
  translateX += deltaX;
  translateY += deltaY;
  ctx.translate(deltaX, deltaY);
  drawAll();
}

function stopPanning() {
  if (!isPanning) return;
  canvas.style.cursor = "default";
  isPanning = false;
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

function removeShape() {
  shapes = shapes.filter((shape) => {
    return shape !== selectedShape;
  });
  saveCanvasState();
  drawAll();
}
function zoomInArea(event) {
  mainMapReset();
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
      mainMenuBar.style.display = "none";
      areaMenuBar.style.display = "flex";
      break;
    }
  }
}
function zoomInOnShape(polygon) {
  saveCanvasState();
  shapes.forEach((s) => (s.isHidden = s !== polygon));

  // Set a fixed zoom ratio
  const fixedZoomRatio = 2.4; // Adjust this value as needed

  // Center the polygon in the viewport
  polygon.x = window.innerWidth / 3 - translateX;
  polygon.y = window.innerHeight / 2 - translateY;

  // Apply the fixed zoom ratio
  polygon.zoomShape(fixedZoomRatio);
  polygon.calculateFurthestCoordinates();
  polygon.color = "#fff";
  polygon.updateChildren();

  areaEditor();
  drawAll();
  saveAreaCanvasState();
}
//---------Area functions----------

//Seats
function startSeatDrawing(event) {
  if (selectedType === "grid") {
    if (clickCount === 0) {
      isDrawing = true;
      startX = event.clientX - translateX;
      startY = event.clientY - translateY;
      clickCount++;
      canvas.addEventListener("mousemove", drawSeatPreview);
    } else if (clickCount === 1) {
      secondX = event.clientX - translateX;
      secondY = event.clientY - translateY;
      clickCount++;
      canvas.removeEventListener("click", startSeatDrawing);
      canvas.addEventListener("click", finishSeatDrawing);
    }
  } else if (selectedType === "row") {
    isDrawing = true;
    startX = event.clientX - translateX;
    startY = event.clientY - translateY;

    canvas.removeEventListener("click", startSeatDrawing);
    canvas.addEventListener("mousemove", drawSeatPreview);
    canvas.addEventListener("click", finishSeatDrawing);
  }
}

function drawSeatPreview(event) {
  if (!isDrawing) return;

  const currentX = event.clientX - translateX;
  const currentY = event.clientY - translateY;
  drawAll();

  if (selectedType === "grid") {
    if (clickCount === 1) {
      drawRowSeatPreview(currentX, currentY);
    } else if (clickCount === 2) {
      drawGridSeatPreview(currentX, currentY);
    }
  } else if (selectedType === "row") {
    drawRowSeatPreview(currentX, currentY);
  }
}

function drawGridSeatPreview(x, y) {
  const angle = Math.atan2(secondY - startY, secondX - startX);

  const dx = secondX - startX;
  const dy = secondY - startY;
  const length = Math.sqrt(dx * dx + dy * dy);
  const normalX = -dy / length;
  const normalY = dx / length;

  const distance = (x - startX) * normalX + (y - startY) * normalY;
  const totalWidth = Math.sqrt(
    Math.pow(secondX - startX, 2) + Math.pow(secondY - startY, 2)
  );
  const numberOfSeatsInRow =
    Math.floor(totalWidth / (2 * seatRadius + seatSpacing)) - 1;
  const numberOfRows = Math.floor(
    Math.abs(distance) / (2 * seatRadius + seatSpacing)
  );

  drawAll();

  for (let row = 0; row <= numberOfRows; row++) {
    for (let i = 0; i <= numberOfSeatsInRow; i++) {
      const offsetX =
        row * (2 * seatRadius + seatSpacing) * normalX * Math.sign(distance);
      const offsetY =
        row * (2 * seatRadius + seatSpacing) * normalY * Math.sign(distance);
      const seatX =
        startX + i * (2 * seatRadius + seatSpacing) * Math.cos(angle) + offsetX;
      const seatY =
        startY + i * (2 * seatRadius + seatSpacing) * Math.sin(angle) + offsetY;
      ctx.beginPath();
      ctx.arc(seatX, seatY, seatRadius, 0, 2 * Math.PI);
      ctx.stroke();
    }
  }
}

function drawRowSeatPreview(x, y) {
  const angle = Math.atan2(y - startY, x - startX);

  const totalWidth = Math.sqrt(
    Math.pow(x - startX, 2) + Math.pow(y - startY, 2)
  );
  const numberOfSeats = Math.floor(totalWidth / (2 * seatRadius + seatSpacing));

  for (let i = 0; i < numberOfSeats; i++) {
    const seatX = startX + i * (2 * seatRadius + seatSpacing) * Math.cos(angle);
    const seatY = startY + i * (2 * seatRadius + seatSpacing) * Math.sin(angle);
    ctx.beginPath();
    ctx.arc(seatX, seatY, seatRadius, 0, 2 * Math.PI);
    ctx.stroke();
  }
}

function finishSeatDrawing(event) {
  if (!isDrawing) return;
  isDrawing = false;
  clickCount = 0;

  canvas.removeEventListener("mousemove", drawSeatPreview);
  canvas.removeEventListener("click", finishSeatDrawing);
  const endX = event.clientX - translateX;
  const endY = event.clientY - translateY;
  zoomedArea;
  if (selectedType === "row") {
    const angle = Math.atan2(endY - startY, endX - startX) * (180 / Math.PI);
    const totalWidth = Math.sqrt(
      Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2)
    );
    const numberOfSeats = Math.floor(
      totalWidth / (2 * seatRadius + seatSpacing)
    );
    if (numberOfSeats == 0) return;
    zoomedArea.createSeatsForRow({
      name: "RX",
      startX,
      startY,
      seatRadius,
      numberOfSeats,
      seatSpacing,
      rotation: angle,
    });
  } else if (selectedType === "grid") {
    const dx = secondX - startX;
    const dy = secondY - startY;
    const length = Math.sqrt(dx * dx + dy * dy);
    const normalX = -dy / length;
    const normalY = dx / length;
    const angle = Math.atan2(secondY - startY, secondX - startX);

    const distance = (endX - startX) * normalX + (endY - startY) * normalY;
    const numberOfRows = Math.floor(
      Math.abs(distance) / (2 * seatRadius + seatSpacing)
    );

    const totalWidth = Math.sqrt(
      Math.pow(secondX - startX, 2) + Math.pow(secondY - startY, 2)
    );
    const numberOfSeats = Math.floor(
      totalWidth / (2 * seatRadius + seatSpacing)
    );

    if (numberOfSeats == 0) return;

    for (let row = 0; row <= numberOfRows; row++) {
      const offsetX =
        row * (2 * seatRadius + seatSpacing) * normalX * Math.sign(distance);
      const offsetY =
        row * (2 * seatRadius + seatSpacing) * normalY * Math.sign(distance);
      const rowStartX = startX + offsetX;
      const rowStartY = startY + offsetY;

      zoomedArea.createSeatsForRow({
        name: `R${row + 1}`,
        startX: rowStartX,
        startY: rowStartY,
        seatRadius,
        numberOfSeats,
        seatSpacing,
        rotation: angle * (180 / Math.PI),
      });
    }
  }
  saveAreaCanvasState();
  canvas.addEventListener("click", startSeatDrawing);
  areaEditor();
  drawAll();
}

function selectAreaShape(event) {
  const mouseX = event.clientX - translateX;
  const mouseY = event.clientY - translateY;

  selectedShape = null;
  for (let i = zoomedArea.shapes.length - 1; i >= 0; i--) {
    if (zoomedArea.shapes[i] instanceof Row) {
      rowEditor(zoomedArea.shapes[i], mouseX, mouseY);
      if (selectedShape == null) {
        continue;
      } else {
        break;
      }
    } else if (zoomedArea.shapes[i] instanceof Text) {
      textEditor(zoomedArea.shapes[i], mouseX, mouseY);
      if (selectedShape == null) {
        continue;
      } else {
        if (zoomedArea.shapes[i].content.length === 0) {
          zoomedArea.shapes.splice(i, 1);
        }
        break;
      }
    }
  }

  if (selectedShape === null) {
    areaEditor();
  }
  drawAll();
}

function selectSeat(event) {
  const mouseX = event.clientX - translateX;
  const mouseY = event.clientY - translateY;
  const selectedRow = selectedShape;
  console.log(selectedRow);
  for (let i = selectedRow.seats.length - 1; i >= 0; i--) {
    seatEditor(selectedRow.seats[i], mouseX, mouseY);
    if (selectedRow != selectedShape) {
      break;
    }
  }
  drawAll();
}

function insertText(event) {
  const mouseX =
    event.clientX - canvas.getBoundingClientRect().left - translateX;
  const mouseY =
    event.clientY - canvas.getBoundingClientRect().top - translateY;

  if (insertTextMode) {
    const newText = new Text({
      content: "New Text",
      x: mouseX - zoomedArea.x,
      y: mouseY - zoomedArea.y,
      fontSize: 30,
      area: zoomedArea,
    });
    zoomedArea.addShape(newText);
    textEditor(newText, mouseX, mouseY);
    insertTextMode = false;
    canvas.style.cursor = "default";
  }
  drawAll();
}

function removeAreaShape() {
  if (selectedShape.type === "Seat") {
    let decreaseNumber = 0;
    zoomedArea.shapes
      .filter(
        (shape) => shape.type === "Row" && shape.name === selectedShape.row.name
      )
      .forEach((row) => {
        row.seats = row.seats.filter((seat) => {
          if (seat === selectedShape) {
            decreaseNumber += 1;
            return false;
          }
          seat.number -= decreaseNumber;
          return true;
        });
      });
  } else {
    zoomedArea.shapes = zoomedArea.shapes.filter((shape) => {
      return shape !== selectedShape;
    });
  }
  saveAreaCanvasState();
  drawAll();
}
