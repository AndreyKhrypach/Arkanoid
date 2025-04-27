package by.krypach.arkanoid;
import java.awt.*;

public class Ball {
    private int x, y;
    private double speedX, speedY;
    private final int size = 20;
    private boolean isStuckToPaddle = true; // Начинаем с прилипшего мяча

    public Ball(int x, int y, double speedX, double speedY) {
        this.x = x;
        this.y = y;
        this.speedX = speedX / 60.0;
        this.speedY = speedY / 60.0;
    }

    public void move(double deltaTime) {
        if (!isStuckToPaddle) {
            x += (int) (speedX * deltaTime);
            y += (int) (speedY * deltaTime);
        }
    }

    public void reverseX() { speedX *= -1; }
    public void reverseY() { speedY *= -1; }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillOval(x, y, size, size);
    }

    // Геттеры (нужны для GamePanel)
    public int getX() { return x; }

    public void setX(int x) {
        this.x = x;
    }

    public double getSpeedX() {
        return speedX;
    }

    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }

    public int getY() { return y; }

    public void setY(int y) {
        this.y = y;
    }

    public int getSize() { return size; }

    public boolean isStuckToPaddle() {
        return isStuckToPaddle;
    }

    public void setStuckToPaddle(boolean stuckToPaddle) {
        isStuckToPaddle = stuckToPaddle;
    }

    public void setSpeed(double speedX, double speedY) {
        this.speedX = speedX / 60.0;
        this.speedY = speedY / 60.0;
    }
}
