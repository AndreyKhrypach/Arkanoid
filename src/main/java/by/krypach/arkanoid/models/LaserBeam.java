package by.krypach.arkanoid.models;
import java.awt.*;

public class LaserBeam {
    private int x, y, width, height;
    private final Color color;
    private long spawnTime; // Время создания луча
    private final long durationMs = 50; // Длительность отображения в миллисекундах
    private boolean hitProcessed = false; // Флаг, что попадание уже обработано
    private final boolean isDownward;

    public LaserBeam(int x, int y, int width, int height, Color color, boolean isDownward) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.isDownward = isDownward;
        this.spawnTime = System.currentTimeMillis();
    }

    public void draw(Graphics g) {
        if (height <= 0) return;

        Graphics2D g2d = (Graphics2D)g;
        Object oldAntialias = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Яркое ядро луча
        g2d.setColor(new Color(255, 50, 50, 200));
        if (isDownward) {
            // Луч вниз (от босса)
            g2d.fillRect(x, y, width, height);
        } else {
            // Луч вверх (от платформы)
            g2d.fillRect(x, y - height, width, height);
        }

        // Внешнее свечение
        GradientPaint gradient;
        if (isDownward) {
            gradient = new GradientPaint(
                    x, y, new Color(255, 100, 100, 100),
                    x, y + height, new Color(255, 100, 100, 50)
            );
        } else {
            gradient = new GradientPaint(
                    x, y - height, new Color(255, 100, 100, 100),
                    x, y, new Color(255, 100, 100, 50)
            );
        }

        g2d.setPaint(gradient);
        if (isDownward) {
            g2d.fillRect(x - 2, y, width + 4, height);
        } else {
            g2d.fillRect(x - 2, y - height, width + 4, height);
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialias);
    }

    public boolean isAlive() {
        return System.currentTimeMillis() - spawnTime < durationMs;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isHitProcessed() {
        return hitProcessed;
    }

    public boolean isDownward() {
        return isDownward;
    }

    public void setHitProcessed() {
        this.hitProcessed = true;
    }
}
