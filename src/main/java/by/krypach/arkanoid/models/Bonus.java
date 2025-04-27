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
            // Здесь будут другие типы бонусов
        }
        g.fillRect(x, y, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, WIDTH, HEIGHT);
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

