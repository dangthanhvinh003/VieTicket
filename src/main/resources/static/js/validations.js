function checkForUnnamedAreas() {
    return shapes
        .filter((shape) => shape.type === "Area")
        .some((area) => !area.name);
}

function checkForDuplicateAreaNames() {
    const areaNames = shapes
        .filter((shape) => shape.type === "Area")
        .map((area) => area.name);
    const nameSet = new Set();
    for (const name of areaNames) {
        if (nameSet.has(name)) {
            return true;
        }
        nameSet.add(name);
    }
    return false;
}

function checkForUnnamedRows() {
    return zoomedArea.shapes
        .filter((shape) => shape.type === "Row")
        .some((row) => {
            return !row.name;
        });
}

function checkForDuplicateRowNames() {
    const rowNames = zoomedArea.shapes
        .filter((shape) => shape.type === "Row")
        .map((row) => row.name);
    const nameSet = new Set();
    for (const name of rowNames) {
        if (nameSet.has(name)) {
            return true;
        }
        nameSet.add(name);
    }
    return false;
}

function checkSeatsOutsidePolygon() {
    for (const shape of zoomedArea.shapes) {
        if (shape.type !== "Row") continue;
        for (const seat of shape.seats) {
            const seatCenterX = shape.startX + seat.x + shape.area.x;
            const seatCenterY = shape.startY + seat.y + shape.area.y;

            const rotationRadians = (shape.rotation * Math.PI) / 180;
            const translatedX = seatCenterX - (shape.startX + shape.area.x);
            const translatedY = seatCenterY - (shape.startY + shape.area.y);

            const cosR = Math.cos(rotationRadians);
            const sinR = Math.sin(rotationRadians);

            const rotatedX =
                translatedX * cosR - translatedY * sinR + shape.startX + shape.area.x;
            const rotatedY =
                translatedX * sinR + translatedY * cosR + shape.startY + shape.area.y;
            if (!zoomedArea.isPointInside(rotatedX, rotatedY)) {
                return true;
            }
        }
    }
    return false;
}

function validateAreas() {
    document.getElementById("duplicateAreaAlert").style.display =
        checkForDuplicateAreaNames() ? "block" : "none";
    document.getElementById("unnamedAreaAlert").style.display =
        checkForUnnamedAreas() ? "block" : "none";
    return checkForDuplicateAreaNames() || checkForUnnamedAreas();
}

function validateRows() {
    document.getElementById("duplicateRowAlert").style.display =
        checkForDuplicateRowNames() ? "block" : "none";
    document.getElementById("unnamedRowAlert").style.display =
        checkForUnnamedRows() ? "block" : "none";
    document.getElementById("outsideAreaAlert").style.display =
        checkSeatsOutsidePolygon() ? "block" : "none";
    return (
        checkForDuplicateRowNames() ||
        checkForUnnamedRows() ||
        checkSeatsOutsidePolygon()
    );
}
