package by.krypach.arkanoid;

import java.awt.*;

public class Paddle {
    private double preciseX;  // Точная позиция (double)
    private int x, y;
    private final int width, height;
    private int maxXPosition;
    private float currentSpeed = 0;
    private final float acceleration = 0.5f;
    private final float deceleration = 0.3f;
    private final float maxSpeed = 12f;

    public Paddle(int x, int y, int width, int height) {
        this.preciseX = x;
        this.maxXPosition = 800 - width - 2;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void update(double deltaTime) {
        // Плавное замедление
        if (currentSpeed != 0) {
            currentSpeed = Math.abs(currentSpeed) > deceleration
                    ? currentSpeed - (Math.signum(currentSpeed) * deceleration)
                    : 0;
        }

        // Движение с учётом deltaTime (speed теперь в пикселях/секунду)
        preciseX += currentSpeed * deltaTime * 60;  // Множитель 60 для сохранения текущей "чувствительности"
        preciseX = Math.max(0, Math.min(preciseX, maxXPosition));
        x = (int) Math.round(preciseX);  // Округление для отрисовки и коллизий
    }

    // Движение платформы (влево/вправо)
    public void move(float direction) { // direction: -1 (влево), 1 (вправо), 0 (стоп)
        currentSpeed += direction * acceleration;
        currentSpeed = Math.max(-maxSpeed, Math.min(currentSpeed, maxSpeed));
    }


    // Отрисовка платформы
    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, width, height);
    }

    // Получение "границ" платформы для коллизий
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Геттеры (используются в GamePanel для проверки границ экрана)
    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public int getMaxXPosition() {
        return maxXPosition;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setMaxXPosition(int maxXPosition) {
        this.maxXPosition = maxXPosition;
    }

    public void setCurrentSpeed(float currentSpeed) {
        this.currentSpeed = currentSpeed;
    }
}
