package by.krypach.arkanoid.service;

import by.krypach.arkanoid.core.GamePanel;
import by.krypach.arkanoid.models.*;
import by.krypach.arkanoid.enums.BonusType;

import java.awt.*;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class BonusManager {
    private final List<Bonus> activeBonuses = new ArrayList<>();
    private final Random random = new Random();
    private static final double DROP_CHANCE = 0.3;
    private final GamePanel gamePanel;

    public BonusManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void spawnFromBrick(Brick brick) {
        if (random.nextDouble() < DROP_CHANCE) {
            BonusType type = determineBonusType(brick.getRow());
            activeBonuses.add(new Bonus(
                    brick.getX() + brick.getWidth() / 2 - Bonus.WIDTH / 2,
                    brick.getY(),
                    type
            ));
        }
    }

    public void update(double deltaTime) {
        activeBonuses.forEach(bonus -> bonus.update(deltaTime));
        activeBonuses.removeIf(bonus -> bonus.getY() > GamePanel.HEIGHT);
    }

    public void checkCollisions(Paddle paddle, List<Ball> balls) {
        Iterator<Bonus> iterator = activeBonuses.iterator();
        while (iterator.hasNext()) {
            Bonus bonus = iterator.next();
            if (bonus.checkCollision(paddle.getBounds())) {
                applyBonusEffect(bonus.getType(), paddle, balls);
                iterator.remove();
            }
        }
    }

    private void applyBonusEffect(BonusType type, Paddle paddle, List<Ball> balls) {
        switch (type) {
            case PADDLE_EXTEND -> {
                int newWidth = Math.min(paddle.getWidth() + 20, Paddle.MAX_WIDTH);
                paddle.setWidth(newWidth);
            }
            case BALL_SPEED_UP -> {
                double targetMultiplier = Ball.SPEED_BOOST;
                balls.forEach(ball -> {
                    if (ball.getSpeedMultiplier() < targetMultiplier) {
                        ball.setSpeedMultiplier(targetMultiplier);
                        ball.setColor(new Color(255, 165, 0));
                    }
                });
            }
            case EXTRA_BALL -> {
                Ball newBall = new Ball(
                        paddle.getX() + paddle.getWidth() / 2 - Ball.DEFAULT_SIZE / 2,
                        paddle.getY() - Ball.DEFAULT_SIZE,
                        0, -375
                );
                newBall.setSpeedX((random.nextBoolean() ? 1 : -1) * random.nextInt(100));
                balls.add(newBall);
            }
            case TIME_SLOW -> {
                final float slowFactor = 0.4f;
                final int slowDuration = 15000;
                gamePanel.setTimeSlowActive(true);
                gamePanel.setTimeSlowRemaining(slowDuration);
                gamePanel.setCurrentBackground(new Color(70, 70, 255, 50));

                balls.forEach(ball -> ball.setSpeedMultiplier(slowFactor));

                Timer timer = new Timer(slowDuration, (ActionEvent e) -> {
                    balls.forEach(ball -> {
                        if (ball.getSpeedMultiplier() == slowFactor) {
                            ball.setSpeedMultiplier(1.0f);
                        }
                    });
                    gamePanel.setCurrentBackground(Color.BLACK);
                    gamePanel.setTimeSlowActive(false);
                });
                timer.setRepeats(false);
                timer.start();
            }
            case EXTRA_LIFE -> {
                gamePanel.setLives(gamePanel.getLives() + 1);
                new Thread(() -> {
                    for (int i = 0; i < 5; i++) {
                        gamePanel.setCurrentBackground(i % 2 == 0 ? Color.PINK : Color.BLACK);
                        try { Thread.sleep(150); }
                        catch (InterruptedException ignored) {}
                    }
                    gamePanel.setCurrentBackground(Color.BLACK);
                }).start();
            }
        }
    }

    // Остальные методы без изменений
    public List<Bonus> getActiveBonuses() {
        return Collections.unmodifiableList(activeBonuses);
    }

    public void clear() {
        activeBonuses.clear();
    }

    private BonusType determineBonusType(int brickRow) {
        return switch (brickRow) {
            case 1 -> BonusType.PADDLE_EXTEND;
            case 2 -> BonusType.BALL_SPEED_UP;
            case 3 -> BonusType.EXTRA_BALL;
            case 4 -> BonusType.TIME_SLOW;
            case 5 -> BonusType.EXTRA_LIFE;
            default -> throw new IllegalStateException("Unexpected row: " + brickRow);
        };
    }
}