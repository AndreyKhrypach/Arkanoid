package by.krypach.arkanoid;
import java.awt.*;

public class Ball {
    private int x, y;
    private float speedX, speedY;
    private final int size = 20;
    private boolean isStuckToPaddle = true; // Начинаем с прилипшего мяча

    public Ball(int x, int y, float speedX, float speedY) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void move(float deltaTime) {
        if (!isStuckToPaddle) {
            x += speedX * deltaTime;
            y += speedY * deltaTime;
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

    public float getSpeedX() {
        return speedX;
    }

    public void setSpeedX(float speedX) {
        this.speedX = speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public void setSpeedY(float speedY) {
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

    public void setSpeed(float speedX, float speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }
}
