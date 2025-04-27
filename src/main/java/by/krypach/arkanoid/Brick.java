package by.krypach.arkanoid;

import java.awt.*;

public class Brick {
    private final int x, y;
    private final int width, height;
    private final int row; // Добавляем номер ряда (1-5)
    private boolean isDestroyed;
    private final Color color; // Цвет в зависимости от ряда

    public Brick(int x, int y, int width, int height, int row) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.row = row;
        this.isDestroyed = false;

        // Устанавливаем цвет в зависимости от ряда
        switch(row) {
            case 1: this.color = new Color(255, 182, 193); // Розовый
                break;
            case 2: this.color = Color.YELLOW;
                break;
            case 3: this.color = Color.GREEN;
                break;
            case 4: this.color = Color.RED;
                break;
            case 5: this.color = new Color(128, 0, 128); // Фиолетовый
                break;
            default: this.color = Color.GRAY; // На случай ошибки
        }
    }

    // Отрисовка кирпича (если не разрушен)
    public void draw(Graphics g) {
        if (!isDestroyed) {
            g.setColor(color);
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

    public int getRow() {
        return row;
    }
}
