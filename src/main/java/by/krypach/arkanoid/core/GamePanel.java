package by.krypach.arkanoid.core;

import by.krypach.arkanoid.game.Level;
import by.krypach.arkanoid.game.LevelGenerator;
import by.krypach.arkanoid.models.Ball;
import by.krypach.arkanoid.models.Brick;
import by.krypach.arkanoid.models.Paddle;
import by.krypach.arkanoid.service.BonusManager;
import by.krypach.arkanoid.service.CollisionSystem;
import by.krypach.arkanoid.service.RenderSystem;

import java.util.ArrayList;
import java.util.Iterator;
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
    private boolean levelCompleted = false;
    private boolean levelTransitionInProgress = false;
    private boolean lifeAnimationActive = false;
    private Color lifeAnimationColor = Color.BLACK;

    // models
    private final Paddle paddle;
    private final BonusManager bonusManager;
    private final RenderSystem renderSystem;
    private final CollisionSystem collisionSystem;
    private final List<Brick> bricks = new ArrayList<>();
    private final List<Ball> balls = new ArrayList<>();

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
        this.levelGenerator = new LevelGenerator();
        this.balls.add(new Ball(WIDTH / 2, HEIGHT / 2, 0, 0));
        this.paddle = new Paddle(
                WIDTH / 2 - PADDLE_INITIAL_WIDTH / 2,
                HEIGHT - 50,
                PADDLE_INITIAL_WIDTH,
                20
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

        if (currentLevel.hasBonuses()) {
            bonusManager.timeSlowEffect(deltaTime);
            bonusManager.update(deltaTime);
            bonusManager.checkCollisions(paddle, balls);
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
            }
            case KeyEvent.VK_LEFT -> leftPressed = true;
            case KeyEvent.VK_RIGHT -> rightPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> leftPressed = false;
            case KeyEvent.VK_RIGHT -> rightPressed = false;
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

    public void setTimeSlowActive(boolean active) {
        this.timeSlowActive = active;
    }

    public void setTimeSlowRemaining(float remaining) {
        this.timeSlowRemaining = remaining;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }
    public Paddle getPaddle() { return paddle; }
    public List<Ball> getBalls() { return balls; }
    public List<Brick> getBricks() { return bricks; }
    public BonusManager getBonusManager() { return bonusManager; }
    public int getCurrentLevelNumber() { return currentLevel.getLevelNumber(); }
    public int getLives() { return lives; }
    public int getScore() { return score; }

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

    public boolean isLevelCompleted() {
        return levelCompleted;
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderSystem.render(g);
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
        levelCompleted = false;
        levelTransitionInProgress = false;

        switch(levelNumber) {
            case 1:
                this.currentLevel = levelGenerator.generateFirstLevel(5, 10);
                bonusManager.setCurrentDropChance(0);
                break;
            case 2:
                this.currentLevel = levelGenerator.generateLevel(2, 5, 10, random);
                bonusManager.setCurrentDropChance(0.3);
                break;
            case 3:
                this.currentLevel = levelGenerator.generateLevel3();
                bonusManager.setCurrentDropChance(0.5);
                break;
            default:
                this.currentLevel = levelGenerator.generateLevel(levelNumber, 5, 10, random);
                bonusManager.setCurrentDropChance(0.3);
        }

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

        Iterator<Ball> ballIterator = ballsCopy.iterator();
        while (ballIterator.hasNext()) {
            Ball ball = ballIterator.next();

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
    }

    private void checkWinCondition() {
        if (levelCompleted || levelTransitionInProgress || !isRunning) return;

        if (currentLevel.isCompleted()) {
            levelCompleted = true;
            showLevelComplete();
        }
    }

    private void showLevelComplete() {
        if (levelTransitionInProgress) return;
        levelTransitionInProgress = true;

        isPaused = true;
        Timer transitionTimer = new Timer(2000, e -> {
            // Очищаем все мячи перед загрузкой нового уровня
            balls.clear();

            if (currentLevel.getLevelNumber() < 10) {
                currentLevel.setLevelNumber(currentLevel.getLevelNumber() + 1);
                loadLevel(currentLevel.getLevelNumber());
            } else {
                gameComplete();
                renderSystem.drawCenteredText(getGraphics(), "ПОБЕДА! Финальный счет: " + score, 40, HEIGHT/2);
            }
            isPaused = false;
            levelTransitionInProgress = false;
            ((Timer)e.getSource()).stop();
        });
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }

    private void gameComplete() {
        isRunning = false;
        levelCompleted = true;
        balls.clear();
        bonusManager.clear();

        renderSystem.drawCenteredText(getGraphics(), "ИГРА ЗАВЕРШЕНА! Финальный счет: " + score, 30, HEIGHT/2);
        repaint();
    }

    private void checkExtraLife(int points) {
        int oldScore = score - points;
        if (oldScore / EXTRA_LIFE_SCORE < score / EXTRA_LIFE_SCORE) {
            lives++;
            animateLifeGain();
        }
    }

    private void loseLife() {
        lives--;
        deathAnimationCounter = 10;

        if (lives <= 0) {
            isRunning = false;
            new Timer(500, e -> repaint()).start();
        } else {
            resetAfterDeath();
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
    }
}
