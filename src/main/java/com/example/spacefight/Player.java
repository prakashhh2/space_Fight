package com.example.spacefight;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

public class Player {

    private double x, y;
    public static final double W = 40, H = 50;
    private static final double SPEED = 2.5;

    private long invincibleUntil = 0;
    private long lastShotMs = 0;
    private static final long SHOOT_COOLDOWN_MS = 220;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // ── Movement ──────────────────────────────────────────────────────────────

    public void moveLeft()               { x = Math.max(0,           x - SPEED); }
    public void moveRight(int screenW)   { x = Math.min(screenW - W, x + SPEED); }
    public void moveUp(int hudY)         { y = Math.max(hudY + 10,   y - SPEED); }
    public void moveDown(int screenH)    { y = Math.min(screenH - H, y + SPEED); }

    // ── Shooting ──────────────────────────────────────────────────────────────

    public void shoot(List<Bullet> bullets) {
        long now = System.currentTimeMillis();
        if (now - lastShotMs >= SHOOT_COOLDOWN_MS) {
            bullets.add(new Bullet(x + W / 2 - 3, y - 14, -6, true));
            lastShotMs = now;
        }
    }

    // ── Damage ────────────────────────────────────────────────────────────────

    public boolean isInvincible() {
        return System.currentTimeMillis() < invincibleUntil;
    }

    public void setInvincible(long durationMs) {
        invincibleUntil = System.currentTimeMillis() + durationMs;
    }

    // ── Collision ─────────────────────────────────────────────────────────────

    public boolean overlaps(double ox, double oy, double ow, double oh) {
        return x < ox + ow && x + W > ox && y < oy + oh && y + H > oy;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    public void render(GraphicsContext gc) {
        // Flicker while invincible
        if (isInvincible() && (System.currentTimeMillis() / 120) % 2 == 0) return;

        double cx = x + W / 2;

        // Thruster glow
        gc.setFill(Color.rgb(0, 120, 255, 0.45));
        gc.fillOval(cx - 9, y + H - 6, 18, 22);

        // Main body
        gc.setFill(Color.CYAN);
        gc.fillPolygon(
            new double[]{ cx,       x + W,      x + W - 8, cx,          x + 8,     x       },
            new double[]{ y,        y + H,      y + H - 12, y + H - 6,  y + H - 12, y + H  },
            6
        );

        // Cockpit glass
        gc.setFill(Color.rgb(0, 210, 255, 0.85));
        gc.fillOval(cx - 7, y + 10, 14, 16);

        // Wing accent lines
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeLine(cx, y + 8, x + 4,      y + H - 4);
        gc.strokeLine(cx, y + 8, x + W - 4,  y + H - 4);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public double getX() { return x; }
    public double getY() { return y; }
}
