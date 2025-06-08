package by.krypach.arkanoid.models;

import by.krypach.arkanoid.enums.BonusType;

import java.awt.*;

public class Brick {

    protected int x, y;
    protected final int width, height;
    private final int row;
    protected boolean isDestroyed;
    protected Color color;
    protected int maxHits;
    protected int currentHits;
    private BonusType bonusType;
    private String chessSymbol;

    private static final Color[] HIT_COLORS = {
            new Color(255, 182, 193), // 1 удар (розовый)
            Color.YELLOW,             // 2 удара (желтый)
            Color.GREEN,              // 3 удара (зеленый)
            Color.RED,                // 4 удара (красный)
            new Color(128, 0, 128)    // 5 ударов (фиолетовый)
    };

    public Brick(int x, int y, int width, int height, int row, int maxHits, String chessSymbol) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.row = row;
        this.isDestroyed = false;
        this.maxHits = maxHits;
        this.currentHits = 0;
        this.chessSymbol = chessSymbol;
        updateColor();
    }

    public void draw(Graphics g) {
        if (!isDestroyed) {
            // Сохраняем оригинальный цвет
            Color originalColor = g.getColor();

            // Рисуем основной прямоугольник кирпича
            g.setColor(color);
            g.fillRect(x, y, width, height);

            // Особый стиль для кирпича "EXIT"
            if ("EXIT".equals(chessSymbol)) {
                g.setColor(Color.YELLOW);
                g.fillRect(x + 2, y + 2, width - 4, height - 4);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString("EXIT", x + width / 2 - 15, y + height / 2 + 5);
            }

            // Рисуем белую рамку
            g.setColor(Color.WHITE);
            g.drawRect(x, y,width, height);

            // Восстанавливаем оригинальный цвет
            g.setColor(originalColor);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean hit() {
        currentHits += 1;
        updateColor();
        if (currentHits >= maxHits) {
            isDestroyed = true;
            return true;
        }
        return false;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public boolean isAlive() {
        return !isDestroyed && currentHits < maxHits;
    }

    public int getRow() {
        return row;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public BonusType getBonusType() {
        return bonusType;
    }

    public void setBonusType(BonusType bonusType) {
        this.bonusType = bonusType;
    }

    public String getChessSymbol() {
        return chessSymbol;
    }

    public int getCurrentHits() {
        return currentHits;
    }

    public Color getColor() {
        return color;
    }

    protected void updateColor() {
        int hitsLeft = maxHits - currentHits;
        int colorIndex = Math.max(0, Math.min(hitsLeft - 1, HIT_COLORS.length - 1));
        this.color = HIT_COLORS[colorIndex];
    }
}
