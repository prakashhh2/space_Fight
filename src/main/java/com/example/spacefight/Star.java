package com.example.spacefight;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

public class Star {

    private double x, y;
    private final double speed;
    private final double size;
    private final double brightness;
    private final int screenWidth;

    public Star(double x, double y, int screenWidth, Random rand) {
        this.x = x;
        this.y = y;
        this.screenWidth = screenWidth;
        this.speed      = 0.2 + rand.nextDouble() * 1.0;
        this.size       = 0.8 + rand.nextDouble() * 2.0;
        this.brightness = 0.3 + rand.nextDouble() * 0.7;
    }

    public void update(int screenHeight) {
        y += speed;
        if (y > screenHeight) {
            y = -4;
            x = Math.random() * screenWidth;
        }
    }

    public void render(GraphicsContext gc) {
        gc.setFill(Color.color(brightness, brightness, 1.0, brightness));
        gc.fillOval(x, y, size, size);
    }
}
