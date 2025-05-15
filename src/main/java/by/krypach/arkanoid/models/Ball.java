package by.krypach.arkanoid.models;
import java.awt.*;

public class Ball {
    public static final double MAX_SPEED = 600.0;
    public static final double MIN_SPEED = 225.0;
    public static final double SPEED_BOOST = 1.8;
    public static final double BOOST_FADE_TIME = 3.0;
    public static final int DEFAULT_SIZE = 20;

    private double x, y;
    private double speedX, speedY;
    private final int size = 20;
    private boolean isStuckToPaddle = true;
    private Color color = Color.WHITE;
    private double speedMultiplier = 1.0;

    public Ball(int x, int y, double speedX, double speedY) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void move(double deltaTime) {
        if (!isStuckToPaddle) {
            if (speedMultiplier > 1.0) {
                speedMultiplier = Math.max(1.0, speedMultiplier - (deltaTime / BOOST_FADE_TIME) * (SPEED_BOOST - 1.0));
            }

            updateColorBasedOnBoost();

            double effectiveSpeedX = speedX * speedMultiplier;
            double effectiveSpeedY = speedY * speedMultiplier;

            double currentSpeed = Math.sqrt(effectiveSpeedX*effectiveSpeedX + effectiveSpeedY*effectiveSpeedY);
            if (currentSpeed > MAX_SPEED) {
                double ratio = MAX_SPEED / currentSpeed;
                effectiveSpeedX *= ratio;
                effectiveSpeedY *= ratio;
            }

            x += effectiveSpeedX * deltaTime;
            y += effectiveSpeedY * deltaTime;
        }
    }

    public void reverseX() { speedX *= -1; }
    public void reverseY() { speedY *= -1; }

    public Rectangle getBounds() {
        return new Rectangle((int)Math.round(x), (int)Math.round(y), size, size);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval((int)Math.round(x), (int)Math.round(y), size, size);

        g.setColor(Color.WHITE);
        g.drawOval((int)Math.round(x), (int)Math.round(y), size, size);
    }

    public int getX() { return (int)Math.round(x); }
    public int getY() { return (int)Math.round(y); }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getSpeedX() { return speedX; }
    public double getSpeedY() { return speedY; }
    public void setSpeedX(double speedX) { this.speedX = speedX; }
    public int getSize() { return size; }
    public boolean isStuckToPaddle() { return isStuckToPaddle; }
    public void setStuckToPaddle(boolean stuckToPaddle) {
        isStuckToPaddle = stuckToPaddle;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setSpeed(double speedX, double speedY) {
        this.speedX = speedX;
        this.speedY = speedY;

        double currentSpeed = Math.sqrt(speedX*speedX + speedY*speedY);
        if (currentSpeed > MAX_SPEED) {
            double ratio = MAX_SPEED / currentSpeed;
            this.speedX *= ratio;
            this.speedY *= ratio;
        }
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

    public void updateColorBasedOnBoost() {
        if (speedMultiplier <= 1.0) {
            this.color = Color.WHITE;
        } else {
            float boostProgress = (float)((speedMultiplier - 1.0) / (SPEED_BOOST - 1.0));
            this.color = new Color(
                    1.0f,                           // Красный
                    Math.max(0.3f, 1.0f - 0.7f * boostProgress), // Зеленый
                    Math.max(0.0f, 0.3f - 0.3f * boostProgress)  // Синий
            );
        }
    }
}
