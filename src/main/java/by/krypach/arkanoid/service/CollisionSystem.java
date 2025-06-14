package by.krypach.arkanoid.service;

import by.krypach.arkanoid.core.GamePanel;
import by.krypach.arkanoid.enums.BonusType;
import by.krypach.arkanoid.models.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import static by.krypach.arkanoid.core.GamePanel.WIDTH;
import static by.krypach.arkanoid.game.LevelGenerator.*;

public class CollisionSystem {

    private final GamePanel gamePanel;

    public CollisionSystem(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void checkWallCollisions(List<Ball> balls, Random random) {
        for (Ball ball : balls) {
            if (ball.getX() <= 0 || ball.getX() >= WIDTH - ball.getSize()) {
                ball.reverseX();
                ball.setX(Math.max(1, Math.min(ball.getX(), WIDTH - ball.getSize() - 1)));
            }

            if (ball.getY() <= 0) {
                ball.reverseY();
                ball.setY(1);
                if (Math.abs(ball.getSpeedX()) < 10) {
                    ball.setSpeedX(ball.getSpeedX() + (random.nextBoolean() ? 15 : -15));
                }
            }
        }
    }

    public void checkPaddleCollision(List<Ball> balls, Paddle paddle) {
        for (Ball ball : balls) {
            if (ball.getBounds().intersects(paddle.getBounds()) && !ball.isStuckToPaddle()) {
                handlePaddleCollision(ball, paddle);
            }
        }
    }

    public void handlePaddleCollision(Ball ball, Paddle paddle) {
        double ballCenterX = ball.getX() + ball.getSize() / 2f;
        float paddleCenterX = paddle.getX() + paddle.getWidth() / 2f;
        double relativeIntersect = (ballCenterX - paddleCenterX) / (paddle.getWidth() / 2f);

        float maxBounceAngle = 60f;
        float baseSpeed = 270f * (float) ball.getSpeedMultiplier();
        float speedBoost = 1.8f;

        double currentSpeed = Math.sqrt(ball.getSpeedX() * ball.getSpeedX() + ball.getSpeedY() * ball.getSpeedY());
        double speed = Math.max(baseSpeed, currentSpeed) + Math.abs(paddle.getCurrentSpeed()) * speedBoost;

        double bounceAngle = relativeIntersect * maxBounceAngle;
        ball.setSpeed(
                speed * Math.sin(Math.toRadians(bounceAngle)),
                -speed * Math.cos(Math.toRadians(bounceAngle))
        );
        ball.setY(paddle.getY() - ball.getSize());
    }

    public void handleBrickCollision(Ball ball, Brick brick) {
        Rectangle ballRect = ball.getBounds();
        Rectangle brickRect = brick.getBounds();
        Rectangle intersection = ballRect.intersection(brickRect);

        // Обработка столкновения с боссом (оставляем без изменений)
        if (brick instanceof BossBrick) {
            if (intersection.width > intersection.height) {
                if (ballRect.y < brickRect.y) {
                    ball.setY(brickRect.y - ball.getSize());
                } else {
                    ball.setY(brickRect.y + brickRect.height);
                }
                ball.reverseY();
            } else {
                if (ballRect.x < brickRect.x) {
                    ball.setX(brickRect.x - ball.getSize());
                } else {
                    ball.setX(brickRect.x + brickRect.width);
                }
                ball.reverseX();
            }
            brick.hit();
            return;
        }

       // Простая и надежная проверка направления столкновения
        if (intersection.width > intersection.height) {
            // Вертикальное столкновение
            if (ballRect.y < brickRect.y) {
                ball.setY(brickRect.y - ball.getSize());
            } else {
                ball.setY(brickRect.y + brickRect.height);
            }
            ball.reverseY();
        } else {
            // Горизонтальное столкновение
            if (ballRect.x < brickRect.x) {
                ball.setX(brickRect.x - ball.getSize());
            } else {
                ball.setX(brickRect.x + brickRect.width);
            }
            ball.reverseX();
        }

        // Обработка попадания в кирпич (без изменений)
        boolean wasAlive = brick.isAlive();
        boolean wasDestroyed = brick.hit();

        if (wasAlive && wasDestroyed) {
            gamePanel.addScore(gamePanel.calculateScoreForBrick(brick) * brick.getMaxHits());

            if (brick instanceof PyramidBrick) {
                handlePyramidBrickDestroyed((PyramidBrick)brick);
            }

            BonusType bonusType = brick.getBonusType();
            if (bonusType != null) {
                if (brick.getBonusType().isTrap()) {
                    Paddle paddle = gamePanel.getPaddle();
                    paddle.setWidth((int) (paddle.getWidth() * 0.7));
                } else {
                    gamePanel.getBonusManager().spawnFromBrick(brick);
                }
            }
            if ("EXIT".equals(brick.getChessSymbol())) {
                gamePanel.addScore(1000);
                gamePanel.getCurrentLevel().setLevelCompleted(true);
            }
        }
    }

    public void checkLaserCollisions() {
        Iterator<LaserBeam> laserIter = gamePanel.getPaddle().getLaserBeams().iterator();
        while (laserIter.hasNext()) {
            LaserBeam laser = laserIter.next();

            // Проверка столкновения с боссом (если это уровень с боссом)
            if (gamePanel.getCurrentLevelNumber() == 7) {  // Изменил с 6 на 7, так как босс на 7 уровне
                for (Brick brick : gamePanel.getBricks()) {
                    if (brick instanceof BossBrick boss && boss.isAlive()) {
                        if (new Rectangle(laser.getX(), laser.getY() - laser.getHeight(),
                                laser.getWidth(), laser.getHeight()).intersects(boss.getBounds())) {
                            boss.hit();
                            laser.setHitProcessed();
                            break;
                        }
                    }
                }
            }

            // Проверка столкновения с платформой игрока
            Paddle paddle = gamePanel.getPaddle();
            if (new Rectangle(laser.getX(), laser.getY() - laser.getHeight(),
                    laser.getWidth(), laser.getHeight()).intersects(paddle.getBounds())) {
                gamePanel.loseLife();
                laser.setHitProcessed();
            }

            if (!laser.isDownward()) {
                Brick closestBrick = getClosestBrick(laser);

                if (closestBrick != null) {
                    laser.setHeight(laser.getY() - closestBrick.getY());
                    if (!laser.isHitProcessed()) {
                        closestBrick.hit();
                        gamePanel.addScore(gamePanel.calculateScoreForBrick(closestBrick));
                        createHitEffect(closestBrick);
                        laser.setHitProcessed();
                    }
                } else {
                    laser.setHeight(laser.getY());
                }
            }

            if (!laser.isAlive()) {
                laserIter.remove();
            }
        }
    }

    public void checkBossLaserCollisions() {
        Iterator<LaserBeam> laserIter = gamePanel.getBossLasers().iterator();
        while (laserIter.hasNext()) {
            LaserBeam laser = laserIter.next();

            // Проверка столкновения с платформой игрока
            if (laser.isDownward()) { // Только для лучей, направленных вниз
                Paddle paddle = gamePanel.getPaddle();
                Rectangle laserRect = new Rectangle(laser.getX(), laser.getY(),
                        laser.getWidth(), laser.getHeight());

                if (laserRect.intersects(paddle.getBounds())) {
                    gamePanel.loseLife();
                    laser.setHitProcessed();
                }
            }

            if (!laser.isAlive()) {
                laserIter.remove();
            }
        }
    }

    public void createHitEffect(Brick brick) {
        // Можно добавить эффекты при попадании (например, частицы)
        // Это можно реализовать через систему частиц в GamePanel
    }

    public void checkBallCollisions(List<Ball> balls) {
        for (Ball ball : balls) {
            if (ball.isStuckToPaddle()) continue;

            // Проверяем все кирпичи для каждого мяча
            for (Brick brick : gamePanel.getBricks()) {
                if (brick.isAlive() && ball.getBounds().intersects(brick.getBounds())) {
                    handleBrickCollision(ball, brick);
                    break; // Обрабатываем только одно столкновение за кадр
                }
            }

            // Затем проверяем другие столкновения (стены, платформа)
            checkWallCollisions(Collections.singletonList(ball), new Random());
            checkPaddleCollision(Collections.singletonList(ball), gamePanel.getPaddle());
        }
    }

    private Brick getClosestBrick(LaserBeam laser) {
        Brick closestBrick = null;
        int maxY = 0;

        for (Brick brick : gamePanel.getBricks()) {
            if (brick.isAlive() &&
                    laser.getX() >= brick.getX() &&
                    laser.getX() <= brick.getX() + brick.getWidth()) {

                if (brick.getY() > maxY) {
                    maxY = brick.getY();
                    closestBrick = brick;
                }
            }
        }
        return closestBrick;
    }

    private void handlePyramidBrickDestroyed(PyramidBrick destroyedBrick) {
        int currentLayer = destroyedBrick.getLayer();

        // Проверяем, остались ли еще кирпичи в текущем слое
        boolean layerDestroyed = true;
        for (Brick brick : gamePanel.getBricks()) {
            if (brick instanceof PyramidBrick pyramidBrick) {
                if (pyramidBrick.getLayer() == currentLayer && pyramidBrick.isAlive()) {
                    layerDestroyed = false;
                    break;
                }
            }
        }

        // Если слой полностью разрушен
        if (layerDestroyed) {
            int nextLayer = currentLayer - 1;

            // Удаляем старые неразрушимые кирпичи
            gamePanel.getBricks().removeIf(brick ->
                    brick instanceof IndestructibleBrick &&
                            brick.getChessSymbol().equals("SHIELD"));

            // Если это не последний слой, создаем новые неразрушимые кирпичи
            if (nextLayer >= 1) {
                int centerX = WIDTH / 2;
                // Находим Y-координату самого нижнего кирпича в следующем слое
                int lowestY = findLowestYInLayer(nextLayer);

                // Создаем новый защитный слой ПОД следующим слоем с увеличенным зазором
                createNewIndestructibleLayer(nextLayer, centerX, lowestY + BRICK_HEIGHT + BRICK_VGAP * 2);

                // Активируем кирпичи следующего слоя
                activateNextLayer(nextLayer);
            } else {
                gamePanel.getCurrentLevel().setLevelCompleted(true);
            }
        }
    }

    private int findLowestYInLayer(int layer) {
        int lowestY = 0;
        for (Brick brick : gamePanel.getBricks()) {
            if (brick instanceof PyramidBrick &&
                    ((PyramidBrick)brick).getLayer() == layer) {
                if (brick.getY() > lowestY) {
                    lowestY = brick.getY();
                }
            }
        }
        return lowestY;
    }

    private void createNewIndestructibleLayer(int layer, int centerX, int yPos) {
        int bricksInRow = (5 - layer) + 1; // Для слоя 4 - 2 кирпича, для 3 - 3 и т.д.
        int totalWidth = bricksInRow * BRICK_WIDTH;
        int startX = centerX - totalWidth / 2;

        for (int i = 0; i < bricksInRow; i++) {
            int x = startX + i * BRICK_WIDTH;
            IndestructibleBrick brick = new IndestructibleBrick(x, yPos,
                    BRICK_WIDTH, BRICK_HEIGHT, 0, "SHIELD");
            brick.setColor(new Color(100, 100, 100));
            gamePanel.getBricks().add(brick);
        }
    }

    private void activateNextLayer(int nextLayer) {
        boolean anyActivated = false;
        for (Brick brick : gamePanel.getBricks()) {
            if (brick instanceof PyramidBrick pyramidBrick) {
                if (pyramidBrick.getLayer() == nextLayer && !pyramidBrick.isActive()) {
                    pyramidBrick.activate(nextLayer);
                    anyActivated = true;
                }
            }
        }

        if (!anyActivated) {
            gamePanel.getCurrentLevel().setLevelCompleted(true);
        }
    }
}
