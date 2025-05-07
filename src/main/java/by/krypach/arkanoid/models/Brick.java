package by.krypach.arkanoid.models;

import java.awt.*;

public class Brick {
    private final int x, y;
    private final int width, height;
    private final int row;
    private boolean isDestroyed;
    private Color color;
    private int maxHits;
    private int currentHits;
    private final Color[] hitColors;


    public Brick(int x, int y, int width, int height, int row, int maxHits) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.row = row;
        this.isDestroyed = false;
        this.maxHits = maxHits;
        this.currentHits = 0;

        this.hitColors = new Color[] {
                new Color(255, 182, 193), // 1 удар
                Color.YELLOW,             // 2 удара
                Color.GREEN,              // 3 удара
                Color.RED,                // 4 удара
                new Color(128, 0, 128)    // 5 ударов
        };

        this.color = hitColors[Math.min(maxHits - 1, hitColors.length - 1)];
    }

    public void draw(Graphics g) {
        if (!isDestroyed) {
            g.setColor(color);
            g.fillRect(x, y, width, height);
            g.setColor(Color.WHITE);
            g.drawRect(x, y, width, height);

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

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean hit() {
        currentHits++;
        if (currentHits >= maxHits) {
            isDestroyed = true;
            return true;
        }
        return false;
    }

    public void destroy() {
        isDestroyed = true;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public boolean isAlive() {
        return !isDestroyed && currentHits < maxHits;
    }

    public void reset() {
        this.isDestroyed = false;
        this.currentHits = 0;
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

    public void setColor(Color color) {
        this.color = color;
    }
}
