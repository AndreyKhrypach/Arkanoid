package by.krypach.arkanoid;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements KeyListener {
    private static final int POINTS_PER_BRICK = 5;
    private static final int EXTRA_LIFE_SCORE = 100;

    private int lives = 3;
    private int score = 0;
    private int deathAnimationCounter = 0;
    private Ball ball;
    private Paddle paddle;
    private List<Brick> bricks;
    private boolean isRunning = true;
    private boolean isPaused = false; // Добавляем флаг паузы
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private long lastTime = System.nanoTime();

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        initGame();

//        long[] lastTime = {System.nanoTime()};

        Timer timer = new Timer(16, e -> {
            long currentTime = System.nanoTime();
            double deltaTime =  (currentTime - lastTime) / 1_000_000_000.0; // В миллисекундах
            lastTime = currentTime;
            update(deltaTime); // Переводим в секунды
            repaint();
        });
        timer.start();
    }

    public void update(double deltaTime) {
        if (!isRunning || isPaused) return;

        // Проверка падения мяча
        if (ball.getY() >= getHeight()) {
            loseLife();
            return; // Прерываем кадр, чтобы избежать двойной обработки
        }

        // Управление платформой
        float direction = 0;
        if (leftPressed) direction -= 1;
        if (rightPressed) direction += 1;
        paddle.move(direction);
        paddle.update(deltaTime);

        if (ball.isStuckToPaddle()) {
            ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getSize() / 2);
            ball.setY(paddle.getY() - ball.getSize());
        }else {
            ball.move(deltaTime);
        }

        checkCollisions();

        // Проверка победы (все кирпичи уничтожены)
        if (bricks.isEmpty()) {
            isRunning = false;
            System.out.println("Победа!"); // Можно заменить на вывод в интерфейс
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_P -> isPaused = !isPaused;
            case KeyEvent.VK_SPACE -> {
                if (ball.isStuckToPaddle()) {
                    ball.setStuckToPaddle(false);
                    ball.setSpeed(0, -200); // Старт вертикально вверх
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
    public void keyTyped(KeyEvent e) {}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Отрисовка HUD
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Жизни: " + lives, 20, 30);

        String scoreText = "Очки: " + score;
        int scoreWidth = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, getWidth() - scoreWidth - 20, 30);

        // Мигание при потере жизни
        if (deathAnimationCounter > 0) {
            g.setColor(new Color(255, 0, 0, 70));
            g.fillRect(0, 0, getWidth(), getHeight());
            deathAnimationCounter--;
        }

        if (isPaused) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("PAUSED", 350, 300);
        }

        if (isRunning) {
            ball.draw(g);
            paddle.draw(g);
            bricks.forEach(brick -> brick.draw(g));
        }
        if (!isRunning) {
            if (bricks.isEmpty()) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 30));
                g.drawString("Ты победил!", 350, 300);
            } else if (lives <= 0) {  // Явно проверяем количество жизней
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 40));
                String gameOver = "GAME OVER";
                int width = g.getFontMetrics().stringWidth(gameOver);
                g.drawString(gameOver, getWidth()/2 - width/2, getHeight()/2);

                g.setFont(new Font("Arial", Font.PLAIN, 20));
                scoreText = "Ваш счёт: " + score;
                width = g.getFontMetrics().stringWidth(scoreText);
                g.drawString(scoreText, getWidth()/2 - width/2, getHeight()/2 + 40);
            }
        }

        g.setColor(Color.YELLOW);
        g.drawLine(0, 0, getWidth(), 0); // Верхняя граница
        g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1); // Нижняя
        g.drawLine(0, 0, 0, getHeight()); // Левая
        g.drawLine(getWidth()-1, 0, getWidth()-1, getHeight()); // Правая
    }

    private void initGame() {
        ball = new Ball(400, 300, 0, 0); // Начальная скорость 0
        paddle = new Paddle(350, 550, 100, 20);
        bricks = new ArrayList<>();

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 10; col++) {
                bricks.add(new Brick(col * 80 + 10, row * 30 + 50, 70, 20));
            }
        }
    }

    private void checkCollisions() {
        // Отскок от стен
        if (ball.getX() <= 0 || ball.getX() >= getWidth() - ball.getSize()) {
            ball.reverseX();
        }
        if (ball.getY() <= 0) {
            ball.reverseY();
        }

        //Отскок от платформы
        if (ball.getBounds().intersects(paddle.getBounds()) && !ball.isStuckToPaddle()) {
            float ballCenterX = ball.getX() + ball.getSize() / 2f;
            float paddleCenterX = paddle.getX() + paddle.getWidth() / 2f;
            float relativeIntersect = (ballCenterX - paddleCenterX) / (paddle.getWidth() / 2f);

            // Параметры для настройки:
            float maxBounceAngle = 60f; // Максимальный угол (градусы)
            float minSpeed = 180f;      // Минимальная скорость
            float speedBoost = 1.2f;    // Усиление от скорости платформы

            // Рассчитываем угол
            float bounceAngle = relativeIntersect * maxBounceAngle;

            // Учитываем скорость платформы
            float paddleSpeed = paddle.getCurrentSpeed();
            float speed = minSpeed + Math.abs(paddleSpeed) * speedBoost;

            ball.setSpeed(
                    (float)(speed * Math.sin(Math.toRadians(bounceAngle))),
                    (float)(-speed * Math.cos(Math.toRadians(bounceAngle)))
            );

            ball.setY(paddle.getY() - ball.getSize());
        }

        // Проверка столкновений с кирпичами
        bricks.removeIf(brick -> {
            if (ball.getBounds().intersects(brick.getBounds())) {
                ball.reverseY();
                addScore(POINTS_PER_BRICK);
                return true;
            }
            return false;
        });
    }

    private void addScore(int points) {
        int oldScore = score;
        score += points;

        // Проверяем, перешли ли через границу в 100 очков
        if (oldScore / EXTRA_LIFE_SCORE < score / EXTRA_LIFE_SCORE) {
            lives++;
            // Анимация получения жизни
            new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    setBackground(i % 2 == 0 ? Color.GREEN : Color.BLACK);
                    try { Thread.sleep(150); } catch (InterruptedException e) {}
                }
                setBackground(Color.BLACK);
            }).start();
        }
    }

    private void loseLife() {
        lives--;
        deathAnimationCounter = 10;

        if (lives <= 0) {
            isRunning = false;
            // Добавляем задержку перед показом "Game Over"
            new Timer(500, e -> {
                repaint();
            }).start();
        } else {
            resetAfterDeath();
        }
    }

    private void resetAfterDeath() {
        ball.setStuckToPaddle(true);
        ball.setX(paddle.getX() + paddle.getWidth() / 2 - ball.getSize() / 2);
        ball.setY(paddle.getY() - ball.getSize());
        paddle.setX(getWidth() / 2 - paddle.getWidth() / 2); // Центрируем платформу
    }
}
