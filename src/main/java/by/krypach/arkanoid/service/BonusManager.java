package by.krypach.arkanoid.service;

import by.krypach.arkanoid.core.GamePanel;
import by.krypach.arkanoid.models.*;
import by.krypach.arkanoid.enums.BonusType;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BonusManager {

    private static final double PULSE_SPEED = 0.05;
    private static final double MAX_PULSE = 1.2;
    private static final double MIN_PULSE = 0.8;

    private final List<Bonus> activeBonuses = new ArrayList<>();
    private final Random random = new Random();
    private static final double DROP_CHANCE = 0.3;
    private final GamePanel gamePanel;

    public BonusManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void spawnFromBrick(Brick brick) {
        if (gamePanel.getCurrentLevel().hasBonuses() &&random.nextDouble() < DROP_CHANCE) {
            BonusType type = determineBonusType(brick.getRow());
            activeBonuses.add(new Bonus(
                    brick.getX() + brick.getWidth() / 2 - Bonus.WIDTH / 2,
                    brick.getY(),
                    type
            ));
        }
    }

    public void update(double deltaTime) {
        for (Bonus bonus : activeBonuses) {
            bonus.update(deltaTime);

            // Управление анимацией пульсации для EXTRA_LIFE
            if (bonus.getType() == BonusType.EXTRA_LIFE) {
                updatePulseAnimation(bonus, deltaTime);
            }
        }
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

    public void timeSlowEffect(double deltaTime){
        if (gamePanel.isTimeSlowActive()) {
            gamePanel.setTimeSlowRemaining((float) (gamePanel.getTimeSlowRemaining() - deltaTime * 1000)); // Уменьшаем на миллисекунды
            if (gamePanel.getTimeSlowRemaining() <= 0) {
                gamePanel.setTimeSlowRemaining(0);
                gamePanel.setTimeSlowActive(false);
                gamePanel.getBalls().forEach(ball -> ball.setSpeedMultiplier(1.0f));
            }
        }
    }

    public List<Bonus> getActiveBonuses() {
        return Collections.unmodifiableList(activeBonuses);
    }

    public void clear() {
        activeBonuses.clear();
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

                balls.forEach(ball -> ball.setSpeedMultiplier(slowFactor));
            }
            case EXTRA_LIFE -> {
                gamePanel.setLives(gamePanel.getLives() + 1);
                Color heartColor = getActiveBonuses().stream()
                        .filter(b -> b.getType() == BonusType.EXTRA_LIFE)
                        .findFirst()
                        .map(Bonus::getHeartColor)
                        .orElse(Color.PINK);

                // Запускаем анимацию через GamePanel
                gamePanel.animateLifeGain();
            }
        }
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

    private void updatePulseAnimation(Bonus bonus, double deltaTime) {
        double newPulse = bonus.getPulseScale() +
                (bonus.isPulseGrowing() ? PULSE_SPEED : -PULSE_SPEED) * deltaTime * 60;

        if (newPulse > MAX_PULSE) {
            bonus.setPulseScale(MAX_PULSE);
            bonus.setPulseGrowing(false);
        } else if (newPulse < MIN_PULSE) {
            bonus.setPulseScale(MIN_PULSE);
            bonus.setPulseGrowing(true);
        } else {
            bonus.setPulseScale(newPulse);
        }
    }
}