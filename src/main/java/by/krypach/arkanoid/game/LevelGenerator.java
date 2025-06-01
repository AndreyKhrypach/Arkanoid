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
    // Черные фигуры (стандартные Unicode)
    public static final String BLACK_KING = "♔";
    public static final String BLACK_QUEEN = "♕";
    public static final String BLACK_ROOK = "♖";
    public static final String BLACK_BISHOP = "♗";
    public static final String BLACK_KNIGHT = "♘";
    public static final String BLACK_PAWN = "♙";

    // Белые фигуры (используем другие Unicode символы)
    public static final String WHITE_KING = "♚";
    public static final String WHITE_QUEEN = "♛";
    public static final String WHITE_ROOK = "♜";
    public static final String WHITE_BISHOP = "♝";
    public static final String WHITE_KNIGHT = "♞";
    public static final String WHITE_PAWN = "♟";
    private final Random random = new Random();

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
            Brick brick = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, pos.y + 1, hitsRequired, "");
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
                Brick brick = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, row + 1, 1, "");
                brick.setColor(new Color(255, 182, 193)); // Розовый для 1 уровня
                bricks.add(brick);
            }
        }
        return new Level(1, bricks, false, Color.BLACK);
    }

    public Level generateChessLevel() {
        List<Brick> bricks = new ArrayList<>();

        String[][] chessBoard = {
                {" ", BLACK_ROOK, BLACK_KNIGHT, BLACK_BISHOP, BLACK_QUEEN, BLACK_KING, BLACK_BISHOP, BLACK_KNIGHT, BLACK_ROOK, " "},
                {" ", BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, BLACK_PAWN, " "},
                {" ", " ", " ", " ", " ", " ", " ", " ", " ", " "},
                {" ", WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, WHITE_PAWN, " "},
                {" ", WHITE_ROOK, WHITE_KNIGHT, WHITE_BISHOP, WHITE_QUEEN, WHITE_KING, WHITE_BISHOP, WHITE_KNIGHT, WHITE_ROOK, " "}
        };

        // Счетчик для распределения прочности кирпичей
        int hitCounter = 1;

        for (int row = 0; row < chessBoard.length; row++) {
            for (int col = 0; col < chessBoard[row].length; col++) {
                int x = col * (BRICK_WIDTH + BRICK_HGAP) + BRICK_LEFT_MARGIN;
                int y = row * (BRICK_HEIGHT + BRICK_VGAP) + BRICK_TOP_MARGIN;
                String symbol = chessBoard[row][col];

                // Определяем прочность кирпича
                int hits; // По умолчанию 1 удар
                if (!symbol.trim().isEmpty()) {
                    hits = getHitsForSymbol(symbol);
                } else {
                    // Для пустых кирпичей чередуем прочность 1-5
                    hits = hitCounter % 5 + 1;
                    hitCounter++;
                }

                // Создаем кирпич
                Brick brick = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, row + 1, hits, symbol);

                // Устанавливаем цвет по количеству ударов
                brick.setColor(getColorForHits(hits));

                // Для фигур добавляем бонусы
                if (!symbol.trim().isEmpty()) {
                    brick.setBonusType(getRandomBonusType(4));
                }

                bricks.add(brick);
            }
        }

        return new Level(4, bricks, true, new Color(30, 30, 30));
    }

    public Level generateMazeLevel() {
        List<Brick> bricks = new ArrayList<>();
        int[][] mazePattern = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 1, 0, 0, 0, 1, 0, 1},
                {1, 0, 1, 1, 0, 1, 0, 1, 0, 1},
                {1, 0, 0, 0, 0, 1, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        for (int row = 0; row < mazePattern.length; row++) {
            for (int col = 0; col < mazePattern[row].length; col++) {
                if (mazePattern[row][col] == 1) {
                    int x = col * (BRICK_WIDTH + BRICK_HGAP) + BRICK_LEFT_MARGIN;
                    int y = row * (BRICK_HEIGHT + BRICK_VGAP) + BRICK_TOP_MARGIN;

                    // Создаем прочные кирпичи для стен лабиринта
                    Brick brick = new Brick(x, y, BRICK_WIDTH, BRICK_HEIGHT, row + 1, 5, "");
                    brick.setColor(new Color(70, 70, 70)); // Серый цвет для стен

                    brick.setBonusType(getRandomBonusType(5));

                    bricks.add(brick);
                }
            }
        }

        // Добавляем "выход" из лабиринта - особый кирпич
        int exitX = 8 * (BRICK_WIDTH + BRICK_HGAP) + BRICK_LEFT_MARGIN;
        int exitY = (BRICK_HEIGHT + BRICK_VGAP) + BRICK_TOP_MARGIN;
        Brick exitBrick = new Brick(exitX, exitY, BRICK_WIDTH, BRICK_HEIGHT, 2, 1, "EXIT");
        exitBrick.setColor(Color.ORANGE);
        exitBrick.setBonusType(BonusType.EXTRA_LIFE); // Особый бонус за выход
        bricks.add(exitBrick);

        return new Level(5, bricks, true, new Color(20, 20, 40)); // Темно-синий фон
    }

    // Возвращает цвет по количеству ударов (как в текущей системе)
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

    public BonusType getRandomBonusType(int levelNumber) {
        List<BonusType> availableBonuses = new ArrayList<>(Arrays.asList(BonusType.values()));

        if (levelNumber < 3) {
            availableBonuses.remove(BonusType.TRAP_SHRINK_PADDLE);
        } else if (levelNumber > 3) {
            if (random.nextDouble() < 0.25) {
                return BonusType.TRAP_SHRINK_PADDLE;
            }
            availableBonuses.remove(BonusType.TRAP_SHRINK_PADDLE);
        }

        if (levelNumber >= 6) {
            if (random.nextDouble() < 0.15) {
                return BonusType.LASER_GUN;
            }
        }

        if (availableBonuses.isEmpty()) {
            return null;
        }
        return availableBonuses.get(random.nextInt(availableBonuses.size()));
    }

    private int getHitsForSymbol(String symbol) {
        return switch (symbol) {
            case BLACK_PAWN, WHITE_PAWN -> 1;
            case BLACK_KNIGHT, WHITE_KNIGHT -> 2; // Конь
            case BLACK_BISHOP, WHITE_BISHOP -> 2; // Слон
            case BLACK_ROOK, WHITE_ROOK -> 3;
            case BLACK_QUEEN, WHITE_QUEEN -> 4;
            case BLACK_KING, WHITE_KING -> 5;
            default -> random.nextInt(2) + 4;
        };
    }
}
