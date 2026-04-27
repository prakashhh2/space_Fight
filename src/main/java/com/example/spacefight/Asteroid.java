package com.example.spacefight;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

public class Asteroid {

    private double x, y;
    private final double size;       // bounding box edge
    private final double radius;     // approximate collision radius
    private final double speedY;
    private double rotation;
    private final double rotSpeed;

    private final double[] shapeXs;
    private final double[] shapeYs;

    private static final Random RAND = new Random();

    public Asteroid(double x, double y) {
        this.x        = x;
        this.y        = y;
        this.size     = 30 + RAND.nextDouble() * 35;
        this.radius   = size * 0.42;
        this.speedY   = 0.6 + RAND.nextDouble() * 0.9;
        this.rotSpeed = (RAND.nextDouble() - 0.5) * 0.05;

        // Generate irregular polygon
        int sides = 7 + RAND.nextInt(4);
        shapeXs = new double[sides];
        shapeYs = new double[sides];
        double half = size / 2;
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            double r     = half * (0.65 + RAND.nextDouble() * 0.6);
            shapeXs[i]   = half + Math.cos(angle) * r;
            shapeYs[i]   = half + Math.sin(angle) * r;
        }
    }

    public void update() {
        y        += speedY;
        rotation += rotSpeed;
    }

    public void render(GraphicsContext gc) {
        gc.save();
        gc.translate(x + size / 2, y + size / 2);
        gc.rotate(Math.toDegrees(rotation));
        gc.translate(-size / 2, -size / 2);

        gc.setFill(Color.rgb(110, 82, 52));
        gc.fillPolygon(shapeXs, shapeYs, shapeXs.length);
        gc.setStroke(Color.rgb(175, 135, 90));
        gc.setLineWidth(1.5);
        gc.strokePolygon(shapeXs, shapeYs, shapeXs.length);

        gc.restore();
    }

    // Circle–rect collision with player
    public boolean overlapsPlayer(double px, double py) {
        double cx  = x + size / 2, cy = y + size / 2;
        double nearX = Math.max(px, Math.min(cx, px + Player.W));
        double nearY = Math.max(py, Math.min(cy, py + Player.H));
        double dx = cx - nearX, dy = cy - nearY;
        return dx * dx + dy * dy < radius * radius;
    }

    // Circle–point(bullet centre) approximation
    public boolean overlapsBullet(double bx, double by) {
        double cx = x + size / 2, cy = y + size / 2;
        double dx = cx - (bx + 3), dy = cy - (by + 8);
        return dx * dx + dy * dy < (radius + 6) * (radius + 6);
    }

    public double getX()      { return x; }
    public double getY()      { return y; }
    public double getRadius() { return radius; }
    public double getSize()   { return size; }
}
