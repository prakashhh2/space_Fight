package com.example.spacefight;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bullet {

    public static final double W = 6, H = 16;

    private double x, y;
    private final double speedY;
    private final boolean fromPlayer;

    public Bullet(double x, double y, double speedY, boolean fromPlayer) {
        this.x          = x;
        this.y          = y;
        this.speedY     = speedY;
        this.fromPlayer = fromPlayer;
    }

    public void update() { y += speedY; }

    public void render(GraphicsContext gc) {
        if (fromPlayer) {
            // Cyan laser with white tip
            gc.setFill(Color.rgb(0, 240, 255, 0.95));
            gc.fillRoundRect(x, y, W, H, 3, 3);
            gc.setFill(Color.WHITE);
            gc.fillOval(x + 1, y, 4, 4);
        } else {
            // Orange plasma bolt with yellow tip
            gc.setFill(Color.rgb(255, 100, 0, 0.95));
            gc.fillRoundRect(x, y, W, H, 3, 3);
            gc.setFill(Color.YELLOW);
            gc.fillOval(x + 1, y + H - 5, 4, 4);
        }
    }

    // ── Collision helpers ─────────────────────────────────────────────────────

    public boolean overlapsRect(double ox, double oy, double ow, double oh) {
        return x < ox + ow && x + W > ox && y < oy + oh && y + H > oy;
    }

    public boolean overlapsCircle(double cx, double cy, double radius) {
        double nearX = Math.max(x, Math.min(cx, x + W));
        double nearY = Math.max(y, Math.min(cy, y + H));
        double dx = cx - nearX, dy = cy - nearY;
        return dx * dx + dy * dy < radius * radius;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public double getX() { return x; }
    public double getY() { return y; }
}
