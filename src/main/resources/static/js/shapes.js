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

class RoundedBorderRectangle extends Shape {
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
    this.type = "RoundedBorderRectangle";
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
    return new RoundedBorderRectangle(data);
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
class Stage extends RoundedBorderRectangle {
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
    this.type = "Stage";
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
    return new Stage(data);
  }

  draw() {
    super.draw();

    ctx.font = `${this.width / 12}px Arial`;
    ctx.textBaseline = "middle";
    ctx.textAlign = "center";
    ctx.fillStyle = "black";
    ctx.fillText(this.name, this.x + this.width / 2, this.y + this.height / 2);
    return "stage";
  }
}

class Area extends RoundedBorderRectangle {
  constructor({
    name = "Name",
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
    ticketPrice = 0,
    shapes = null,
  }) {
    super({
      x,
      y,
      width,
      height,
      borderRadius,
      topLeftBorderRadius: topLeftBorderRadius,
      topRightBorderRadius: topRightBorderRadius,
      bottomLeftBorderRadius: bottomLeftBorderRadius,
      bottomRightBorderRadius: bottomRightBorderRadius,
      color,
      rotation,
    });
    this.ticketPrice = ticketPrice;
    this.type = "Area";
    this.name = name;
    this.shapes = shapes ?? [];
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
    const area = new Area(data);
    area.shapes = data.shapes.map((shapeData) => {
      switch (shapeData.type) {
        case "Row":
          return Row.deserialize(shapeData, area);
        case "Text":
          return Text.deserialize(shapeData);
        default:
          throw new Error(`Unknown shape type: ${shapeData.type}`);
      }
    });
    return area;
  }

  addShape(shape) {
    shape.area = this;
    this.shapes.push(shape);
  }

  draw(isZoomed = false) {
    super.draw();
    ctx.font = `${this.width / 12}px Arial`;
    ctx.textBaseline = "middle";
    ctx.textAlign = "center";
    ctx.fillStyle = "lightgrey";
    ctx.fillText(this.name, this.x + this.width / 2, this.y + this.height / 2);
    if (isZoomed) {
      this.shapes.forEach((shape) => {
        shape.draw();
      });
    }
    return "area";
  }

  createRow({
    name = "",
    startX,
    startY,
    seatRadius,
    seatSpacing = 10,
    rotation = 0,
  }) {
    const row = new Row({
      name,
      startX: startX - this.x,
      startY: startY - this.y,
      seatRadius,
      seatSpacing,
      rotation: rotation - this.rotation,
      area: this,
    });
    return row;
  }

  createSeatsForRow({
    name,
    startX,
    startY,
    seatRadius,
    numberOfSeats,
    seatSpacing = 10,
    rotation,
  }) {
    const row = this.createRow({
      name,
      startX,
      startY,
      seatRadius,
      seatSpacing,
      rotation,
      area: this,
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
    };
  }
  static deserialize(data) {
    return new Text(data);
  }

  draw() {
    ctx.save();
    const areaCenterX = this.area.x + this.area.width / 2;
    const areaCenterY = this.area.y + this.area.height / 2;

    ctx.translate(areaCenterX, areaCenterY);
    ctx.rotate(((this.rotation + this.area.rotation) * Math.PI) / 180);
    ctx.translate(-areaCenterX, -areaCenterY);

    ctx.font = `${this.fontSize}px ${this.fontFamily}`;
    ctx.fillStyle = this.color;
    ctx.textBaseline = "middle";
    ctx.textAlign = "center";
    ctx.fillText(this.content, this.x + this.area.x, this.y + this.area.y);
    ctx.restore();
  }

  isPointInside(x, y) {
    ctx.save();
    const areaCenterX = this.area.x + this.area.width / 2;
    const areaCenterY = this.area.y + this.area.height / 2;

    ctx.translate(areaCenterX, areaCenterY);
    ctx.rotate(((this.rotation + this.area.rotation) * Math.PI) / 180);
    ctx.translate(-areaCenterX, -areaCenterY);

    ctx.font = `${this.fontSize}px ${this.fontFamily}`;
    const textWidth = ctx.measureText(this.content).width;
    const textHeight = this.fontSize;
    const localX = x - (this.x + this.area.x);
    const localY = y - (this.y + this.area.y);
    const cosR = Math.cos(
      (-(this.rotation + this.area.rotation) * Math.PI) / 180
    );
    const sinR = Math.sin(
      (-(this.rotation + this.area.rotation) * Math.PI) / 180
    );
    const rotatedX = localX * cosR - localY * sinR + (this.x + this.area.x);
    const rotatedY = localX * sinR + localY * cosR + (this.y + this.area.y);
    const isInside =
      rotatedX >= this.x + this.area.x - textWidth / 2 &&
      rotatedX <= this.x + this.area.x + textWidth / 2 &&
      rotatedY >= this.y + this.area.y - textHeight / 2 &&
      rotatedY <= this.y + this.area.y + textHeight / 2;
    ctx.restore();
    return isInside;
  }

  drawBoundingRectangle() {
    const textWidth = ctx.measureText(this.content).width;
    const textHeight = this.fontSize;
    ctx.save();
    const areaCenterX = this.area.x + this.area.width / 2;
    const areaCenterY = this.area.y + this.area.height / 2;

    ctx.translate(areaCenterX, areaCenterY);
    ctx.rotate(((this.rotation + this.area.rotation) * Math.PI) / 180);
    ctx.translate(-areaCenterX, -areaCenterY);

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
    };
  }

  static deserialize(data, area = null) {
    const row = new Row({
      ...data,
    });
    row.seats = data.seats.map((seatData) => Seat.deserialize(seatData));
    return row;
  }

  getAbsoluteCoordinates() {
    if (!this.area) return { x: this.startX, y: this.startY };
    return {
      x: this.area.x + this.startX,
      y: this.area.y + this.startY,
    };
  }

  isPointInside(x, y) {
    const { x: absX, y: absY } = this.getAbsoluteCoordinates();
    const totalWidth =
      (this.seats.length - 1) * (this.seatRadius * 2 + this.seatSpacing) +
      this.seatRadius * 2;
    const rectWidth = totalWidth;
    const rectHeight = this.seatRadius * 2;

    const translatedX = x - (this.startX + this.area.x);
    const translatedY = y - (this.startY + this.area.y);

    const rotationRadians =
      ((this.rotation + this.area.rotation) * Math.PI) / 180;

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

  getAreaCenter() {
    return {
      x: this.area.x + this.area.width / 2,
      y: this.area.y + this.area.height / 2,
    };
  }

  createSeat({ number, isBuyed = false }) {
    const x = this.seats.length * (this.seatRadius * 2 + this.seatSpacing);
    const y = 0;
    const seat = new Seat({
      row: this,
      number: number,
      x: x,
      y: y,
      radius: this.seatRadius,
      isBuyed,
    });
    this.addSeat(seat);
  }

  draw() {
    const { x: absX, y: absY } = this.getAbsoluteCoordinates();
    ctx.save();
    ctx.translate(this.startX + this.area.x, this.startY + this.area.y);
    ctx.rotate(((this.rotation + this.area.rotation) * Math.PI) / 180);
    ctx.translate(-(this.startX + this.area.x), -(this.startY + this.area.y));

    console.log(this.seats);
    this.seats.forEach((seat) => seat.draw());

    ctx.restore();
  }

  drawBoundingRectangle() {
    const { x: absX, y: absY } = this.getAbsoluteCoordinates();
    ctx.save();
    ctx.translate(this.startX + this.area.x, this.startY + this.area.y);
    ctx.rotate(((this.rotation + this.area.rotation) * Math.PI) / 180);
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
    };
  }

  static deserialize(data) {
    return new Seat(data);
  }

  draw() {
    ctx.beginPath();
    ctx.arc(
      this.x + this.row.startX + this.row.area.x,
      this.y + this.row.startY + this.row.area.y,
      this.radius,
      0,
      Math.PI * 2,
      false
    );
    ctx.strokeStyle = "black";
    ctx.stroke();
    if (this.isBuyed) {
      ctx.fillStyle = "red";
      ctx.fill();
    }
    ctx.fillStyle = "black";
    ctx.font = `${this.radius * 0.75}px Arial`;
    ctx.textBaseline = "middle";
    ctx.textAlign = "center";
    ctx.fillText(
      this.row.name + this.number,
      this.x + this.row.startX + this.row.area.x,
      this.y + this.row.startY + this.row.area.y
    );
  }

  isPointInside(x, y) {
    const seatCenterX = this.x + this.row.startX + this.row.area.x;
    const seatCenterY = this.y + this.row.startY + this.row.area.y;

    // Calculate the distance between the point and the seat's center
    const distance = Math.sqrt((x - seatCenterX) ** 2 + (y - seatCenterY) ** 2);

    // Check if the distance is less than or equal to the seat's radius
    return distance <= this.radius;
  }

  drawBoundingRectangle() {
    const seatCenterX = this.x + this.row.startX + this.row.area.x;
    const seatCenterY = this.y + this.row.startY + this.row.area.y;
    const rectX = seatCenterX - this.radius - 2;
    const rectY = seatCenterY - this.radius - 2;
    const rectWidth = this.radius * 2 + 4;
    const rectHeight = this.radius * 2 + 4;

    ctx.save();

    ctx.strokeStyle = "black";
    ctx.setLineDash([5, 3]);

    ctx.strokeRect(rectX, rectY, rectWidth, rectHeight);

    ctx.restore();
  }
}
