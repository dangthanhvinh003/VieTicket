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

function checkForUnnamedRows(zoomedArea) {
  return zoomedArea.shapes
    .filter((shape) => shape.type === "Row")
    .some((row) => !row.name);
}

function checkForDuplicateRowNames(zoomedArea) {
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

function validateAreas() {
  document.getElementById("duplicateAreaAlert").style.display =
    checkForDuplicateAreaNames() ? "block" : "none";
  document.getElementById("unnamedAreaAlert").style.display =
    checkForUnnamedAreas() ? "block" : "none";
}

function validateRows() {
  document.getElementById("duplicateRowAlert").style.display =
    checkForDuplicateRowNames(zoomedArea) ? "block" : "none";
  document.getElementById("unnamedRowAlert").style.display =
    checkForUnnamedRows(zoomedArea) ? "block" : "none";
}
