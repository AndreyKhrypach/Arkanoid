package by.krypach.arkanoid.models;

import by.krypach.arkanoid.enums.BonusType;

import java.awt.*;

public class Bonus {
    public static final int WIDTH = 30;
    public static final int HEIGHT = 15;
    public static final int SPEED = 100;

    private int x, y;
    private final BonusType type;
    private boolean active;
    private double pulseScale = 1.0f;
    private boolean pulseGrowing = true;
    private final Color heartColor = new Color(255, 50, 50); // Ярко-красный

    public Bonus(int x, int y, BonusType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.active = true;
    }

    public void update(double deltaTime) {
        y += (int) (SPEED * deltaTime);
    }

    public void draw(Graphics g) {
        if (!active) return;

        // Общий прямоугольник для всех бонусов
        g.setColor(new Color(50, 50, 50, 150)); // Темно-серый с прозрачностью
        g.fillRect(x, y, WIDTH, HEIGHT);

        switch (type) {
            case PADDLE_EXTEND -> { // Синий прямоугольник
                g.setColor(Color.CYAN);
                g.fillRect(x, y, WIDTH, HEIGHT);
                g.setColor(Color.BLACK);
                g.drawString("→|←", x + 5, y + 12);
            }
            case BALL_SPEED_UP -> { // Оранжевый прямоугольник
                g.setColor(Color.ORANGE);
                g.fillRect(x, y, WIDTH, HEIGHT);
                g.setColor(Color.RED);
                g.drawString(">>", x + 8, y + 12);
            }
            case EXTRA_BALL -> { // Круглый бонус
                g.setColor(Color.WHITE);
                g.fillOval(x + 3, y + 3, WIDTH - 6, HEIGHT - 6);

                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString("+", x + 11, y + 15);

                g.setColor(new Color(255, 150, 0));
                g.drawOval(x + 3, y + 3, WIDTH - 6, HEIGHT - 6);
            }
            case TIME_SLOW -> { // Синий прямоугольник
                g.setColor(new Color(100, 100, 255));
                g.fillRect(x, y, WIDTH, HEIGHT);
                g.setColor(Color.WHITE);
                g.drawString("⌛", x + 8, y + 12);
            }
            case EXTRA_LIFE -> {
                int centerX = x + WIDTH / 2;
                int centerY = y + HEIGHT / 2;
                int size = (int) (15 * pulseScale);

                g.setColor(heartColor);

                g.fillArc(centerX - size, centerY - size / 2, size, size, 0, 180);
                g.fillArc(centerX, centerY - size / 2, size, size, 0, 180);
                int[] xPoints = {centerX - size, centerX + size, centerX};
                int[] yPoints = {centerY, centerY, centerY + size};
                g.fillPolygon(xPoints, yPoints, 3);

                g.setColor(Color.WHITE);
                g.drawArc(centerX - size, centerY - size / 2, size, size, 0, 180);
                g.drawArc(centerX, centerY - size / 2, size, size, 0, 180);
                g.drawPolygon(xPoints, yPoints, 3);
            }
            case LASER_GUN -> {
                int[] xPoints = {x, x + WIDTH / 2, x + WIDTH};
                int[] yPoints = {y + HEIGHT, y, y + HEIGHT};
                g.setColor(Color.RED);
                g.fillPolygon(xPoints, yPoints, 3);
            }
        }

        g.setColor(Color.BLACK);
        g.drawRect(x, y, WIDTH, HEIGHT);
    }

    public boolean checkCollision(Rectangle otherBounds) {
        if (!active) return false;

        Rectangle bonusBounds = new Rectangle(x, y, WIDTH, HEIGHT);
        if (bonusBounds.intersects(otherBounds)) {
            active = false; // Деактивируем бонус при столкновении
            return true;
        }
        return false;
    }

    public BonusType getType() {
        return type;
    }

    public int getY() {
        return y;
    }

    public double getPulseScale() {
        return pulseScale;
    }

    public void setPulseScale(double pulseScale) {
        this.pulseScale = pulseScale;
    }

    public boolean isPulseGrowing() {
        return pulseGrowing;
    }

    public void setPulseGrowing(boolean pulseGrowing) {
        this.pulseGrowing = pulseGrowing;
    }
}
