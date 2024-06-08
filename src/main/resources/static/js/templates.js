class FirstTemplate {
  constructor(ctx, x, y, width, height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.getShapes();
  }

  shapes = [];
  stage;
  area;
  getStage() {
    this.stage = new Stage({
      name: "Stage",
      x: this.x + this.width / 3.2,
      y: this.y,
      width: this.width / 2.5,
      height: this.height / 6,
    });
  }
  getArea() {
    this.area = new Area({
      name: "A1",
      x: this.x,
      y: this.y + this.height / 4,
      width: this.width,
      height: this.height / 2,
    });
  }

  getShapes() {
    this.getStage();
    this.getArea();
    this.shapes.push(this.area, this.stage);
  }
}
