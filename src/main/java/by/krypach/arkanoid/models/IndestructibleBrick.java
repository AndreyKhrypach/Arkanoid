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
}