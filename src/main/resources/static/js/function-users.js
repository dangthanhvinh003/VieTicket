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
    goBackButton.innerHTML =
        '<i class="bi bi-box-arrow-left" style="color: white;"></i>';
    goBackButton.removeEventListener("click", handleBack);
    goBackButton.addEventListener("click", popHistory);
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
    goBackButton.removeEventListener("click", popHistory);
    saveCanvasState();
    goBackButton.innerHTML =
        '<i class="bi bi-chevron-left" style="color: white;"></i>';
    goBackButton.addEventListener("click", handleBack);

    shapes.forEach((s) => (s.isHidden = s !== polygon));

    canvas.removeEventListener("click", zoomInArea);

    canvas.addEventListener("mousedown", selectAreaShape);

    const fixedZoomRatio = 2.4; // Adjust this value as needed

    polygon.x = window.innerWidth / 3 - translateX;
    polygon.y = window.innerHeight / 2 - translateY;

    updateAreaShapes(polygon);
    polygon.zoomShape(fixedZoomRatio);
    polygon.calculateFurthestCoordinates();
    polygon.color = "#fff";
    polygon.updateChildren();

    drawAll();
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
            if (selectedShape.status === "taken") return;
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

function getTakenOrReservedSeats() {
    eventData.areas.forEach((area) => {
        const areaName = area.area.name;

        area.rows.forEach((row) => {
            const rowName = row.row.rowName;

            row.seats.forEach((seat) => {
                if (
                    seat.taken &&
                    (seat.taken === "TAKEN" || seat.taken === "RESERVED")
                ) {
                    takenOrReservedSeats.push({
                        areaName: areaName,
                        rowName: rowName,
                        seatNumber: seat.number,
                    });
                }
            });
        });
    });
}

function updateAreaShapes(area) {
    if (!takenOrReservedSeats.some((seat) => seat.areaName === area.name)) {
        return;
    }
    const relaventSeats = takenOrReservedSeats.filter(
        (seat) => seat.areaName === area.name
    );
    area.shapes
        .filter((shape) => {
            if (shape.type !== "Row") return false;
            if (!relaventSeats.some((seat) => seat.rowName === shape.name))
                return false;
            return true;
        })
        .forEach((row) => {
            row.seats.forEach((seat) => {
                relaventSeats.forEach((relSeat) => {
                    if (
                        parseInt(relSeat.seatNumber) === seat.number &&
                        relSeat.rowName === seat.row.name
                    ) {
                        seat.status = "taken";
                    }
                });
            });
        });
}

function getSelectedAreasWithSeats() {
    const selectedSeatsMap = new Map();
    for (const seat of selectedSeats) {
        const areaName = seat.row.area.name;
        if (!selectedSeatsMap.has(areaName)) {
            selectedSeatsMap.set(areaName, []);
        }
        selectedSeatsMap.get(areaName).push(seat);
    }

    const result = [];

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
    let data = {
        seats: seatIds,
        eventId: eventData.event.eventId,
    };

    fetch("/purchase/select-tickets", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
    })
        .then((response) => {
            if (response.ok) {
                return response.text(); // Get the response text
            } else {
                // Throw an error with content from response
                return response.text().then(function (text) {
                    throw new Error(text);
                });
            }
        })
        .then(async (paymentUrl) => {
            // Redirect to payment URL
            window.location.href = await paymentUrl;
        })
        .catch(function (error) {
            // Print the message sent by server
            alert(error.message);
            // Reload the page to update the current number of seats left
            document.getElementById("seatSelectionForm").reset();
            location.reload();
        });
}
