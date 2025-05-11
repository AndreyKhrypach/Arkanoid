package by.krypach.arkanoid.game;

import by.krypach.arkanoid.enums.BonusType;
import by.krypach.arkanoid.models.Brick;

import java.awt.*;
import java.util.*;
import java.util.List;

public class LevelGenerator {
    private static final int BRICK_WIDTH = 70;
    private static final int BRICK_HEIGHT = 20;
    private static final int BRICK_HGAP = 10;
    private static final int BRICK_VGAP = 10;
    private static final int BRICK_TOP_MARGIN = 50;
    private static final int BRICK_LEFT_MARGIN = 10;
    private Random random = new Random();

    public Level generateLevel(int levelNumber, int rows, int cols, Random random) {
        List<Brick> bricks = new ArrayList<>();
        boolean hasBonuses = levelNumber > 1;

        if (levelNumber == 1) {
            return generateFirstLevel(rows, cols);
        }

        // Создаем список всех возможных позиций кирпичей
        List<Point> positions = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                positions.add(new Point(col, row));
            }
        }
        Collections.shuffle(positions, random); // Перемешиваем позиции

        // Рассчитываем количество кирпичей каждого типа
        int totalBricks = rows * cols;
        int[] brickCounts = {
                (int) Math.round(totalBricks * 0.40), // 1 удар (40%)
                (int) Math.round(totalBricks * 0.25), // 2 удара (25%)
                (int) Math.round(totalBricks * 0.15), // 3 удара (15%)
                (int) Math.round(totalBricks * 0.10), // 4 удара (10%)
                (int) Math.round(totalBricks * 0.10)  // 5 ударов (10%)
        };

        // Распределяем кирпичи по перемешанным позициям
        int brickType = 0;

        for (Point pos : positions) {
            int x = pos.x * (BRICK_WIDTH + BRICK_HGAP) + BRICK_LEFT_MARGIN;
            int y = pos.y * (BRICK_HEIGHT + BRICK_VGAP) + BRICK_TOP_MARGIN;

            // Выбираем тип кирпича
            while (brickType < brickCounts.length && brickCounts[brickType] <= 0) {
                brickType++;
            }
            if (brickType >= brickCounts.length) brickType = brickCounts.length - 1;

            int hitsRequired = brickType + 1;
            Brick brick = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, pos.y + 1, hitsRequired);
            if (hasBonuses) {
                brick.setBonusType(getRandomBonusType(levelNumber));
            }
            bricks.add(brick);

            brickCounts[brickType]--;
        }

        return new Level(levelNumber, bricks, hasBonuses, Color.BLACK);
    }

    public Level generateFirstLevel(int rows, int cols) {
        List<Brick> bricks = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * (BRICK_WIDTH + BRICK_HGAP) + BRICK_LEFT_MARGIN;
                int y = row * (BRICK_HEIGHT + BRICK_VGAP) + BRICK_TOP_MARGIN;
                Brick brick = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, row + 1, 1);
                brick.setColor(new Color(255, 182, 193)); // Розовый для 1 уровня
                bricks.add(brick);
            }
        }
        return new Level(1, bricks, false, Color.BLACK);
    }

    public Level generateLevel3() {
        List<Brick> bricks = new ArrayList<>();
        int trapCounter = 0;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 10; col++) {
                int x = col * (BRICK_WIDTH + BRICK_HGAP) + BRICK_LEFT_MARGIN;
                int y = row * (BRICK_HEIGHT + BRICK_VGAP) + BRICK_TOP_MARGIN;

                // Определяем прочность кирпича (1-5 ударов)
                int maxHits = (row % 3) + 1; // Чередуем прочность

                Brick brick = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, row + 1, maxHits);

                // Каждый 4-й кирпич - ловушка
                trapCounter++;
                if (trapCounter % 4 == 0) {
                    brick.setBonusType(BonusType.TRAP_SHRINK_PADDLE);
                } else {
                    brick.setBonusType(getRandomBonusType(3));
                }

                bricks.add(brick);
            }
        }

        return new Level(3, bricks, true, new Color(30, 30, 70)); // Синий фон для уровня
    }

    public BonusType getRandomBonusType(int levelNumber) {
        List<BonusType> availableBonuses = new ArrayList<>(Arrays.asList(BonusType.values()));
        if (levelNumber != 3) {
            availableBonuses.remove(BonusType.TRAP_SHRINK_PADDLE);
        }
        if (availableBonuses.isEmpty()) {
            return null;
        }
        return availableBonuses.get(random.nextInt(availableBonuses.size()));
    }
}
