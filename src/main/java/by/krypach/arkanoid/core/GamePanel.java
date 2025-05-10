package by.krypach.arkanoid.core;

import by.krypach.arkanoid.game.Level;
import by.krypach.arkanoid.game.LevelGenerator;
import by.krypach.arkanoid.models.Ball;
import by.krypach.arkanoid.models.Brick;
import by.krypach.arkanoid.models.Paddle;
import by.krypach.arkanoid.service.BonusManager;
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
    private static final int PADDLE_MAX_WIDTH = 200;
    private static final int BRICK_ROWS = 5;
    private static final int BRICK_COLS = 10;
    private static final int BRICK_WIDTH = 70;
    private static final int BRICK_HEIGHT = 20;
    private static final int BRICK_TOP_MARGIN = 50;
    private static final int BRICK_LEFT_MARGIN = 10;
    private static final int BRICK_HGAP = 10;
    private static final int BRICK_VGAP = 10;

    // Game state
    private int lives = INITIAL_LIVES;
    private int score = 0;
    private int deathAnimationCounter = 0;
    private boolean isRunning = true;
    private boolean isPaused = false;
    private boolean timeSlowActive = false;
    private float timeSlowRemaining = 0;
    private Color currentBackground = Color.BLACK;
    private int currentLevelNumber = 1;
    private Level currentLevel;
    private boolean levelCompleted = false;
    private boolean levelTransitionInProgress = false;

    // models
    private final Paddle paddle;
    private final BonusManager bonusManager;
    private final RenderSystem renderSystem;
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
        loadLevel(currentLevelNumber);
        this.renderSystem = new RenderSystem(this);

        startGameLoop();
    }

    public void update(double deltaTime) {
        if (!isRunning || isPaused) return;

        checkCollisions();

        bonusManager.update(deltaTime);
        bonusManager.checkCollisions(paddle, balls);
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

    public void setTimeSlowActive(boolean active) {
        this.timeSlowActive = active;
    }

    public void setTimeSlowRemaining(float remaining) {
        this.timeSlowRemaining = remaining;
    }

    public void setCurrentBackground(Color color) {
        this.currentBackground = color;
        setBackground(color);
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void handleExtraLife() {
        lives++;
        animateLifeGain();
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

    private void initBricks() {
        for (int row = 0; row < BRICK_ROWS; row++) {
            for (int col = 0; col < BRICK_COLS; col++) {
                int x = col * (BRICK_WIDTH + BRICK_HGAP) + BRICK_LEFT_MARGIN;
                int y = row * (BRICK_HEIGHT + BRICK_VGAP) + BRICK_TOP_MARGIN;
                int hitsRequired = determineHitsRequired(row, random);

                bricks.add(new Brick(
                        x, y,
                        BRICK_WIDTH, BRICK_HEIGHT,
                        BRICK_ROWS - row, // row number (1-5)
                        hitsRequired
                ));
            }
        }
    }

    private void loadLevel(int levelNumber) {
        bonusManager.clear();
        levelCompleted = false;
        levelTransitionInProgress = false;

        boolean wasStuck = !balls.isEmpty() && balls.get(0).isStuckToPaddle();
        double speedX = balls.isEmpty() ? 0 : balls.get(0).getSpeedX();
        double speedY = balls.isEmpty() ? 0 : balls.get(0).getSpeedY();

        this.currentLevel = levelGenerator.generateLevel(
                levelNumber,
                5, // rows
                10, // cols
                random
        );

        this.bricks.clear();
        this.bricks.addAll(currentLevel.getBricks()); // Remove initBricks() call

        resetAfterLevelStart(wasStuck, speedX, speedY);
    }

    private void resetAfterLevelStart(boolean keepStuck, double speedX, double speedY) {
        balls.clear();
        Ball newBall = createNewBall();
        newBall.setStuckToPaddle(keepStuck);
        if (!keepStuck) {
            newBall.setSpeed(speedX, speedY);
        }
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
                    handleBrickCollision(ball, brick);
                    if (brick.hit()) {
                        bonusManager.spawnFromBrick(brick);
                        addScore(brick.getRow() * brick.getMaxHits());
                    }
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
        checkWallCollisions();
        checkPaddleCollision();
    }

    private void checkWallCollisions() {
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

    private void checkPaddleCollision() {
        for (Ball ball : balls) {
            if (ball.getBounds().intersects(paddle.getBounds()) && !ball.isStuckToPaddle()) {
                handlePaddleCollision(ball);
            }
        }
    }

    private void handlePaddleCollision(Ball ball) {
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

    private void checkBrickCollisions() {
        for (Brick brick : bricks) {
            if (brick.isAlive() /* проверка коллизии */) {
                if (brick.hit()) {
                    bonusManager.spawnFromBrick(brick); // Генерация бонуса
                }
            }
        }
    }

    private void handleBrickCollision(Ball ball, Brick brick) {
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
    }

    private void checkWinCondition() {
        if (levelCompleted || levelTransitionInProgress || !isRunning) return;

        boolean allDestroyed = true;
        for (Brick brick : bricks) {
            if (brick.isAlive()) {
                allDestroyed = false;
                break;
            }
        }

        if (allDestroyed) {
            levelCompleted = true;
            showLevelComplete();
        }
    }

    private void showLevelComplete() {
        if (levelTransitionInProgress) return;
        levelTransitionInProgress = true;

        isPaused = true;
        Timer transitionTimer = new Timer(2000, e -> {
            if (currentLevelNumber < 10) {
                currentLevelNumber++;
                loadLevel(currentLevelNumber);
            } else {
                gameComplete();
                drawCenteredText(getGraphics(), "ПОБЕДА! Финальный счет: " + score, 40, HEIGHT/2);
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

        drawCenteredText(getGraphics(), "ИГРА ЗАВЕРШЕНА! Финальный счет: " + score, 30, HEIGHT/2);
        repaint();
    }

    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Уровень: " + currentLevelNumber, 300, 30);
        g.drawString("Жизни: " + lives, 20, 30);
        String scoreText = "Очки: " + score;
        int scoreWidth = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, WIDTH - scoreWidth - 20, 30);
    }

    private void drawGameState(Graphics g) {
        if (isPaused && levelCompleted) {
            drawCenteredText(g, "Уровень " + (currentLevelNumber-1) + " пройден!", 40, HEIGHT/2 - 50);
            drawCenteredText(g, "Переход на уровень " + currentLevelNumber, 30, HEIGHT/2 + 20);
        }

        if (deathAnimationCounter > 0) {
            g.setColor(new Color(255, 0, 0, 70));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            deathAnimationCounter--;
        }

        if (isPaused) {
            drawCenteredText(g, "PAUSED", 40, 300);
        }

        if (timeSlowActive) {
            // Рисуем полосу прогресса вверху экрана
            int progressWidth = (int)(WIDTH * (timeSlowRemaining / 15000f));
            g.setColor(new Color(100, 100, 255, 200));
            g.fillRect(0, 5, progressWidth, 5);
        }

        if (!isRunning) {
            if (levelCompleted && currentLevelNumber >= 10) {
                drawCenteredText(g, "ИГРА ЗАВЕРШЕНА!", 40, HEIGHT/2 - 30);
                drawCenteredText(g, "Финальный счет: " + score, 30, HEIGHT/2 + 20);
            } else if (lives <= 0) {
                drawGameOver(g);
            }
        }
    }

    private void drawCenteredText(Graphics g, String text, int fontSize, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, fontSize));
        int width = g.getFontMetrics().stringWidth(text);
        g.drawString(text, WIDTH / 2 - width / 2, y);
    }

    private void drawGameOver(Graphics g) {
        drawCenteredText(g, "GAME OVER", 40, HEIGHT / 2);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String scoreText = "Ваш счёт: " + score;
        int width = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, WIDTH / 2 - width / 2, HEIGHT / 2 + 40);
    }

    private void drawModels(Graphics g) {
        if (isRunning) {
            bonusManager.getActiveBonuses().forEach(bonus -> bonus.draw(g));
            balls.forEach(ball -> ball.draw(g)); // Рисуем все мячи
            paddle.draw(g);
            bricks.forEach(brick -> brick.draw(g));
        }
    }

    private void drawBorders(Graphics g) {
        g.setColor(Color.YELLOW);
        g.drawLine(0, 0, WIDTH, 0);
        g.drawLine(0, HEIGHT - 1, WIDTH, HEIGHT - 1);
        g.drawLine(0, 0, 0, HEIGHT);
        g.drawLine(WIDTH - 1, 0, WIDTH - 1, HEIGHT);
    }

    private void addScore(int points) {
        score += points;
        checkExtraLife(points);
    }

    private void checkExtraLife(int points) {
        int oldScore = score - points;
        if (oldScore / EXTRA_LIFE_SCORE < score / EXTRA_LIFE_SCORE) {
            lives++;
            animateLifeGain();
        }
    }

    private void animateLifeGain() {
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                setBackground(i % 2 == 0 ? Color.GREEN : Color.BLACK);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }
            }
            setBackground(Color.BLACK);
        }).start();
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

    private int determineHitsRequired(int row, Random random) {
        double chance = random.nextDouble();
        return switch (row) {
            case 0 -> chance < 0.6 ? 1 : chance < 0.85 ? 2 : chance < 0.95 ? 3 : 4;
            case 1 -> chance < 0.4 ? 1 : chance < 0.7 ? 2 : chance < 0.9 ? 3 : 4;
            case 2 -> chance < 0.2 ? 1 : chance < 0.5 ? 2 : chance < 0.8 ? 3 : chance < 0.95 ? 4 : 5;
            case 3 -> chance < 0.1 ? 1 : chance < 0.3 ? 2 : chance < 0.6 ? 3 : chance < 0.85 ? 4 : 5;
            case 4 -> chance < 0.05 ? 1 : chance < 0.15 ? 2 : chance < 0.35 ? 3 : chance < 0.65 ? 4 : 5;
            default -> 1;
        };
    }

    public Paddle getPaddle() { return paddle; }
    public List<Ball> getBalls() { return balls; }
    public List<Brick> getBricks() { return bricks; }
    public BonusManager getBonusManager() { return bonusManager; }
    public Color getCurrentBackground() { return currentBackground; }
    public int getCurrentLevelNumber() { return currentLevelNumber; }
    public int getLives() { return lives; }
    public int getScore() { return score; }
}
