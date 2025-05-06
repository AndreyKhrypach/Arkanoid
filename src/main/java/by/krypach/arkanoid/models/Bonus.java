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

        switch(type) {
            case PADDLE_EXTEND:
                g.setColor(Color.CYAN);
                break;
            case BALL_SPEED_UP:
                g.setColor(Color.ORANGE);
                break;
            case EXTRA_BALL:
                g.setColor(Color.MAGENTA);
                break;
        }
        g.fillRect(x, y, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, WIDTH, HEIGHT);

        // Добавляем обозначения для новых бонусов
        switch (type) {
            case BALL_SPEED_UP:
                g.setColor(Color.RED);
                g.drawString(">>", x + 8, y + 12);
                break;
            case EXTRA_BALL:
                g.setColor(Color.WHITE);
                g.drawString("+1", x + 8, y + 12);
                break;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public BonusType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

