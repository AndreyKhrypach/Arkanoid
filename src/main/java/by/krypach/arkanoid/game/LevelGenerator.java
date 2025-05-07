package by.krypach.arkanoid.game;
import by.krypach.arkanoid.models.Brick;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LevelGenerator {
    private static final int BRICK_WIDTH = 70;
    private static final int BRICK_HEIGHT = 20;
    private static final int BRICK_HGAP = 10;
    private static final int BRICK_VGAP = 10;
    private static final int BRICK_TOP_MARGIN = 50;
    private static final int BRICK_LEFT_MARGIN = 10;

    public Level generateLevel(int levelNumber, int rows, int cols, Random random) {
        List<Brick> bricks = new ArrayList<>();
        Color baseColor = getBaseColorForLevel(levelNumber);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * (BRICK_WIDTH + BRICK_HGAP) + BRICK_LEFT_MARGIN;
                int y = row * (BRICK_HEIGHT + BRICK_VGAP) + BRICK_TOP_MARGIN;

                int hitsRequired = determineHitsRequired(levelNumber, row, random);
                Brick brick = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, row+1, hitsRequired);

                if (levelNumber == 1) {
                    brick.setColor(new Color(255, 182, 193)); // Розовый для 1 уровня
                } else {
                    brick.setColor(getRowColor(row));
                }

                bricks.add(brick);
            }
        }

        return new Level(levelNumber, bricks, levelNumber > 1, baseColor);
    }

    private Color getRowColor(int row) {
        return switch (row % 7) {
            case 0 -> new Color(100, 200, 255);  // Голубой
            case 1 -> new Color(255, 182, 193);  // Розовый
            case 2 -> new Color(144, 238, 144);  // Зеленый
            case 3 -> new Color(255, 255, 150);  // Желтый
            case 4 -> new Color(200, 150, 255);  // Фиолетовый
            case 5 -> new Color(255, 150, 150);  // Красный
            default -> new Color(150, 150, 255);  // Синий
        };
    }

    private int determineHitsRequired(int level, int row, Random random) {
        if (level == 1) return 1;

        double chance = random.nextDouble();
        switch (row) {
            case 0: return chance < 0.6 ? 1 : chance < 0.85 ? 2 : 3;
            case 1: return chance < 0.4 ? 1 : chance < 0.7 ? 2 : 3;
            case 2: return chance < 0.2 ? 1 : chance < 0.5 ? 2 : 3;
            case 3: return chance < 0.1 ? 1 : chance < 0.3 ? 2 : 3;
            default: return 1;
        }
    }

    private Color getBaseColorForLevel(int level) {
        Color[] colors = {
                new Color(100, 200, 255),  // Голубой
                new Color(255, 182, 193),  // Розовый
                new Color(144, 238, 144),  // Светло-зеленый
                new Color(255, 255, 150),  // Желтый
                new Color(200, 150, 255),  // Фиолетовый
                new Color(255, 150, 150),  // Красный
                new Color(150, 150, 255)   // Синий
        };
        return colors[level % colors.length];
    }
}
