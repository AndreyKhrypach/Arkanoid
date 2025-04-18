package by.krypach.arkanoid;

import java.awt.*;

public class Brick {
    private final int x, y;
    private final int width, height;
    private boolean isDestroyed;

    public Brick(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isDestroyed = false;
    }

    // Отрисовка кирпича (если не разрушен)
    public void draw(Graphics g) {
        if (!isDestroyed) {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
            g.setColor(Color.WHITE);
            g.drawRect(x, y, width, height); // Граница для красоты
        }
    }

    // Получение "границ" кирпича для коллизий
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Разрушение кирпича
    public void destroy() {
        isDestroyed = true;
    }

    // Проверка, разрушен ли кирпич
    public boolean isDestroyed() {
        return isDestroyed;
    }
}
