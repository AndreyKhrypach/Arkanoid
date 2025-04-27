package by.krypach.arkanoid;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class GamePanel extends JPanel implements KeyListener {
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

        Timer timer = new Timer(16, e -> {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastTime) / 1_000_000_000.0; // В миллисекундах
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
        } else {
            ball.move(deltaTime);
        }

        checkCollisions();

        // Проверка победы (все кирпичи уничтожены)
        if (bricks.isEmpty()) {
            isRunning = false;
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
                    ball.setSpeed(0, -250); // Старт вертикально вверх
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

    public void keyTyped(KeyEvent e) {
    }

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
                g.drawString(gameOver, getWidth() / 2 - width / 2, getHeight() / 2);

                g.setFont(new Font("Arial", Font.PLAIN, 20));
                scoreText = "Ваш счёт: " + score;
                width = g.getFontMetrics().stringWidth(scoreText);
                g.drawString(scoreText, getWidth() / 2 - width / 2, getHeight() / 2 + 40);
            }
        }

        g.setColor(Color.YELLOW);
        g.drawLine(0, 0, getWidth(), 0); // Верхняя граница
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1); // Нижняя
        g.drawLine(0, 0, 0, getHeight()); // Левая
        g.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight()); // Правая
    }

    private void initGame() {
        ball = new Ball(400, 300, 0, 0);
        paddle = new Paddle(350, 550, 100, 20);
        bricks = new ArrayList<>();
        Random random = new Random();

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 10; col++) {
                // Определяем живучесть в зависимости от ряда
                int hitsRequired = determineHitsRequired(row, random);

                bricks.add(new Brick(
                        col * 80 + 10,
                        row * 30 + 50,
                        70, 20,
                        5 - row, // номер ряда (от 1 до 5)
                        hitsRequired
                ));
            }
        }
    }

    private void checkCollisions() {
        // Отскок от стен
        if (ball.getX() <= 0 || ball.getX() >= getWidth() - ball.getSize()) {
            ball.reverseX();
            // Корректировка позиции для предотвращения залипания
            if (ball.getX() <= 0) ball.setX(1);
            if (ball.getX() >= getWidth() - ball.getSize())
                ball.setX(getWidth() - ball.getSize() - 1);
        }
        // Улучшенная обработка верхней границы
        if (ball.getY() <= 0) {
            ball.reverseY();
            ball.setY(1); // Принудительно устанавливаем позицию ниже потолка
            // Добавляем небольшую горизонтальную составляющую, если мяч двигался строго вертикально
            if (Math.abs(ball.getSpeedX()) < 10) {
                ball.setSpeedX(ball.getSpeedX() + (Math.random() > 0.5 ? 15 : -15));
            }
        }

        //Отскок от платформы
        if (ball.getBounds().intersects(paddle.getBounds()) && !ball.isStuckToPaddle()) {
            double ballCenterX = ball.getX() + ball.getSize() / 2f;
            float paddleCenterX = paddle.getX() + paddle.getWidth() / 2f;
            double relativeIntersect = (ballCenterX - paddleCenterX) / (paddle.getWidth() / 2f);

            // Параметры для настройки:
            float maxBounceAngle = 60f; // Максимальный угол (градусы)
            float minSpeed = 180f;      // Минимальная скорость
            float speedBoost = 1.2f;    // Усиление от скорости платформы

            // Рассчитываем угол
            double bounceAngle = relativeIntersect * maxBounceAngle;

            // Учитываем скорость платформы
            float paddleSpeed = paddle.getCurrentSpeed();
            float speed = minSpeed + Math.abs(paddleSpeed) * speedBoost;

            ball.setSpeed(
                    (float) (speed * Math.sin(Math.toRadians(bounceAngle))),
                    (float) (-speed * Math.cos(Math.toRadians(bounceAngle)))
            );

            ball.setY(paddle.getY() - ball.getSize());
        }

        // Улучшенная проверка столкновений с кирпичами
        bricks.removeIf(brick -> {
            if (!brick.isDestroyed() && ball.getBounds().intersects(brick.getBounds())) {
                // Определяем сторону столкновения
                Rectangle ballRect = ball.getBounds();
                Rectangle brickRect = brick.getBounds();

                // Вычисляем пересечение
                Rectangle intersection = ballRect.intersection(brickRect);

                // Определяем направление отскока
                if (intersection.width > intersection.height) {
                    // Столкновение сверху или снизу
                    if (ballRect.y < brickRect.y) {
                        // Удар снизу кирпича
                        ball.setY(brickRect.y - ball.getSize());
                    } else {
                        // Удар сверху кирпича
                        ball.setY(brickRect.y + brickRect.height);
                    }
                    ball.reverseY();
                } else {
                    // Столкновение сбоку
                    if (ballRect.x < brickRect.x) {
                        // Удар справа от кирпича
                        ball.setX(brickRect.x - ball.getSize());
                    } else {
                        // Удар слева от кирпича
                        ball.setX(brickRect.x + brickRect.width);
                    }
                    ball.reverseX();
                }

                // Обрабатываем удар по кирпичу
                boolean destroyed = brick.hit();
                if (destroyed) {
                    addScore(brick.getRow() * brick.getMaxHits());
                    return true;
                }
                return false;
            }
            return false;
        });
    }

    private void addScore(int row) {
        // 1-5 очков в зависимости от ряда
        score += row;

        // Проверка на дополнительную жизнь (каждые 100 очков)
        int oldScore = score - row;
        if (oldScore / EXTRA_LIFE_SCORE < score / EXTRA_LIFE_SCORE) {
            lives++;
            // Анимация получения жизни
            new Thread(() -> {
                for (int i = 0; i < 5; i++) {
                    setBackground(i % 2 == 0 ? Color.GREEN : Color.BLACK);
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                    }
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

    // Определяем сколько ударов нужно для кирпича
    private int determineHitsRequired(int row, Random random) {
        // Вероятности для каждого ряда (можно настроить)
        switch (row) {
            case 0: // Самый верхний ряд (ряд 1 в игре)
                // 60% - 1 удар, 25% - 2 удара, 10% - 3 удара, 5% - 4 удара
                double chance = random.nextDouble();
                if (chance < 0.6) return 1;
                if (chance < 0.85) return 2;
                if (chance < 0.95) return 3;
                return 4;

            case 1: // Второй сверху ряд
                // 40% - 1 удар, 30% - 2 удара, 20% - 3 удара, 10% - 4 удара
                chance = random.nextDouble();
                if (chance < 0.4) return 1;
                if (chance < 0.7) return 2;
                if (chance < 0.9) return 3;
                return 4;

            case 2: // Средний ряд
                // 20% - 1 удар, 30% - 2 удара, 30% - 3 удара, 15% - 4 удара, 5% - 5 ударов
                chance = random.nextDouble();
                if (chance < 0.2) return 1;
                if (chance < 0.5) return 2;
                if (chance < 0.8) return 3;
                if (chance < 0.95) return 4;
                return 5;

            case 3: // Четвертый ряд
                // 10% - 1 удар, 20% - 2 удара, 30% - 3 удара, 25% - 4 удара, 15% - 5 ударов
                chance = random.nextDouble();
                if (chance < 0.1) return 1;
                if (chance < 0.3) return 2;
                if (chance < 0.6) return 3;
                if (chance < 0.85) return 4;
                return 5;

            case 4: // Самый нижний ряд
                // 5% - 1 удар, 10% - 2 удара, 20% - 3 удара, 30% - 4 удара, 35% - 5 ударов
                chance = random.nextDouble();
                if (chance < 0.05) return 1;
                if (chance < 0.15) return 2;
                if (chance < 0.35) return 3;
                if (chance < 0.65) return 4;
                return 5;

            default:
                return 1; // На всякий случай
        }
    }
}
