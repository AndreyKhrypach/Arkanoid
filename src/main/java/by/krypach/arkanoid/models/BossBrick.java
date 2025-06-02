package by.krypach.arkanoid.models;

import by.krypach.arkanoid.core.GamePanel;
import by.krypach.arkanoid.enums.BonusType;

import java.awt.*;
import java.util.Random;

import static by.krypach.arkanoid.core.GamePanel.WIDTH;
import static by.krypach.arkanoid.game.LevelGenerator.BRICK_LEFT_MARGIN;

public class BossBrick extends Brick {
    public static final int BOSS_WIDTH = 2 * 70; // Ширина 2 кирпича (140px)
    public static final int BOSS_HEIGHT = 4 * 20; // Высота 4 кирпичей (80px)
    private static final int MAX_HITS = 25; // Живучесть 25 ударов
    private static final int BONUS_DROP_CHANCE = 30; // 30% шанс выпадения бонуса
    private static final int LASER_WIDTH = 5; // Ширина лазера босса

    private final GamePanel gamePanel;
    private final Random random = new Random();

    public BossBrick(int x, int y, GamePanel gamePanel) {
        super(x, y, BOSS_WIDTH, BOSS_HEIGHT, 0, MAX_HITS, "BOSS");
        this.gamePanel = gamePanel;
        this.setColor(Color.WHITE);
    }

    @Override
    public void draw(Graphics g) {
        if (!isDestroyed()) {
            // Рисуем основное тело босса
            g.setColor(getColor());
            g.fillRect(getX(), getY(), getWidth(), getHeight());

            // Рисуем детали босса
            g.setColor(Color.RED);
            // Глаза
            g.fillOval(getX() + 20, getY() + 20, 30, 30);
            g.fillOval(getX() + getWidth() - 50, getY() + 20, 30, 30);

            // Рот
            g.fillRect(getX() + getWidth()/2 - 20, getY() + getHeight() - 30, 40, 10);

            // Рисуем индикатор здоровья
            double healthPercent = (double)(getMaxHits() - getCurrentHits()) / getMaxHits();
            int healthWidth = (int)(getWidth() * healthPercent);

            g.setColor(Color.RED);
            g.fillRect(getX(), getY() - 10, getWidth(), 5);
            g.setColor(Color.GREEN);
            g.fillRect(getX(), getY() - 10, healthWidth, 5);

            // Белая рамка
            g.setColor(Color.WHITE);
            g.drawRect(getX(), getY(), getWidth(), getHeight());
        }
    }

    @Override
    public boolean hit() {
        currentHits++;
        updateColor();

        // Проверяем, нужно ли активировать эффекты (каждые 5 попаданий)
        if (currentHits % 5 == 0) {
            // Сначала стреляем лазером прямо вниз
            shootVerticalLaser();

            // Затем инвертируем управление и телепортируемся
            gamePanel.invertControls(15000); // 15 секунд эффекта
            teleport(); // Телепортируем босса
        }

        // 30% шанс выпадения бонуса при попадании
        if (random.nextInt(100) < BONUS_DROP_CHANCE) {
            dropRandomBonus();
        }

        if (currentHits >= maxHits) {
            isDestroyed = true;
            return true;
        }
        return false;
    }

    private void dropRandomBonus() {
        // Исключаем ловушки и лазер для босса
        BonusType[] possibleBonuses = {
                BonusType.PADDLE_EXTEND,
                BonusType.BALL_SPEED_UP,
                BonusType.EXTRA_BALL,
                BonusType.TIME_SLOW,
                BonusType.EXTRA_LIFE
        };

        BonusType type = possibleBonuses[random.nextInt(possibleBonuses.length)];

        // Создаем временный кирпич для генерации бонуса
        Brick tempBrick = new Brick(getX(), getY(), getWidth(), getHeight(), 0, 1, "");
        tempBrick.setBonusType(type);
        gamePanel.getBonusManager().spawnFromBrick(tempBrick);
    }

    private void teleport() {
        int newX;

        // Если босс в центре (начальная позиция), выбираем случайную сторону
        if (Math.abs(this.x - (WIDTH/2 - BOSS_WIDTH/2)) < 10) {
            boolean teleportToLeft = random.nextBoolean();
            if (teleportToLeft) {
                newX = BRICK_LEFT_MARGIN;
            } else {
                newX = WIDTH - BRICK_LEFT_MARGIN - BOSS_WIDTH;
            }
        }
        // Иначе телепортируем на противоположную сторону
        else {
            boolean isCurrentlyLeft = this.x <= WIDTH / 2;
            if (isCurrentlyLeft) {
                newX = WIDTH - BRICK_LEFT_MARGIN - BOSS_WIDTH;
            } else {
                newX = BRICK_LEFT_MARGIN;
            }
        }

        this.x = newX;
        gamePanel.animateTeleportEffect();
    }

    private void shootVerticalLaser() {
        int laserX = getX() + getWidth()/2 - LASER_WIDTH/2;
        int laserY = getY() + getHeight();

        LaserBeam laser = new LaserBeam(
                laserX,
                laserY,
                LASER_WIDTH,
                GamePanel.HEIGHT - laserY,
                Color.RED,
                true // isDownward = true
        );

        gamePanel.getBossLasers().add(laser);
    }
}
