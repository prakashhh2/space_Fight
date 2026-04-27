package com.example.spacefight;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.*;

public class GamePane extends Pane {

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int HUD_H = 80;   // pixel height reserved for HUD at top

    // ── Screen ────────────────────────────────────────────────────────────────
    private final int W, H;
    private final Canvas canvas;
    private final GraphicsContext gc;

    // ── Input ─────────────────────────────────────────────────────────────────
    private final Set<KeyCode> held = new HashSet<>();

    // ── Game state ────────────────────────────────────────────────────────────
    private enum State { START, PLAYING, GAME_OVER }
    private State state = State.START;

    private Player player;
    private final List<Enemy>    enemies      = new ArrayList<>();
    private final List<Bullet>   playerBullets = new ArrayList<>();
    private final List<Bullet>   enemyBullets  = new ArrayList<>();
    private final List<Asteroid> asteroids     = new ArrayList<>();
    private final List<Star>     stars         = new ArrayList<>();

    private int score, lives, level, highScore;

    // ── Spawn timers (nanoseconds) ────────────────────────────────────────────
    private long lastEnemySpawn    = 0;
    private long lastAsteroidSpawn = 0;
    private long enemyInterval     = 2_000_000_000L;
    private static final long ASTEROID_INTERVAL = 5_000_000_000L;

    private static final Random RAND = new Random();

    // ── Constructor ───────────────────────────────────────────────────────────

    public GamePane(int width, int height) {
        this.W = width;
        this.H = height;
        canvas = new Canvas(W, H);
        gc     = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        initStars();
    }

    // ── Key handling (called by HelloApplication) ─────────────────────────────

    public void handleKeyPressed(KeyCode code) {
        held.add(code);
        if (code == KeyCode.ENTER) {
            if (state == State.START || state == State.GAME_OVER) {
                startNewGame();
            }
        }
    }

    public void handleKeyReleased(KeyCode code) {
        held.remove(code);
    }

    // ── Loop entry point ──────────────────────────────────────────────────────

    public void startLoop() {
        renderStartScreen();          // show something immediately

        new AnimationTimer() {
            @Override public void handle(long now) {
                switch (state) {
                    case START     -> renderStartScreen();
                    case GAME_OVER -> renderGameOver();
                    case PLAYING   -> { update(now); render(); }
                }
            }
        }.start();
    }

    // ── Init helpers ──────────────────────────────────────────────────────────

    private void initStars() {
        for (int i = 0; i < 130; i++) {
            stars.add(new Star(RAND.nextInt(W), RAND.nextInt(H), W, RAND));
        }
    }

    private void startNewGame() {
        score = 0;
        lives = 3;
        level = 1;
        enemies.clear();
        playerBullets.clear();
        enemyBullets.clear();
        asteroids.clear();
        lastEnemySpawn    = 0;
        lastAsteroidSpawn = 0;
        enemyInterval     = 2_000_000_000L;
        player = new Player(W / 2.0 - Player.W / 2, H - 110);
        state  = State.PLAYING;
    }

    // ── Game update ───────────────────────────────────────────────────────────

    private void update(long now) {
        handleInput();
        stars.forEach(s -> s.update(H));

        spawnEntities(now);

        playerBullets.forEach(Bullet::update);
        enemyBullets.forEach(Bullet::update);
        enemies.forEach(e -> e.update(enemyBullets, W));
        asteroids.forEach(Asteroid::update);

        resolveCollisions();
        removeOffscreen();

        // Level progression
        level = score / 1000 + 1;
        enemyInterval = Math.max(500_000_000L, 2_000_000_000L - (long)(level - 1) * 200_000_000L);
    }

    private void handleInput() {
        if (held.contains(KeyCode.LEFT)  || held.contains(KeyCode.A)) player.moveLeft();
        if (held.contains(KeyCode.RIGHT) || held.contains(KeyCode.D)) player.moveRight(W);
        if (held.contains(KeyCode.UP)    || held.contains(KeyCode.W)) player.moveUp(HUD_H);
        if (held.contains(KeyCode.DOWN)  || held.contains(KeyCode.S)) player.moveDown(H);
        player.shoot(playerBullets);  // continuous auto-fire
    }

    private void spawnEntities(long now) {
        if (now - lastEnemySpawn > enemyInterval) {
            int pattern = RAND.nextInt(3);
            enemies.add(new Enemy(RAND.nextInt(W - (int) Enemy.W), -60, pattern, level));
            lastEnemySpawn = now;
        }
        if (now - lastAsteroidSpawn > ASTEROID_INTERVAL) {
            asteroids.add(new Asteroid(RAND.nextInt(W - 60), -60));
            lastAsteroidSpawn = now;
        }
    }

    private void resolveCollisions() {
        double px = player.getX(), py = player.getY();

        // Player bullets vs enemies
        List<Bullet>  deadBullets  = new ArrayList<>();
        List<Enemy>   deadEnemies  = new ArrayList<>();

        for (Bullet b : playerBullets) {
            for (Enemy e : enemies) {
                if (e.overlapsBullet(b.getX(), b.getY())) {
                    deadBullets.add(b);
                    e.hit();
                    if (e.isDead()) { deadEnemies.add(e); score += 100 * level; }
                    break;
                }
            }
        }
        playerBullets.removeAll(deadBullets);
        enemies.removeAll(deadEnemies);

        // Player bullets vs asteroids
        deadBullets.clear();
        List<Asteroid> deadRocks = new ArrayList<>();

        for (Bullet b : playerBullets) {
            for (Asteroid a : asteroids) {
                if (a.overlapsBullet(b.getX(), b.getY())) {
                    deadBullets.add(b);
                    deadRocks.add(a);
                    score += 50 * level;
                    break;
                }
            }
        }
        playerBullets.removeAll(deadBullets);
        asteroids.removeAll(deadRocks);

        if (player.isInvincible()) return;   // no damage while flashing

        // Enemy bullets vs player
        deadBullets.clear();
        for (Bullet b : enemyBullets) {
            if (b.overlapsRect(px, py, Player.W, Player.H)) {
                deadBullets.add(b);
                loseLife();
                break;
            }
        }
        enemyBullets.removeAll(deadBullets);

        // Enemy ships vs player
        deadEnemies.clear();
        for (Enemy e : enemies) {
            if (e.overlapsPlayer(px, py)) { deadEnemies.add(e); loseLife(); break; }
        }
        enemies.removeAll(deadEnemies);

        // Asteroids vs player
        deadRocks.clear();
        for (Asteroid a : asteroids) {
            if (a.overlapsPlayer(px, py)) { deadRocks.add(a); loseLife(); break; }
        }
        asteroids.removeAll(deadRocks);
    }

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            if (score > highScore) highScore = score;
            state = State.GAME_OVER;
        } else {
            player.setInvincible(2500);
        }
    }

    private void removeOffscreen() {
        playerBullets.removeIf(b -> b.getY()         < -20);
        enemyBullets.removeIf( b -> b.getY()         > H + 20);
        enemies.removeIf(      e -> e.getY()         > H + 80);
        asteroids.removeIf(    a -> a.getY() + a.getSize() > H + 80);
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void render() {
        // Background
        gc.setFill(Color.rgb(4, 4, 18));
        gc.fillRect(0, 0, W, H);

        stars.forEach(s -> s.render(gc));

        asteroids.forEach(a -> a.render(gc));
        enemies.forEach(e -> e.render(gc));
        enemyBullets.forEach(b -> b.render(gc));
        playerBullets.forEach(b -> b.render(gc));
        player.render(gc);

        renderHUD();
    }

    private void renderHUD() {
        // Separator line
        gc.setStroke(Color.rgb(0, 160, 255, 0.35));
        gc.setLineWidth(1);
        gc.strokeLine(0, HUD_H, W, HUD_H);

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 20));

        // Score
        gc.setFill(Color.CYAN);
        gc.fillText("SCORE: " + score, 20, 36);

        // High score
        gc.setFill(Color.rgb(100, 200, 255));
        gc.fillText("BEST: " + highScore, 20, 62);

        // Lives (mini ships)
        gc.setFill(Color.LIME);
        gc.fillText("LIVES:", W / 2.0 - 70, 36);
        for (int i = 0; i < lives; i++) drawMiniShip(W / 2.0 - 5 + i * 28, 22);

        // Level
        gc.setFill(Color.YELLOW);
        gc.fillText("LEVEL: " + level, W - 130, 36);
    }

    private void drawMiniShip(double x, double y) {
        double cx = x + 8;
        gc.setFill(Color.LIME);
        gc.fillPolygon(
            new double[]{ cx,    x + 16, x + 9, cx,    x + 7,  x    },
            new double[]{ y,     y + 18, y + 13, y + 16, y + 13, y + 18 },
            6
        );
    }

    private void renderStartScreen() {
        gc.setFill(Color.rgb(4, 4, 18));
        gc.fillRect(0, 0, W, H);
        stars.forEach(s -> s.render(gc));

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 62));
        gc.setFill(Color.CYAN);
        gc.fillText("SPACE FIGHT", W / 2.0 - 210, H / 2.0 - 90);

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
        gc.setFill(Color.WHITE);
        gc.fillText("WASD / Arrow Keys  —  Move",   W / 2.0 - 155, H / 2.0 + 10);
        gc.fillText("Auto-fire always on",          W / 2.0 - 95,  H / 2.0 + 42);
        gc.fillText("Destroy enemies & asteroids",  W / 2.0 - 155, H / 2.0 + 74);

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 26));
        gc.setFill(Color.YELLOW);
        gc.fillText("Press ENTER to Start", W / 2.0 - 148, H / 2.0 + 130);
    }

    private void renderGameOver() {
        gc.setFill(Color.rgb(4, 4, 18, 0.78));
        gc.fillRect(0, 0, W, H);
        stars.forEach(s -> s.render(gc));

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 62));
        gc.setFill(Color.RED);
        gc.fillText("GAME OVER", W / 2.0 - 210, H / 2.0 - 70);

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 28));
        gc.setFill(Color.WHITE);
        gc.fillText("Final Score : " + score,     W / 2.0 - 130, H / 2.0 + 10);
        gc.fillText("Level Reached : " + level,   W / 2.0 - 130, H / 2.0 + 50);
        gc.fillText("Best Score : " + highScore,  W / 2.0 - 130, H / 2.0 + 90);

        gc.setFont(Font.font("Courier New", FontWeight.BOLD, 22));
        gc.setFill(Color.YELLOW);
        gc.fillText("Press ENTER to Play Again", W / 2.0 - 165, H / 2.0 + 150);
    }
}
