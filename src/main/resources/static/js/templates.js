class FirstTemplate {
    shapes = [];
    stage;
    area;

    constructor(x, y, width, height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.getShapes();
    }

    getStage() {
        this.stage = new RectangleStage({
            name: "Stage",
            x: this.x + this.width / 3.2,
            y: this.y,
            width: this.width / 2.5,
            height: this.height / 6,
        });
    }

    getShapes() {
        this.getStage();
        this.shapes.push(this.stage);
    }
}
