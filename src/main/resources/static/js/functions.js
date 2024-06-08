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
function startAreaDrawing(event) {
  startX = event.clientX - translateX;
  startY = event.clientY - translateY;
  isDrawing = true;
  canvas.addEventListener("mousemove", drawAreaPreview);
  canvas.addEventListener("mouseup", finishAreaDrawing);
}

function drawAreaPreview(event) {
  if (!isDrawing) return;

  const currentX = event.clientX - translateX;
  const currentY = event.clientY - translateY;
  const width = currentX - startX;
  const height = currentY - startY;

  drawAll();
  const tempRect = new RoundedBorderRectangle({
    x: startX,
    y: startY,
    width: width,
    height: height,
  });
  tempRect.draw();
}

function finishAreaDrawing(event) {
  if (!isDrawing) return;

  const endX = event.clientX - translateX;
  const endY = event.clientY - translateY;
  const width = endX - startX;
  const height = endY - startY;

  isDrawing = false;
  canvas.removeEventListener("mousemove", drawAreaPreview);
  canvas.removeEventListener("mouseup", finishAreaDrawing);

  const finalRect = new Area({
    x: startX,
    y: startY,
    width: width,
    height: height,
  });
  shapes.push(finalRect);
  saveCanvasState();
  mainEditor();
  drawAll();
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
  const tempRect = new Stage({
    x: startX,
    y: startY,
    width: width,
    height: height,
  });
  tempRect.draw();
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

  const finalRect = new Stage({
    x: startX,
    y: startY,
    width: width,
    height: height,
  });
  shapes.push(finalRect);
  saveCanvasState();
  drawAll();
}

function selectShape(event) {
  const mouseX = event.clientX - translateX;
  const mouseY = event.clientY - translateY;

  selectedShape = null;
  for (let i = shapes.length - 1; i >= 0; i--) {
    if (shapes[i] instanceof Area) {
      roundedRectangleEditor(shapes[i], mouseX, mouseY);
      if (selectedShape == null) {
        continue;
      } else {
        break;
      }
    } else if (shapes[i] instanceof Stage) {
      roundedRectangleEditor(shapes[i], mouseX, mouseY);
      if (selectedShape == null) {
        continue;
      } else {
        break;
      }
    }
  }
  if (selectedShape === null) {
    mainEditor();
  }
  drawAll();
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
    } else {
      const mouseX = event.clientX - translateX;
      const mouseY = event.clientY - translateY;
      selectedShape.x = mouseX - offsetX;
      selectedShape.y = mouseY - offsetY;
    }
    drawAll();
  }
}

function stopDragShape(event) {
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
    if (shapes[i] instanceof Stage) continue;
    if (shapes[i].isPointInside(mouseX, mouseY)) {
      zoomedArea = shapes[i];
      zoomInOnShape(shapes[i]);
      mainMenuBar.style.display = "none";
      areaMenuBar.style.display = "flex";
      break;
    }
  }
}

function zoomInOnShape(shape) {
  saveCanvasState();
  shapes.forEach((s) => (s.isHidden = s !== shape));

  let zoomedWidth, zoomedHeight;

  if (shape.height > shape.width) {
    zoomedHeight = window.innerHeight / 1.7;
    zoomedWidth = (shape.width * zoomedHeight) / shape.height;
  } else {
    zoomedWidth = window.innerWidth / 1.7;
    zoomedHeight = (shape.height * zoomedWidth) / shape.width;
  }
  const centerX = window.innerWidth / 2;
  const centerY = window.innerHeight / 2;
  const newX = centerX - zoomedWidth / 1.5;
  const newY = centerY - zoomedHeight / 2;

  shape.width = zoomedWidth;
  shape.height = zoomedHeight;
  shape.x = newX - translateX;
  shape.y = newY - translateY;

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
      startX = event.clientX;
      startY = event.clientY;
      clickCount++;
      canvas.addEventListener("mousemove", drawSeatPreview);
    } else if (clickCount === 1) {
      secondX = event.clientX;
      secondY = event.clientY;
      clickCount++;
      canvas.removeEventListener("click", startSeatDrawing);
      canvas.addEventListener("click", finishSeatDrawing);
    }
  } else if (selectedType === "row") {
    isDrawing = true;
    startX = event.clientX;
    startY = event.clientY;

    canvas.removeEventListener("click", startSeatDrawing);
    canvas.addEventListener("mousemove", drawSeatPreview);
    canvas.addEventListener("click", finishSeatDrawing);
  }
}

function drawSeatPreview(event) {
  if (!isDrawing) return;

  const currentX = event.clientX;
  const currentY = event.clientY;
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

  const endX = event.clientX;
  const endY = event.clientY;

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
  for (let i = selectedRow.seats.length - 1; i >= 0; i--) {
    seatEditor(selectedRow.seats[i], mouseX, mouseY);
    if (selectedRow != selectedShape) {
      break;
    }
  }
  drawAll();
}

function insertText(event) {
  const mouseX = event.clientX - canvas.getBoundingClientRect().left;
  const mouseY = event.clientY - canvas.getBoundingClientRect().top;

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
  // else {
  //   shapes.forEach((shape) => {
  //     if (shape.isPointInside(mouseX, mouseY)) {
  //       textEditor(shape, mouseX, mouseY);
  //     }
  //   });
  // }
}

function removeAreaShape() {
  zoomedArea.shapes = zoomedArea.shapes.filter((shape) => {
    return shape !== selectedShape;
  });
  saveAreaCanvasState();
  drawAll();
}
