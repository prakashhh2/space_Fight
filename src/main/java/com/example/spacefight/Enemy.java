package com.example.spacefight;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;
import java.util.Random;

/**
 * Enemy ship. Three movement patterns:
 *   0 = straight down
 *   1 = sine-wave zigzag
 *   2 = diagonal glide
 */
public class Enemy {

    private double x, y;
    public static final double W = 48, H = 48;

    private final double speedY;
    private double dx;           // horizontal drift (pattern 2)
    private double waveAngle;    // accumulator for pattern 1
    private final int pattern;
    private int health;

    private long lastShotMs = 0;
    private final long shootCooldownMs;

    private static final Random RAND = new Random();

    public Enemy(double x, double y, int pattern, int level) {
        this.x               = x;
        this.y               = y;
        this.pattern         = pattern;
        this.speedY          = 0.8 + level * 0.12;
        this.health          = (level >= 3) ? 2 : 1;
        this.shootCooldownMs = Math.max(1200L, 3000L - level * 180L);
        this.dx              = (pattern == 2) ? (RAND.nextBoolean() ? 1.8 : -1.8) : 0;
    }

    public void update(List<Bullet> enemyBullets, int screenW) {
        waveAngle += 0.04;

        switch (pattern) {
            case 1 -> { y += speedY; x += Math.sin(waveAngle * 3) * 2.2; }
            case 2 -> {
                y += speedY;
                x += dx;
                // Bounce off walls
                if (x < 0)          { x = 0;          dx = Math.abs(dx); }
                if (x > screenW - W) { x = screenW - W; dx = -Math.abs(dx); }
            }
            default -> y += speedY;
        }

        // Shoot
        long now = System.currentTimeMillis();
        if (now - lastShotMs > shootCooldownMs) {
            enemyBullets.add(new Bullet(x + W / 2 - 3, y + H, 3.5, false));
            lastShotMs = now;
        }
    }

    public void hit()         { health--; }
    public boolean isDead()   { return health <= 0; }

    public void render(GraphicsContext gc) {
        double cx = x + W / 2;

        // Thruster glow (top — enemy moves downward)
        gc.setFill(Color.rgb(255, 80, 0, 0.4));
        gc.fillOval(cx - 9, y - 12, 18, 18);

        // Hull colour by health
        gc.setFill(health > 1 ? Color.rgb(190, 40, 200) : Color.rgb(210, 40, 40));
        gc.fillPolygon(
            new double[]{ cx,       x,           x + 8,      cx,          x + W - 8,  x + W    },
            new double[]{ y + H,    y,            y + 10,    y + 5,       y + 10,     y         },
            6
        );

        // Cockpit
        gc.setFill(Color.rgb(255, 160, 60, 0.85));
        gc.fillOval(cx - 7, y + H - 26, 14, 16);

        // Wing accent lines
        gc.setStroke(Color.rgb(255, 160, 160));
        gc.setLineWidth(1);
        gc.strokeLine(cx, y + H - 4, x + 4,      y + 2);
        gc.strokeLine(cx, y + H - 4, x + W - 4,  y + 2);
    }

    public boolean overlapsPlayer(double px, double py) {
        return x < px + Player.W && x + W > px && y < py + Player.H && y + H > py;
    }

    public boolean overlapsBullet(double bx, double by) {
        return x < bx + Bullet.W && x + W > bx && y < by + Bullet.H && y + H > by;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
