package by.krypach.arkanoid.models;
import java.awt.*;

public class Ball {
    // Публичные константы для настройки игры
    public static final double MAX_SPEED = 900.0;
    public static final double MIN_SPEED = 225.0;
    public static final double SPEED_DAMPING = 0.995; // Легкое трение (0.98 = 2% потерь за кадр)

    private double x, y;
    private double speedX, speedY;
    private final int size = 20;
    private boolean isStuckToPaddle = true;

    public Ball(int x, int y, double speedX, double speedY) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void move(double deltaTime) {
        if (!isStuckToPaddle) {
            // Ограничение максимальной скорости (сохраняя направление)
            double currentSpeed = Math.sqrt(speedX*speedX + speedY*speedY);
            if (currentSpeed > MAX_SPEED) {
                double ratio = MAX_SPEED / currentSpeed;
                speedX *= ratio;
                speedY *= ratio;
            }

            // Применяем трение только если скорость выше минимальной
            if (currentSpeed > MIN_SPEED) {
                speedX *= SPEED_DAMPING;
                speedY *= SPEED_DAMPING;
            }

            // Гарантируем минимальную скорость
            ensureMinimumSpeed();

            // Движение
            x += speedX * deltaTime;
            y += speedY * deltaTime;
        }
    }

    public void reverseX() { speedX *= -1; }
    public void reverseY() { speedY *= -1; }

    public Rectangle getBounds() {
        return new Rectangle((int)Math.round(x), (int)Math.round(y), size, size);
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillOval((int)Math.round(x), (int)Math.round(y), size, size);
    }

    // Геттеры и сеттеры
    public int getX() { return (int)Math.round(x); }
    public int getY() { return (int)Math.round(y); }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getSpeedX() { return speedX; }
    public double getSpeedY() { return speedY; }
    public void setSpeedX(double speedX) { this.speedX = speedX; }
    public void setSpeedY(double speedY) { this.speedY = speedY; }
    public int getSize() { return size; }
    public boolean isStuckToPaddle() { return isStuckToPaddle; }
    public void setStuckToPaddle(boolean stuckToPaddle) {
        isStuckToPaddle = stuckToPaddle;
    }

    public void setSpeed(double speedX, double speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
        ensureMinimumSpeed();
    }

    private void ensureMinimumSpeed() {
        double currentSpeed = Math.sqrt(speedX*speedX + speedY*speedY);
        if (currentSpeed < MIN_SPEED && currentSpeed > 0) {
            double ratio = MIN_SPEED / currentSpeed;
            speedX *= ratio;
            speedY *= ratio;
        }
    }
}
