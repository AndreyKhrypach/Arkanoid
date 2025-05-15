package by.krypach.arkanoid.service;

import by.krypach.arkanoid.core.GamePanel;
import by.krypach.arkanoid.enums.BonusType;
import by.krypach.arkanoid.models.Ball;
import by.krypach.arkanoid.models.Brick;
import by.krypach.arkanoid.models.LaserBeam;
import by.krypach.arkanoid.models.Paddle;

import java.awt.*;
import java.util.*;
import java.util.List;

import static by.krypach.arkanoid.core.GamePanel.WIDTH;

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

        boolean wasAlive = brick.isAlive();
        boolean wasDestroyed = brick.hit();

        if (wasAlive && wasDestroyed) {
            gamePanel.addScore(gamePanel.calculateScoreForBrick(brick) * brick.getMaxHits());
            BonusType bonusType = brick.getBonusType();
            if (bonusType != null) {
                if (brick.getBonusType().isTrap()) {
                    // Ловушка: уменьшаем платформу на 30%
                    Paddle paddle = gamePanel.getPaddle();
                    paddle.setWidth((int)(paddle.getWidth() * 0.7));
                } else {
                    // Обычные бонусы
                    gamePanel.getBonusManager().spawnFromBrick(brick);
                }
            }
            // Особый случай - выход из лабиринта
            if ("EXIT".equals(brick.getChessSymbol())) {
                gamePanel.addScore(1000); // Бонусные очки за выход
                gamePanel.getCurrentLevel().setLevelCompleted(true); // Помечаем уровень завершенным
            }
        }
    }
    public void checkLaserCollisions() {
        Iterator<LaserBeam> laserIter = gamePanel.getPaddle().getLaserBeams().iterator();
        while (laserIter.hasNext()) {
            LaserBeam laser = laserIter.next();

            // Проверяем только "длинные" лучи (не искры)
            if (laser.getHeight() == GamePanel.HEIGHT) {
                for (Brick brick : gamePanel.getBricks()) {
                    if (brick.isAlive() && laser.getBounds().intersects(brick.getBounds())) {
                        brick.hit();
                        gamePanel.addScore(gamePanel.calculateScoreForBrick(brick));
                        // Создаем эффект попадания
                        createHitEffect(brick);
                        break;
                    }
                }
            } else {
                // Удаляем искры после проверки
                laserIter.remove();
            }
        }
    }

    private void createHitEffect(Brick brick) {
        // Можно добавить эффекты при попадании (например, частицы)
        // Это можно реализовать через систему частиц в GamePanel
    }
}
