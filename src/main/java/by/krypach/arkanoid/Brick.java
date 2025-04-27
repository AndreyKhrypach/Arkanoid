package by.krypach.arkanoid;

import java.awt.*;

public class Brick {
    private final int x, y;
    private final int width, height;
    private final int row; // Добавляем номер ряда (1-5)
    private boolean isDestroyed;
    private final Color color; // Цвет в зависимости от ряда
    private int maxHits;  // Сколько всего нужно ударов для разрушения
    private int currentHits; // Сколько ударов уже было
    private final Color[] hitColors; // Цвета для разных уровней живучести

    public Brick(int x, int y, int width, int height, int row, int maxHits) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.row = row;
        this.isDestroyed = false;
        this.maxHits = maxHits;
        this.currentHits = 0;

        // Цвета для разных уровней живучести (можно настроить)
        this.hitColors = new Color[] {
                new Color(255, 182, 193), // 1 удар
                Color.YELLOW,             // 2 удара
                Color.GREEN,              // 3 удара
                Color.RED,                // 4 удара
                new Color(128, 0, 128)    // 5 ударов
        };

        // Устанавливаем цвет в зависимости от живучести (но не более 5)
        this.color = hitColors[Math.min(maxHits - 1, hitColors.length - 1)];
    }

    // Отрисовка кирпича (теперь с цифрой живучести)
    public void draw(Graphics g) {
        if (!isDestroyed) {
            g.setColor(color);
            g.fillRect(x, y, width, height);
            g.setColor(Color.WHITE);
            g.drawRect(x, y, width, height);

            // Рисуем цифру оставшихся ударов
            int hitsLeft = maxHits - currentHits;
            String hitsText = String.valueOf(hitsLeft);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));

            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(hitsText);
            int textHeight = fm.getAscent();

            g.drawString(hitsText,
                    x + (width - textWidth) / 2,
                    y + (height + textHeight) / 2 - 2);
        }
    }

    // Получение "границ" кирпича для коллизий
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // Обработка попадания по кирпичу
    public boolean hit() {
        currentHits++;
        if (currentHits >= maxHits) {
            isDestroyed = true;
            return true; // Кирпич разрушен
        }
        return false; // Кирпич еще жив
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

    public int getMaxHits() {
        return maxHits;
    }
}
