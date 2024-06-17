class Shape {
  constructor() {
    this.isHidden = false;
    this.type = "Shape";
  }

  serialize() {
    return {
      type: this.type,
      isHidden: this.isHidden,
    };
  }

  static deserialize(data) {
    const shape = new Shape();
    shape.isHidden = data.isHidden;
    return shape;
  }

  draw() {
    return "shape";
  }
}

class Rectangle extends Shape {
  constructor({
    x,
    y,
    width,
    height,
    topLeftBorderRadius = 0,
    topRightBorderRadius = 0,
    bottomLeftBorderRadius = 0,
    bottomRightBorderRadius = 0,
    borderRadius = 0,
    color = "white",
    rotation = 0,
  }) {
    super();
    this.type = "Rectangle";
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.topLeftBorderRadius =
      topLeftBorderRadius == 0 ? borderRadius : topLeftBorderRadius;
    this.topRightBorderRadius =
      topRightBorderRadius == 0 ? borderRadius : topRightBorderRadius;
    this.bottomLeftBorderRadius =
      bottomLeftBorderRadius == 0 ? borderRadius : bottomLeftBorderRadius;
    this.bottomRightBorderRadius =
      bottomRightBorderRadius == 0 ? borderRadius : bottomRightBorderRadius;
    this.color = color;
    this.rotation = rotation;
  }

  serialize() {
    return {
      ...super.serialize(),
      type: this.type,
      x: this.x,
      y: this.y,
      width: this.width,
      height: this.height,
      topLeftBorderRadius: this.topLeftBorderRadius,
      topRightBorderRadius: this.topRightBorderRadius,
      bottomLeftBorderRadius: this.bottomLeftBorderRadius,
      bottomRightBorderRadius: this.bottomRightBorderRadius,
      color: this.color,
      rotation: this.rotation,
    };
  }

  static deserialize(data) {
    return new Rectangle(data);
  }

  isPointInside(x, y) {
    const translatedX = x - (this.x + this.width / 2);
    const translatedY = y - (this.y + this.height / 2);

    const rotationRadians = (this.rotation * Math.PI) / 180;

    const cosR = Math.cos(-rotationRadians);
    const sinR = Math.sin(-rotationRadians);

    const localX = translatedX * cosR - translatedY * sinR + this.width / 2;
    const localY = translatedX * sinR + translatedY * cosR + this.height / 2;

    return (
      localX >= 0 &&
      localX <= this.width &&
      localY >= 0 &&
      localY <= this.height
    );
  }

  draw() {
    ctx.save();
    ctx.translate(this.x + this.width / 2, this.y + this.height / 2);
    ctx.rotate((this.rotation * Math.PI) / 180);
    ctx.translate(-this.width / 2, -this.height / 2);
    ctx.beginPath();
    ctx.moveTo(this.topLeftBorderRadius, 0);
    ctx.lineTo(this.width - this.topRightBorderRadius, 0);
    ctx.quadraticCurveTo(this.width, 0, this.width, this.topRightBorderRadius);
    ctx.lineTo(this.width, this.height - this.bottomRightBorderRadius);
    ctx.quadraticCurveTo(
      this.width,
      this.height,
      this.width - this.bottomRightBorderRadius,
      this.height
    );
    ctx.lineTo(this.bottomLeftBorderRadius, this.height);
    ctx.quadraticCurveTo(
      0,
      this.height,
      0,
      this.height - this.bottomLeftBorderRadius
    );
    ctx.lineTo(0, this.topLeftBorderRadius);
    ctx.quadraticCurveTo(0, 0, this.topLeftBorderRadius, 0);
    ctx.closePath();
    ctx.stroke();
    ctx.fillStyle = this.color;
    ctx.fill();
    ctx.restore();
    return "rectangle";
  }
}

class Ellipse extends Shape {
  constructor({ x, y, width, height, color = "white", rotation = 0 }) {
    super();
    this.type = "Ellipse";
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.color = color;
    this.rotation = rotation;
  }

  serialize() {
    return {
      ...super.serialize(),
      type: this.type,
      x: this.x,
      y: this.y,
      width: this.width,
      height: this.height,
      color: this.color,
      rotation: this.rotation,
    };
  }

  static deserialize(data) {
    return new Ellipse(data);
  }

  isPointInside(x, y) {
    const translatedX = x - (this.x + this.width / 2);
    const translatedY = y - (this.y + this.height / 2);

    const rotationRadians = (this.rotation * Math.PI) / 180;

    const cosR = Math.cos(-rotationRadians);
    const sinR = Math.sin(-rotationRadians);

    const localX = translatedX * cosR - translatedY * sinR + this.width / 2;
    const localY = translatedX * sinR + translatedY * cosR + this.height / 2;

    const normalizedX = (localX - this.width / 2) / (this.width / 2);
    const normalizedY = (localY - this.height / 2) / (this.height / 2);

    return normalizedX * normalizedX + normalizedY * normalizedY <= 1;
  }

  draw() {
    ctx.save();
    ctx.translate(this.x + this.width / 2, this.y + this.height / 2);
    ctx.rotate((this.rotation * Math.PI) / 180);
    ctx.translate(-this.width / 2, -this.height / 2);
    ctx.beginPath();
    ctx.ellipse(
      this.width / 2,
      this.height / 2,
      this.width / 2,
      this.height / 2,
      0,
      0,
      Math.PI * 2
    );
    ctx.closePath();
    ctx.stroke();
    ctx.fillStyle = this.color;
    ctx.fill();
    ctx.restore();
    return "Ellipse";
  }
}
class Polygon extends Shape {
  constructor({
    points = [],
    color = "#EFEFEF",
    selectedPointIndex = null,
    x = 0,
    y = 0,
    furthestX = 0,
    furthestY = 0,
  }) {
    super();
    this.type = "Polygon";
    this.points = points;
    this.color = color;
    this.selectedPointIndex = selectedPointIndex;
    this.x = x;
    this.y = y;
    this.furthestX = furthestX;
    this.furthestY = furthestY;
  }

  addPoint(x, y) {
    this.points.push({ x, y });
  }

  closePolygon() {
    if (this.points.length > 2) {
      const firstPoint = this.points[0];
      const lastPoint = this.points[this.points.length - 1];
      const distance = Math.sqrt(
        (firstPoint.x - lastPoint.x) ** 2 + (firstPoint.y - lastPoint.y) ** 2
      );
      if (distance < 10) {
        this.points.pop();
        this.getCenter();
        this.calculateFurthestCoordinates();
        this.setOffsetPoints();
        return true;
      }
    }
    return false;
  }

  mirrorVertically() {
    this.points = this.points.map((point) => {
      return {
        ...point,
        y: this.y + (this.y - point.y),
      };
    });
    this.setOffsetPoints();
  }

  mirrorHorizontally() {
    this.points = this.points.map((point) => {
      return {
        ...point,
        x: this.x + (this.x - point.x),
      };
    });
    this.setOffsetPoints();
  }

  setOffsetPoints() {
    this.points = this.points.map((point) => {
      return {
        ...point,
        offsetX: point.x - this.x,
        offsetY: point.y - this.y,
      };
    });
  }

  updatePoints() {
    this.points = this.points.map((point) => {
      return {
        ...point,
        x: point.offsetX + this.x,
        y: point.offsetY + this.y,
      };
    });
  }

  zoomShape(ratio) {
    this.points = this.points.map((point) => {
      return {
        offsetX: point.offsetX * ratio,
        offsetY: point.offsetY * ratio,
        x: point.offsetX * ratio + this.x,
        y: point.offsetY * ratio + this.y,
      };
    });
  }

  calculateFurthestCoordinates() {
    if (this.points.length === 0) return { furthestX: 0, furthestY: 0 };

    let furthestX = 0;
    let furthestY = 0;

    this.points.forEach((point) => {
      const deltaX = Math.abs(point.x - this.x);
      const deltaY = Math.abs(point.y - this.y);
      if (deltaX > furthestX) furthestX = deltaX;
      if (deltaY > furthestY) furthestY = deltaY;
    });

    this.furthestX = furthestX;
    this.furthestY = furthestY;
  }

  getCenter() {
    const sum = this.points.reduce(
      (acc, point) => {
        acc.x += point.x;
        acc.y += point.y;
        return acc;
      },
      { x: 0, y: 0 }
    );
    this.x = sum.x / this.points.length;
    this.y = sum.y / this.points.length;
  }

  serialize() {
    return {
      ...super.serialize(),
      type: this.type,
      points: this.points,
      color: this.color,
      x: this.x,
      y: this.y,
      furthestX: this.furthestX,
      furthestY: this.furthestY,
    };
  }

  static deserialize(data) {
    const polygon = new Polygon(data);
    return polygon;
  }

  isPointInside(x, y) {
    for (let i = 0; i < this.points.length; i++) {
      const point = this.points[i];
      const distance = Math.sqrt((point.x - x) ** 2 + (point.y - y) ** 2);
      if (distance <= 4) {
        return true;
      }
    }

    let isInside = false;
    const points = this.points;
    for (let i = 0, j = points.length - 1; i < points.length; j = i++) {
      const xi = points[i].x,
        yi = points[i].y;
      const xj = points[j].x,
        yj = points[j].y;

      const intersect =
        yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi;
      if (intersect) isInside = !isInside;
    }
    return isInside;
  }

  isPointInsidePoints(x, y) {
    for (let i = 0; i < this.points.length; i++) {
      const point = this.points[i];
      const distance = Math.sqrt((point.x - x) ** 2 + (point.y - y) ** 2);
      if (distance <= 4) {
        return true;
      }
    }
    return false;
  }

  draw() {
    if (this.points.length < 2) return;

    ctx.save();
    ctx.beginPath();
    ctx.moveTo(this.points[0].x, this.points[0].y);
    for (let i = 1; i < this.points.length; i++) {
      ctx.lineTo(this.points[i].x, this.points[i].y);
    }
    ctx.lineTo(this.points[0].x, this.points[0].y);
    ctx.closePath();
    ctx.stroke();
    ctx.fillStyle = this.color;
    ctx.fill();
    ctx.restore();
    return "polygon";
  }

  drawPreview(currentX, currentY) {
    if (this.points.length < 1) return;

    ctx.save();
    ctx.beginPath();
    ctx.moveTo(this.points[0].x, this.points[0].y);
    for (let i = 1; i < this.points.length; i++) {
      ctx.lineTo(this.points[i].x, this.points[i].y);
    }
    ctx.lineTo(currentX, currentY);
    ctx.stroke();
    ctx.restore();
  }

  drawPoints() {
    ctx.save();
    ctx.fillStyle = "black";
    this.points.forEach((point) => {
      ctx.beginPath();
      ctx.arc(point.x, point.y, 4, 0, Math.PI * 2);
      ctx.fill();
    });
    ctx.restore();
  }
}
class RectangleStage extends Rectangle {
  constructor({
    name = "Stage",
    x,
    y,
    width,
    height,
    borderRadius = 0,
    topLeftBorderRadius = 0,
    topRightBorderRadius = 0,
    bottomLeftBorderRadius = 0,
    bottomRightBorderRadius = 0,
    rotation = 0,
    color = "#EFEFEF",
  }) {
    super({
      x,
      y,
      width,
      height,
      borderRadius: borderRadius,
      topLeftBorderRadius: topLeftBorderRadius,
      topRightBorderRadius: topRightBorderRadius,
      bottomLeftBorderRadius: bottomLeftBorderRadius,
      bottomRightBorderRadius: bottomRightBorderRadius,
      rotation: rotation,
      color: color,
    });
    this.type = "RectangleStage";
    this.name = name;
  }

  serialize() {
    return {
      ...super.serialize(),
      type: this.type,
      name: this.name,
    };
  }

  static deserialize(data) {
    return new RectangleStage(data);
  }

  draw() {
    super.draw();

    ctx.font = `${this.width / 12}px Arial`;
    ctx.textBaseline = "middle";
    ctx.textAlign = "center";
    ctx.fillStyle = "black";
    ctx.fillText(this.name, this.x + this.width / 2, this.y + this.height / 2);
    return "Stage";
  }
}
class EllipseStage extends Ellipse {
  constructor({
    name = "Stage",
    x,
    y,
    width,
    height,
    rotation = 0,
    color = "#EFEFEF",
  }) {
    super({
      x,
      y,
      width,
      height,
      rotation: rotation,
      color: color,
    });
    this.type = "EllipseStage";
    this.name = name;
  }

  serialize() {
    return {
      ...super.serialize(),
      type: this.type,
      name: this.name,
    };
  }

  static deserialize(data) {
    return new EllipseStage(data);
  }

  draw() {
    super.draw();

    ctx.font = `${this.width / 12}px Arial`;
    ctx.textBaseline = "middle";
    ctx.textAlign = "center";
    ctx.fillStyle = "black";
    ctx.fillText(this.name, this.x + this.width / 2, this.y + this.height / 2);
    return "Stage";
  }
}

class PolygonArea extends Polygon {
  constructor({
    name = "A2",
    points = [],
    shapes = null,
    color = "#EFEFEF",
    selectedPointIndex = null,
    x = 0,
    y = 0,
    furthestX = 0,
    furthestY = 0,
    ticketPrice = 0,
  }) {
    super(points, color, selectedPointIndex, x, y, furthestX, furthestY);
    this.points = points;
    this.color = color;
    this.selectedPointIndex = selectedPointIndex;
    this.x = x;
    this.y = y;
    this.furthestX = furthestX;
    this.furthestY = furthestY;
    this.type = "Area";
    this.name = name;
    this.ticketPrice = ticketPrice;
    this.shapes = [];
    shapes
      ? shapes.map((shape) => {
          switch (shape.type) {
            case "Row": {
              const row = new Row({ ...shape });
              for (
                let seatIndex = 0;
                seatIndex < shape.seats.length;
                seatIndex++
              ) {
                row.createSeat({
                  number: seatIndex + 1,
                  isBuyed: false,
                });
              }

              this.shapes.push(row);
            }
            case "Text": {
              const text = new Text({ ...shape });
              this.shapes.push(text);
            }
          }
        })
      : (this.shapes = []);
  }

  serialize() {
    return {
      ...super.serialize(),
      type: this.type,
      name: this.name,
      ticketPrice: this.ticketPrice,
      shapes: this.shapes.map((shape) => shape.serialize()),
    };
  }

  static deserialize(data) {
    const polygonArea = new PolygonArea(data);
    polygonArea.shapes = data.shapes.map((shapeData) => {
      switch (shapeData.type) {
        case "Row":
          return Row.deserialize(shapeData);
        case "Text":
          return Text.deserialize(shapeData);
        default:
          throw new Error(`Unknown shape type: ${shapeData.type}`);
      }
    });
    return polygonArea;
  }

  addShape(shape) {
    this.shapes.push(shape);
  }

  draw(isZoomed = false) {
    super.draw();
    ctx.font = `16px Arial`;
    ctx.textBaseline = "middle";
    ctx.textAlign = "center";
    ctx.fillStyle = "black";
    ctx.fillText(this.name, this.x, this.y);
    if (isZoomed) {
      this.shapes.forEach((shape) => {
        shape.draw();
      });
    }
    return "polygonArea";
  }

  mirrorVertically() {
    super.mirrorVertically();

    this.shapes.forEach((shape) => {
      if (shape instanceof Text) {
        shape.y = -shape.y;
        shape.rotation = (360 - shape.rotation) % 360;
      } else if (shape instanceof Row) {
        shape.startY = -shape.startY;
        shape.rotation = (360 - shape.rotation) % 360;
      }
    });
  }

  mirrorHorizontally() {
    super.mirrorHorizontally();

    this.shapes.forEach((shape) => {
      if (shape instanceof Text) {
        shape.x = -shape.x;
        shape.rotation = (180 - shape.rotation) % 360;
      } else if (shape instanceof Row) {
        shape.startX = -shape.startX;
        shape.rotation = (180 - shape.rotation) % 360;
      }
    });
  }

  createRow({
    name = "",
    startX,
    startY,
    seatRadius,
    rotation,
    seatSpacing = 10,
  }) {
    const row = new Row({
      name,
      startX: startX - this.x,
      startY: startY - this.y,
      seatRadius,
      seatSpacing,
      area: {
        points: this.points,
        color: this.color,
        selectedPointIndex: this.selectedPointIndex,
        x: this.x,
        y: this.y,
        furthestX: this.furthestX,
        furthestY: this.furthestY,
      },
      rotation: rotation,
    });
    return row;
  }

  createSeatsForRow({
    name,
    startX,
    startY,
    seatRadius,
    numberOfSeats,
    rotation,
    seatSpacing = 10,
  }) {
    const row = this.createRow({
      name,
      startX,
      startY,
      seatRadius,
      seatSpacing,
      rotation,
    });

    for (let seatIndex = 0; seatIndex < numberOfSeats; seatIndex++) {
      row.createSeat({
        number: seatIndex + 1,
        isBuyed: false,
      });
    }
    this.addShape(row);
  }

  updateChildren() {
    this.shapes.forEach((shape) => {
      shape.area = this;
      if (shape.type === "Row") {
        shape.updateChildren();
      }
    });
  }
}

class Text {
  constructor({
    content = "New Text",
    x,
    y,
    fontSize = 16,
    fontFamily = "Arial",
    color = "#000000",
    rotation = 0,
    area,
  }) {
    this.content = content;
    this.x = x;
    this.y = y;
    this.fontSize = fontSize;
    this.fontFamily = fontFamily;
    this.color = color;
    this.rotation = rotation;
    this.type = "Text";
    this.area = area;
  }
  serialize() {
    return {
      type: this.type,
      content: this.content,
      x: this.x,
      y: this.y,
      fontSize: this.fontSize,
      fontFamily: this.fontFamily,
      color: this.color,
      rotation: this.rotation,
      area: this.area,
    };
  }
  static deserialize(data) {
    return new Text(data);
  }

  draw() {
    ctx.save();
    ctx.translate(this.x + this.area.x, this.y + this.area.y);
    ctx.rotate((this.rotation * Math.PI) / 180);
    ctx.translate(-(this.x + this.area.x), -(this.y + this.area.y));

    ctx.font = `${this.fontSize}px ${this.fontFamily}`;
    ctx.fillStyle = this.color;
    ctx.textBaseline = "middle";
    ctx.textAlign = "center";
    ctx.fillText(this.content, this.x + this.area.x, this.y + this.area.y);
    ctx.restore();
  }

  isPointInside(x, y) {
    ctx.save();
    ctx.translate(this.x + this.area.x, this.y + this.area.y);
    ctx.rotate((this.rotation * Math.PI) / 180);
    ctx.translate(-(this.x + this.area.x), -(this.y + this.area.y));

    ctx.font = `${this.fontSize}px ${this.fontFamily}`;
    const textWidth = ctx.measureText(this.content).width;
    const textHeight = this.fontSize;
    const localX = x - (this.x + this.area.x);
    const localY = y - (this.y + this.area.y);
    const cosR = Math.cos((-this.rotation * Math.PI) / 180);
    const sinR = Math.sin((-this.rotation * Math.PI) / 180);
    const rotatedX = localX * cosR - localY * sinR + this.x;
    const rotatedY = localX * sinR + localY * cosR + this.y;
    const isInside =
      rotatedX >= this.x - textWidth / 2 &&
      rotatedX <= this.x + textWidth / 2 &&
      rotatedY >= this.y - textHeight / 2 &&
      rotatedY <= this.y + textHeight / 2;
    ctx.restore();
    return isInside;
  }

  drawBoundingRectangle() {
    const textWidth = ctx.measureText(this.content).width;
    const textHeight = this.fontSize;
    ctx.save();
    ctx.translate(this.x + this.area.x, this.y + this.area.y);
    ctx.rotate((this.rotation * Math.PI) / 180);
    ctx.translate(-(this.x + this.area.x), -(this.y + this.area.y));

    ctx.strokeStyle = "black";
    ctx.setLineDash([5, 3]);
    ctx.strokeRect(
      this.x + this.area.x - textWidth / 2 - 2,
      this.y + this.area.y - textHeight / 2 - 4,
      textWidth + 4,
      textHeight + 4
    );

    ctx.restore();
  }
}
class Row {
  constructor({
    name = "",
    startX,
    startY,
    seatRadius = 10,
    seatSpacing = 10,
    rotation = 0,
    area,
  }) {
    this.name = name;
    this.startX = startX;
    this.startY = startY;
    this.seatRadius = seatRadius;
    this.seatSpacing = seatSpacing;
    this.seats = [];
    this.rotation = rotation;
    this.area = area;
    this.type = "Row";
    this.area = area;
  }

  serialize() {
    return {
      type: this.type,
      name: this.name,
      startX: this.startX,
      startY: this.startY,
      seatRadius: this.seatRadius,
      seatSpacing: this.seatSpacing,
      seats: this.seats.map((seat) => seat.serialize()),
      rotation: this.rotation,
      area: this.area,
    };
  }

  static deserialize(data) {
    const row = new Row({
      ...data,
    });
    row.seats = data.seats.map((seatData) => Seat.deserialize(seatData));
    return row;
  }

  isPointInside(x, y) {
    const totalWidth =
      (this.seats.length - 1) * (this.seatRadius * 2 + this.seatSpacing) +
      this.seatRadius * 2;
    const rectWidth = totalWidth;
    const rectHeight = this.seatRadius * 2;

    const translatedX = x - (this.startX + this.area.x);
    const translatedY = y - (this.startY + this.area.y);

    const rotationRadians = (this.rotation * Math.PI) / 180;

    const cosR = Math.cos(-rotationRadians);
    const sinR = Math.sin(-rotationRadians);

    const localX = translatedX * cosR - translatedY * sinR;
    const localY = translatedX * sinR + translatedY * cosR;

    return (
      localX >= -this.seatRadius &&
      localX <= rectWidth - this.seatRadius &&
      localY >= -this.seatRadius &&
      localY <= rectHeight - this.seatRadius
    );
  }

  addSeat(seat) {
    this.seats.push(seat);
  }

  createSeat({ number, isBuyed = false }) {
    const x = this.seats.length * (this.seatRadius * 2 + this.seatSpacing);
    const y = 0;
    const seat = new Seat({
      row: {
        startX: this.startX,
        startY: this.startY,
        area: this.area,
        seatRadius: this.seatRadius,
        rotation: this.rotation,
      },
      number: number,
      x: x,
      y: y,
      radius: this.seatRadius,
      isBuyed,
    });
    this.addSeat(seat);
  }

  draw() {
    ctx.save();
    ctx.translate(this.startX + this.area.x, this.startY + this.area.y);
    ctx.rotate((this.rotation * Math.PI) / 180);
    ctx.translate(-(this.startX + this.area.x), -(this.startY + this.area.y));

    ctx.save();
    ctx.translate(this.startX + this.area.x - 20, this.startY + this.area.y);
    ctx.rotate(-(this.rotation * Math.PI) / 180);
    ctx.translate(
      -(this.startX + this.area.x - 20),
      -(this.startY + this.area.y)
    );

    ctx.font = "16px Arial";
    ctx.fillStyle = "red";
    ctx.fillText(
      this.name,
      this.startX + this.area.x - 20,
      this.startY + this.area.y
    );
    ctx.restore();

    this.seats.forEach((seat) => seat.draw());

    ctx.restore();
  }

  drawBoundingRectangle() {
    ctx.save();
    ctx.translate(this.startX + this.area.x, this.startY + this.area.y);
    ctx.rotate((this.rotation * Math.PI) / 180);
    ctx.translate(-(this.startX + this.area.x), -(this.startY + this.area.y));

    const totalWidth =
      (this.seats.length - 1) * (this.seatRadius * 2 + this.seatSpacing);
    const rectWidth = totalWidth + this.seatRadius * 2;
    const rectHeight = this.seatRadius * 2;

    ctx.strokeStyle = "black";
    ctx.setLineDash([5, 3]);
    ctx.strokeRect(
      this.startX + this.area.x - this.seatRadius - 2,
      this.startY + this.area.y - this.seatRadius - 2,
      rectWidth + 4,
      rectHeight + 4
    );

    ctx.restore();
  }

  setSeatRadius(newRadius) {
    this.seatRadius = newRadius;
    this.updateChildren();
  }

  setSeatSpacing(newSpacing) {
    this.seatSpacing = newSpacing;
    this.updateChildren();
  }

  setRowName(newName) {
    this.name = newName;
    this.updateChildren();
  }

  setSeatsCoor(x, y) {
    this.startX = x;
    this.startY = y;
    this.updateChildren();
  }
  updateChildren() {
    this.seats.forEach((seat, index) => {
      seat.row = this;
      seat.radius = this.seatRadius;
      seat.x = index * (this.seatRadius * 2 + this.seatSpacing);
    });
  }
}

class Seat {
  constructor({ row, number, x, y, radius, isBuyed = false }) {
    this.row = row;
    this.number = number;
    this.x = x;
    this.y = y;
    this.radius = radius;
    this.isBuyed = isBuyed;
    this.type = "Seat";
  }

  serialize() {
    return {
      type: this.type,
      number: this.number,
      x: this.x,
      y: this.y,
      radius: this.radius,
      isBuyed: this.isBuyed,
      row: this.row,
    };
  }

  static deserialize(data) {
    return new Seat(data);
  }

  draw() {
    const centerX = this.x + this.row.startX + this.row.area.x;
    const centerY = this.y + this.row.startY + this.row.area.y;

    // Draw the seat circle
    ctx.beginPath();
    ctx.arc(centerX, centerY, this.radius, 0, Math.PI * 2, false);
    ctx.strokeStyle = "black";
    ctx.stroke();

    if (this.isBuyed) {
      ctx.fillStyle = "red";
      ctx.fill();
    }

    // Draw the seat number
    ctx.save(); // Save the current state
    ctx.translate(centerX, centerY);
    ctx.rotate(-(this.row.rotation * Math.PI) / 180); // Reset the rotation for the text
    ctx.translate(-centerX, -centerY);

    ctx.fillStyle = "black";
    ctx.font = `${this.radius * 0.75}px Arial`;
    ctx.textBaseline = "middle";
    ctx.textAlign = "center";
    ctx.fillText(this.number, centerX, centerY);

    ctx.restore(); // Restore the state after drawing the text
  }

  isPointInside(x, y) {
    // Translate the point back to the origin
    const translatedX = x - (this.row.startX + this.row.area.x);
    const translatedY = y - (this.row.startY + this.row.area.y);

    // Rotate the point back by the negative of the row's rotation
    const rotationRad = -(this.row.rotation * Math.PI) / 180;
    const unrotatedX =
      translatedX * Math.cos(rotationRad) - translatedY * Math.sin(rotationRad);
    const unrotatedY =
      translatedX * Math.sin(rotationRad) + translatedY * Math.cos(rotationRad);

    // Translate the seat center to the unrotated coordinate system
    const seatCenterX = this.x;
    const seatCenterY = this.y;

    // Calculate the distance between the unrotated point and the seat's center
    const distance = Math.sqrt(
      (unrotatedX - seatCenterX) ** 2 + (unrotatedY - seatCenterY) ** 2
    );

    // Check if the distance is less than or equal to the seat's radius
    return distance <= this.radius;
  }

  drawBoundingRectangle() {
    const rectWidth = this.radius * 2 + 4;
    const rectHeight = this.radius * 2 + 4;

    ctx.save();

    ctx.translate(
      this.row.startX + this.row.area.x,
      this.row.startY + this.row.area.y
    );

    const rotationRad = (this.row.rotation * Math.PI) / 180;
    ctx.rotate(rotationRad);

    ctx.translate(this.x, this.y);
    ctx.strokeStyle = "black";
    ctx.setLineDash([5, 3]);
    ctx.strokeRect(-this.radius - 2, -this.radius - 2, rectWidth, rectHeight); // Draw the rectangle centered at the seat

    ctx.restore();
  }
}
