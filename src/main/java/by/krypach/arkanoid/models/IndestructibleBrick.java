package by.krypach.arkanoid.models;

import java.awt.*;

public class IndestructibleBrick extends Brick {
    public IndestructibleBrick(int x, int y, int width, int height, int row, String symbol) {
        super(x, y, width, height, row, Integer.MAX_VALUE, symbol); // Максимальное значение hits
        this.setColor(new Color(70, 70, 70)); // Серый цвет
    }

    @Override
    public boolean hit() {
        // Неразрушимый кирпич не может быть разрушен
        return false;
    }

    @Override
    public boolean isAlive() {
        // Всегда остается "живым"
        return true;
    }

    @Override
    protected void updateColor() {
        // Цвет не меняется
    }

    @Override
    public void draw(Graphics g) {
        if (!isDestroyed) {
            // Сохраняем оригинальный цвет
            Color originalColor = g.getColor();

            // Рисуем основной прямоугольник кирпича
            g.setColor(color);
            g.fillRect(x, y, width, height);

            // Рисуем текст "ЩИТ"
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String text = "ЩИТ";
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, x + (width - textWidth)/2, y + height/2 + 5);

            // Рисуем белую рамку
            g.setColor(Color.WHITE);
            g.drawRect(x, y, width, height);

            // Восстанавливаем оригинальный цвет
            g.setColor(originalColor);
        }
    }
}
