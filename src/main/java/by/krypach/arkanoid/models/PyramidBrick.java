package by.krypach.arkanoid.models;

import java.awt.*;

public class PyramidBrick extends Brick {
    private boolean isActive;
    private final int layer;

    public PyramidBrick(int x, int y, int width, int height, int row, int maxHits, String chessSymbol, int layer) {
        super(x, y, width, height, row, maxHits, chessSymbol);
        this.layer = layer;
        this.isActive = (layer == 5); // Только верхний слой (5) активен изначально
        if (!isActive) {
            setColor(new Color(100, 100, 100)); // Серый цвет для неактивных кирпичей
        } else {
            setColor(getColorForHits(layer)); // Цвет согласно прочности
        }
    }

    @Override
    public boolean isAlive() {
        return !isDestroyed && isActive;
    }

    public void activate(int hits) {
        this.isActive = true;
        this.maxHits = hits;
        this.currentHits = 0;
        setColor(getColorForHits(hits));
    }

    public int getLayer() {
        return layer;
    }

    public boolean isActive() {
        return isActive;
    }

    private Color getColorForHits(int hits) {
        return switch (hits) {
            case 1 -> new Color(255, 182, 193); // Розовый
            case 2 -> Color.YELLOW;
            case 3 -> Color.GREEN;
            case 4 -> Color.RED;
            case 5 -> new Color(128, 0, 128);   // Фиолетовый
            default -> Color.WHITE;
        };
    }
}
