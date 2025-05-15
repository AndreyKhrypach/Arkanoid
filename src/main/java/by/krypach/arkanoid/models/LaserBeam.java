package by.krypach.arkanoid.models;
import java.awt.*;

public class LaserBeam {
    private final int x, y, width, height;
    private final Color color;

    public LaserBeam(int x, int y, int width, int height, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, width, height);

        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Градиент для эффекта свечения
        GradientPaint gradient = new GradientPaint(
                x, y, new Color(255, 100, 100, 150),
                x + width, y, new Color(255, 50, 50, 50)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(x - 2, y, width + 4, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getHeight() {
        return height;
    }
}
