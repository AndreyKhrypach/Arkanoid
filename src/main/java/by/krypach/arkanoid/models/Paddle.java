package by.krypach.arkanoid.models;

import java.awt.*;

public class Paddle {
    private static final int GAME_WIDTH = 800;
    private static final float ACCELERATION = 0.75f;
    private static final float DECELERATION = 0.3f;
    private static final float MAX_SPEED = 18f;

    private double preciseX;
    private final int y;
    private int width;
    private final int height;
    private int maxXPosition;
    private float currentSpeed;

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
    }

    public Rectangle getBounds() {
        return new Rectangle(getX(), y, width, height);
    }

    public void setWidth(int newWidth) {
        double center = this.preciseX + this.width / 2.0;
        this.width = newWidth;
        this.maxXPosition = GAME_WIDTH - newWidth;
        this.preciseX = Math.max(0, Math.min(center - newWidth / 2.0, maxXPosition));
    }

    // Геттеры
    public int getX() {
        return (int) Math.round(preciseX);
    }
    public int getWidth() { return width; }
    public int getY() { return y; }
    public int getHeight() { return height; }
    public float getCurrentSpeed() { return currentSpeed; }

    // Сеттеры
    public void setCurrentSpeed(float speed) { this.currentSpeed = speed; }
    public void setPreciseX(double x) { this.preciseX = x; }
}
