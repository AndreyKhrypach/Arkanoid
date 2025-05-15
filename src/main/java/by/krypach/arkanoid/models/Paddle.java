package by.krypach.arkanoid.models;

import by.krypach.arkanoid.core.GamePanel;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class Paddle {
    public static final int LASER_COOLDOWN_MS = 500; // Задержка между выстрелами (миллисекунды)
    public static final int LASER_DURATION_MS = 10000; // Длительность активности лазера (10 сек)
    public static final int LASER_BEAM_WIDTH = 3; // Ширина луча в пикселях
    private static final Color LASER_COLOR = Color.RED; // Цвет луча
    public static final int MAX_WIDTH = 200;

    private static final int GAME_WIDTH = 800;
    private static final float ACCELERATION = 2.5f;
    private static final float DECELERATION = 1.8f;
    private static final float MAX_SPEED = 18f;

    private double preciseX;
    private final int y;
    private int width;
    private final int height;
    private int maxXPosition;
    private float currentSpeed;
    private boolean laserActive = false;
    private long lastLaserShotTime = 0; // Время последнего выстрела
    private final List<LaserBeam> laserBeams = new ArrayList<>();

    public Paddle(int x, int y, int width, int height) {
        this.preciseX = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxXPosition = GAME_WIDTH - width;
        this.currentSpeed = 0;
    }

    public void update(double deltaTime) {
        applyDeceleration();
        updatePosition(deltaTime);

        long currentTime = System.currentTimeMillis();
        laserBeams.removeIf(beam ->
                currentTime - lastLaserShotTime > 100 &&
                        beam.getHeight() == GamePanel.HEIGHT
        );
    }

    public void activateLaser() {
        this.laserActive = true;
        this.laserBeams.clear();
    }

    public void deactivateLaser() {
        this.laserActive = false;
        this.laserBeams.clear();
    }

    public void fireLaser() {
        if (laserActive && System.currentTimeMillis() - lastLaserShotTime >= LASER_COOLDOWN_MS) {
            // Создаем луч с эффектом "искры" в начале
            laserBeams.add(new LaserBeam(
                    getX() + width/2 - LASER_BEAM_WIDTH/2,
                    y - GamePanel.HEIGHT,
                    LASER_BEAM_WIDTH,
                    GamePanel.HEIGHT,
                    LASER_COLOR
            ));

            // Добавляем небольшую искру в точке выстрела
            int sparkSize = 8;
            laserBeams.add(new LaserBeam(
                    getX() + width/2 - sparkSize/2,
                    y - sparkSize,
                    sparkSize,
                    sparkSize,
                    new Color(255, 255, 100)
            ));

            lastLaserShotTime = System.currentTimeMillis();
        }
    }

    public List<LaserBeam> getLaserBeams() {
        return laserBeams;
    }

    private void applyDeceleration() {
        if (currentSpeed != 0) {
            currentSpeed = Math.abs(currentSpeed) > DECELERATION
                    ? currentSpeed - (Math.signum(currentSpeed) * DECELERATION)
                    : 0;
        }
    }

    private void updatePosition(double deltaTime) {
        preciseX += currentSpeed * deltaTime * 60;
        preciseX = Math.max(0, Math.min(preciseX, maxXPosition));
    }

    public void move(float direction) {
        currentSpeed += direction * ACCELERATION;
        currentSpeed = Math.max(-MAX_SPEED, Math.min(currentSpeed, MAX_SPEED));
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(getX(), y, width, height);

        if (laserActive) {
            g.setColor(Color.RED);
            int laserIndicatorSize = 10;
            int[] xPoints = {
                    getX() + width/2 - laserIndicatorSize/2,
                    getX() + width/2,
                    getX() + width/2 + laserIndicatorSize/2
            };
            int[] yPoints = {y, y - laserIndicatorSize, y};
            g.fillPolygon(xPoints, yPoints, 3);
        }

        // Отрисовка лазерных лучей
        laserBeams.forEach(laser -> laser.draw(g));
    }

    public void clearLasers() {
        laserBeams.clear();
        laserActive = false;
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), y, width, height);
    }

    public void setWidth(int newWidth) {
        double center = this.preciseX + this.width / 2.0;
        this.width = Math.max(40, newWidth);
        this.maxXPosition = GAME_WIDTH - newWidth;
        this.preciseX = Math.max(0, Math.min(center - newWidth / 2.0, maxXPosition));
    }

    public int getX() {
        return (int) Math.round(preciseX);
    }
    public int getWidth() { return width; }
    public int getY() { return y; }
    public float getCurrentSpeed() { return currentSpeed; }

    public void setCurrentSpeed(float speed) { this.currentSpeed = speed; }
    public void setPreciseX(double x) { this.preciseX = x; }
}
