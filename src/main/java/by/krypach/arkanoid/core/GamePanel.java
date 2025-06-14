package by.krypach.arkanoid.core;

import by.krypach.arkanoid.game.Level;
import by.krypach.arkanoid.game.LevelGenerator;
import by.krypach.arkanoid.models.*;
import by.krypach.arkanoid.service.BonusManager;
import by.krypach.arkanoid.service.CollisionSystem;
import by.krypach.arkanoid.service.RenderSystem;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GamePanel extends JPanel implements KeyListener {
    // Game constants
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    private static final int EXTRA_LIFE_SCORE = 100;
    private static final int INITIAL_LIVES = 3;
    private static final int PADDLE_INITIAL_WIDTH = 100;

    // Game state
    private int lives = INITIAL_LIVES;
    private int score = 0;
    private int deathAnimationCounter = 0;
    private boolean isRunning = true;
    private boolean isPaused = false;
    private boolean timeSlowActive = false;
    private double timeSlowRemaining = 0;
    private Level currentLevel;
    private boolean levelTransitionInProgress = false;
    private boolean lifeAnimationActive = false;
    private Color lifeAnimationColor = Color.BLACK;
    private boolean laserActive = false;
    private boolean controlsInverted = false;
    private long controlsInvertedEndTime = 0;

    // models
    private final Paddle paddle;
    private final BonusManager bonusManager;
    private final RenderSystem renderSystem;
    private final CollisionSystem collisionSystem;
    private final List<Brick> bricks = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();
    private final List<LaserBeam> bossLasers = new ArrayList<>();

    // Input
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    //level
    private final LevelGenerator levelGenerator;

    // Game loop
    private long lastTime = System.nanoTime();
    private final int initialPaddleWidth;
    private final Random random = new Random();

    public GamePanel() {
        this.bonusManager = new BonusManager(this); // 20% шанс выпадения
        this.levelGenerator = new LevelGenerator(this);
        this.balls.add(new Ball(WIDTH / 2, HEIGHT / 2, 0, 0));
        this.paddle = new Paddle(
                WIDTH / 2 - PADDLE_INITIAL_WIDTH / 2,
                HEIGHT - 50,
                PADDLE_INITIAL_WIDTH,
                20,
                this
        );
        this.initialPaddleWidth = paddle.getWidth();

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setupInput();
        loadLevel(1);
        this.renderSystem = new RenderSystem(this);
        this.collisionSystem = new CollisionSystem(this);
        startGameLoop();
    }

    public void update(double deltaTime) {
        if (!isRunning || isPaused) return;

        if (controlsInverted && System.currentTimeMillis() > controlsInvertedEndTime) {
            controlsInverted = false;
        }

        if (!bossLasers.isEmpty()) {
            collisionSystem.checkBossLaserCollisions();
        }

        if (currentLevel.hasBonuses()) {
            bonusManager.timeSlowEffect(deltaTime);
            bonusManager.update(deltaTime);
            bonusManager.checkCollisions(paddle, balls);
        }

        // Проверяем коллизии лазера только если есть активные лучи
        if (!paddle.getLaserBeams().isEmpty()) {
            collisionSystem.checkLaserCollisions();
        }

        checkCollisions();
        checkBallsLoss();
        updatePaddle(deltaTime);
        updateBalls(deltaTime);
        checkWinCondition();
    }

    // Input handling
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_P -> isPaused = !isPaused;
            case KeyEvent.VK_SPACE -> {
                if (!balls.isEmpty()) {
                    for (Ball ball : balls) {
                        if (ball.isStuckToPaddle()) {
                            ball.setStuckToPaddle(false);
                            ball.setSpeed(0, -375);
                        }
                    }
                }
                if (laserActive) {
                    paddle.fireLaser();
                }
            }
            case KeyEvent.VK_LEFT -> {
                if (controlsInverted) rightPressed = true;
                else leftPressed = true;
            }
            case KeyEvent.VK_RIGHT -> {
                if (controlsInverted) leftPressed = true;
                else rightPressed = true;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> {
                if (controlsInverted) rightPressed = false;
                else leftPressed = false;
            }
            case KeyEvent.VK_RIGHT -> {
                if (controlsInverted) leftPressed = false;
                else rightPressed = false;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void animateLifeGain() {
        lifeAnimationActive = true;
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                lifeAnimationColor = (i % 2 == 0 ? Color.GREEN : Color.BLACK);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }
            }
            lifeAnimationColor = Color.BLACK;
            lifeAnimationActive = false;
        }).start();
    }

    public void addScore(int points) {
        score += points;
        checkExtraLife(points);
    }

    public int calculateScoreForBrick(Brick brick) {
        return 6 - brick.getRow(); // Обратный порядок: 1 строка -> 5 очков, 5 строка -> 1 очко
    }

    public void checkWinCondition() {
        if (levelTransitionInProgress || !isRunning) return;

        if (currentLevel.isCompleted() || currentLevel.isLevelCompleted()) {
            showLevelComplete();
        }
    }

    public void invertControls(long durationMillis) {
        this.controlsInverted = true;
        this.controlsInvertedEndTime = System.currentTimeMillis() + durationMillis;

        // Визуальная индикация эффекта
        animateControlInversion();
    }

    public void animateTeleportEffect() {
        new Thread(() -> {
            for (int i = 0; i < 3; i++) {
                setBackground(Color.CYAN);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                setBackground(getCurrentLevel().getBackgroundColor());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    public void loseLife() {
        lives--;
        deathAnimationCounter = 10;

        if (lives <= 0) {
            isRunning = false;
            new Timer(500, e -> repaint()).start();
        } else {
            resetAfterDeath();
        }
    }

    public void setTimeSlowActive(boolean active) {
        this.timeSlowActive = active;
    }

    public void setTimeSlowRemaining(float remaining) {
        this.timeSlowRemaining = remaining;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public Paddle getPaddle() {
        return paddle;
    }

    public List<Ball> getBalls() {
        return balls;
    }

    public List<Brick> getBricks() {
        return bricks;
    }

    public BonusManager getBonusManager() {
        return bonusManager;
    }

    public CollisionSystem getCollisionSystem() {
        return collisionSystem;
    }

    public int getCurrentLevelNumber() {
        return currentLevel.getLevelNumber();
    }

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public boolean isLifeAnimationActive() {
        return lifeAnimationActive;
    }

    public Color getLifeAnimationColor() {
        return lifeAnimationColor;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public int getDeathAnimationCounter() {
        return deathAnimationCounter;
    }

    public void setDeathAnimationCounter(int deathAnimationCounter) {
        this.deathAnimationCounter = deathAnimationCounter;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isTimeSlowActive() {
        return timeSlowActive;
    }

    public double getTimeSlowRemaining() {
        return timeSlowRemaining;
    }

    public void setLaserActive(boolean laserActive) {
        this.laserActive = laserActive;
    }

    public boolean isControlsInverted() {
        return controlsInverted;
    }

    public long getControlsInvertedEndTime() {
        return controlsInvertedEndTime;
    }

    public List<LaserBeam> getBossLasers() {
        return bossLasers;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderSystem.render(g);
        for (LaserBeam laser : paddle.getLaserBeams()) {
            laser.draw(g);
        }
        for (LaserBeam laser : bossLasers) { // Отрисовываем лазеры босса
            laser.draw(g);
        }
    }

    private void setupInput() {
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();
    }

    private void startGameLoop() {
        new Timer(16, e -> {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;
            update(deltaTime);
            repaint();
        }).start();
    }

    private void loadLevel(int levelNumber) {
        bonusManager.clear();
        levelTransitionInProgress = false;
        paddle.getLaserBeams().clear();
        bossLasers.clear();

        switch (levelNumber) {
            case 1:
                this.currentLevel = levelGenerator.generateFirstLevel(5, 10);
                bonusManager.setCurrentDropChance(0);
                break;
            case 2:
                this.currentLevel = levelGenerator.generateLevel(2, 5, 10, random);
                bonusManager.setCurrentDropChance(0.3);
                break;
            case 3:
                this.currentLevel = levelGenerator.generateLevel(3, 5, 10, random);
                bonusManager.setCurrentDropChance(0.5);
                break;
            case 4:
                this.currentLevel = levelGenerator.generateChessLevel();
                bonusManager.setCurrentDropChance(0.4);  // Больше бонусов для сложного уровня
                break;
            case 5:  // Новый уровень-лабиринт
                this.currentLevel = levelGenerator.generateMazeLevel();
                bonusManager.setCurrentDropChance(0.6);  // Больше бонусов в лабиринте
                break;
            case 6:
                this.currentLevel = levelGenerator.generateLevel(6, 5, 10, random);
                bonusManager.setCurrentDropChance(0.5);
                break;
            case 7:
                this.currentLevel = levelGenerator.generatePyramidLevel();
                bonusManager.setCurrentDropChance(0.7); // Много бонусов
                break;
            case 8:
                this.currentLevel = levelGenerator.generateBossLevel();
                bonusManager.setCurrentDropChance(0.5);
                break;
            default:
                this.currentLevel = levelGenerator.generateLevel(levelNumber, 5, 10, random);
                bonusManager.setCurrentDropChance(0.3);
        }

        currentLevel.setLevelCompleted(false);
        this.bricks.clear();
        this.bricks.addAll(currentLevel.getBricks());
        setBackground(currentLevel.getBackgroundColor());
        resetAfterLevelStart();
    }

    private void resetAfterLevelStart() {
        balls.clear();
        Ball newBall = createNewBall();
        newBall.setStuckToPaddle(true); // Всегда прилипает при новом уровне
        balls.add(newBall);

        paddle.setWidth(initialPaddleWidth);
        paddle.setPreciseX(WIDTH / 2.0 - initialPaddleWidth / 2.0);
        paddle.deactivateLaser(); // Добавляем деактивацию лазера
        laserActive = false; // Сбрасываем флаг активности лазера
    }

    private Ball createNewBall() {
        Ball ball = new Ball(
                paddle.getX() + paddle.getWidth() / 2 - Ball.DEFAULT_SIZE / 2,
                paddle.getY() - Ball.DEFAULT_SIZE,
                0, 0
        );
        ball.setStuckToPaddle(true);
        return ball;
    }

    private void checkBallsLoss() {
        if (levelTransitionInProgress) return;

        List<Ball> ballsCopy = new ArrayList<>(balls);
        List<Brick> bricksCopy = new ArrayList<>(bricks);

        for (Ball ball : ballsCopy) {
            if (ball.getY() > HEIGHT) {
                balls.remove(ball); // Safe because we're not iterating over the original
                if (balls.isEmpty()) {
                    loseLife();
                }
                continue;
            }

            for (Brick brick : bricksCopy) {
                if (brick.isAlive() && ball.getBounds().intersects(brick.getBounds())) {
                    collisionSystem.handleBrickCollision(ball, brick);
                    break;
                }
            }
        }
    }

    private void updatePaddle(double deltaTime) {
        float direction = 0;
        if (leftPressed) direction -= 1;
        if (rightPressed) direction += 1;

        paddle.move(direction);
        paddle.update(deltaTime);
    }

    private void updateBalls(double deltaTime) {
        for (Ball ball : balls) {
            if (ball.isStuckToPaddle()) {
                ball.setX(paddle.getX() + paddle.getWidth() / 2.0 - ball.getSize() / 2.0);
                ball.setY(paddle.getY() - ball.getSize());
            } else {
                ball.move(deltaTime);
            }
        }
    }

    private void checkCollisions() {
        collisionSystem.checkWallCollisions(balls, random);
        collisionSystem.checkPaddleCollision(balls, paddle);
        collisionSystem.checkBallCollisions(balls);
    }

    private void showLevelComplete() {
        if (levelTransitionInProgress) return;

        currentLevel.setLevelCompleted(true);
        isPaused = true;
        repaint();

        paddle.getLaserBeams().clear();
        laserActive = false;

        Timer showMessageTimer = new Timer(1000, e -> {
            levelTransitionInProgress = true;
            balls.clear();

            if (currentLevel.getLevelNumber() < 10) {
                int nextLevel = getNextLevelNumber(currentLevel.getLevelNumber());
                loadLevel(nextLevel);

                if (nextLevel == 5) {
                    renderSystem.drawCenteredText(getGraphics(), "Вы нашли выход из лабиринта!", 30, HEIGHT / 2 - 30);
                }
            } else {
                gameComplete();
                renderSystem.drawCenteredText(getGraphics(), "ПОБЕДА! Финальный счет: " + score, 40, HEIGHT / 2);
            }
            isPaused = false;
            levelTransitionInProgress = false;
            ((Timer) e.getSource()).stop();
        });
        showMessageTimer.setRepeats(false);
        showMessageTimer.start();
    }

    private int getNextLevelNumber(int currentLevel) {
        // Специальная последовательность уровней
        return switch (currentLevel) {
            case 3 -> 4;  // После 3 обычного уровня идет 4 (шахматы)
            case 4 -> 5;  // После шахмат идет лабиринт
            case 5 -> 6;  // После лабиринта идет 6 обычный
            case 6 -> 7;  // После 6 обычного идет босс
            case 7 -> 8;  // После босса продолжаем обычные уровни
            default -> currentLevel + 1;  // Для остальных случаев просто +1
        };
    }

    private void gameComplete() {
        isRunning = false;
        currentLevel.setLevelCompleted(true);
        balls.clear();
        bonusManager.clear();
        paddle.clearLasers();

        renderSystem.drawCenteredText(getGraphics(), "ИГРА ЗАВЕРШЕНА! Финальный счет: " + score, 30, HEIGHT / 2);
        repaint();
    }

    private void checkExtraLife(int points) {
        int oldScore = score - points;
        if (oldScore / EXTRA_LIFE_SCORE < score / EXTRA_LIFE_SCORE) {
            lives++;
            animateLifeGain();
        }
    }

    private void resetAfterDeath() {
        balls.clear();
        Ball ball = new Ball(
                paddle.getX() + paddle.getWidth() / 2 - Ball.DEFAULT_SIZE / 2,
                paddle.getY() - Ball.DEFAULT_SIZE,
                0, 0
        );
        ball.setStuckToPaddle(true);
        balls.add(ball);

        paddle.setWidth(initialPaddleWidth);
        paddle.setPreciseX(WIDTH / 2.0 - initialPaddleWidth / 2.0);
        paddle.setCurrentSpeed(0);
        paddle.clearLasers();
        laserActive = false;
    }

    private void animateControlInversion() {
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                setBackground(i % 2 == 0 ? Color.RED : Color.BLACK);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }
            }
            setBackground(getCurrentLevel().getBackgroundColor());
        }).start();
    }
}
