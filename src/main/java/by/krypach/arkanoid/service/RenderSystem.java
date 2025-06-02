package by.krypach.arkanoid.service;

import by.krypach.arkanoid.core.GamePanel;
import by.krypach.arkanoid.models.*;

import java.awt.*;
import java.io.InputStream;

import static by.krypach.arkanoid.core.GamePanel.HEIGHT;
import static by.krypach.arkanoid.core.GamePanel.WIDTH;

public class RenderSystem {
    private final GamePanel gamePanel;
    private Font chessFont;

    public RenderSystem(GamePanel panel) {
        this.gamePanel = panel;
        try {
            // Пробуем загрузить как ресурс
            InputStream is = getClass().getResourceAsStream("/DejaVuSans.ttf");
            if (is != null) {
                this.chessFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(26f);
            } else {
                throw new Exception("Файл шрифта не найден в ресурсах");
            }
        } catch (Exception e) {
            // Fallback на стандартный шрифт
            this.chessFont = new Font("Arial", Font.BOLD, 26);
            System.err.println("Не удалось загрузить шрифт, используем стандартный");
        }
    }

    public void render(Graphics g) {
        renderBackground(g);
        renderGameObjects(g);
        renderUI(g);
    }

    public void drawCenteredText(Graphics g, String text, int fontSize, int y) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, fontSize));
        int width = g.getFontMetrics().stringWidth(text);
        g.drawString(text, WIDTH / 2 - width / 2, y);
    }

    private void renderBackground(Graphics g) {
        if (gamePanel.isLifeAnimationActive()) {
            g.setColor(gamePanel.getLifeAnimationColor());
        } else if (gamePanel.isTimeSlowActive()) {
            g.setColor(new Color(70, 70, 255, 50));
        } else {
            g.setColor(gamePanel.getCurrentLevel().getBackgroundColor());
        }
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void renderGameObjects(Graphics g) {
        // Отрисовка в правильном порядке
        renderBricks(g);
        renderBalls(g);
        renderPaddle(g);
        renderBonuses(g);
    }

    private void renderBricks(Graphics g) {
        Color originalColor = g.getColor();

        for (Brick brick : gamePanel.getBricks()) {
            if (!brick.isDestroyed()) {
                // Рисуем кирпич (используем метод draw из класса Brick)
                brick.draw(g);

                if (brick.getBonusType() != null && brick.getBonusType().isTrap()) {
                    g.setColor(brick.getBonusType().getColor());
                    g.drawRect(brick.getX() + 5, brick.getY() + 5,
                            brick.getWidth() - 10, brick.getHeight() - 10);
                }

                if (gamePanel.getCurrentLevelNumber() == 4 && brick.getChessSymbol() != null
                        && !brick.getChessSymbol().trim().isEmpty()) {
                    boolean isWhiteFigure = brick.getY() > HEIGHT / 2;

                    // Уменьшаем ширину в 2 раза (но не менее 10 пикселей)
                    int padding = 2;
                    int bgWidth = Math.max(10, (brick.getWidth() - 2 * padding) / 2); // Уменьшили ширину
                    int bgHeight = brick.getHeight() - 2 * padding;

                    // Центрируем уменьшенный прямоугольник
                    int centerX = brick.getX() + (brick.getWidth() - bgWidth) / 2;

                    // Рисуем фон для фигуры
                    g.setColor(isWhiteFigure ? Color.WHITE : Color.BLACK);
                    g.fillRect(
                            centerX,
                            brick.getY() + padding,
                            bgWidth,
                            bgHeight
                    );

                    // Рисуем фигуру
                    g.setFont(chessFont);
                    String symbol = brick.getChessSymbol();
                    FontMetrics fm = g.getFontMetrics();
                    int x = brick.getX() + (brick.getWidth() - fm.stringWidth(symbol)) / 2;
                    int y = brick.getY() + brick.getHeight() / 2 + 10;

                    g.setColor(isWhiteFigure ? Color.BLACK : Color.WHITE);
                    g.drawString(symbol, x, y);
                }
            }
        }

        g.setColor(originalColor);
    }

    private void renderBalls(Graphics g) {
        gamePanel.getBalls().forEach(ball -> ball.draw(g));
    }

    private void renderPaddle(Graphics g) {
        gamePanel.getPaddle().draw(g);
        gamePanel.getPaddle().getLaserBeams().forEach(laser -> laser.draw(g));
    }

    private void renderBonuses(Graphics g) {
        gamePanel.getBonusManager().getActiveBonuses().forEach(bonus -> bonus.draw(g));
    }

    private void renderUI(Graphics g) {
        renderHUD(g);
        renderGameState(g);
        renderBorders(g);

        if (gamePanel.isControlsInverted()) {
            long remaining = (gamePanel.getControlsInvertedEndTime() - System.currentTimeMillis()) / 1000;
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Инверсия: " + remaining + "s", 20, HEIGHT - 20);
        }
    }

    private void renderHUD(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Уровень: " + gamePanel.getCurrentLevelNumber(), 300, 30);
        g.drawString("Жизни: " + gamePanel.getLives(), 20, 30);

        String scoreText = "Очки: " + gamePanel.getScore();
        int scoreWidth = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, WIDTH - scoreWidth - 20, 30);
    }


    private void renderGameState(Graphics g) {
        if (gamePanel.getCurrentLevel().isLevelCompleted()) {
            g.setColor(Color.orange);
            drawCenteredText(g, "Уровень " + (gamePanel.getCurrentLevelNumber()) + " пройден!", 40, HEIGHT / 2 - 50);
            drawCenteredText(g, "Переход на уровень " + (gamePanel.getCurrentLevelNumber() + 1), 30, HEIGHT / 2 + 20);
        }

        if (gamePanel.getDeathAnimationCounter() > 0) {
            g.setColor(new Color(255, 0, 0, 70));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            gamePanel.setDeathAnimationCounter(gamePanel.getDeathAnimationCounter() - 1);
        }

        if (gamePanel.isPaused() && !gamePanel.getCurrentLevel().isLevelCompleted()) {
            drawCenteredText(g, "PAUSED", 40, 300);
        }

        if (gamePanel.isTimeSlowActive()) {
            // Рисуем полосу прогресса вверху экрана
            int progressWidth = (int) (WIDTH * (gamePanel.getTimeSlowRemaining() / 15000f));
            g.setColor(new Color(100, 100, 255, 200));
            g.fillRect(0, 5, progressWidth, 5);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            String timeText = String.format("%.1f", gamePanel.getTimeSlowRemaining() / 1000f);
            g.drawString(timeText + "s", progressWidth + 5, 15);
        }

        if (!gamePanel.isRunning()) {
            if (gamePanel.getCurrentLevel().isLevelCompleted() && gamePanel.getCurrentLevelNumber() > 10) {
                drawCenteredText(g, "ИГРА ЗАВЕРШЕНА!", 40, HEIGHT / 2 - 30);
                drawCenteredText(g, "Финальный счет: " + gamePanel.getScore(), 30, HEIGHT / 2 + 20);
            } else if (gamePanel.getLives() <= 0) {
                drawGameOver(g);
            }
        }
    }

    private void renderBorders(Graphics g) {
        g.setColor(Color.YELLOW);
        g.drawLine(0, 0, WIDTH, 0);
        g.drawLine(0, HEIGHT - 1, WIDTH, HEIGHT - 1);
        g.drawLine(0, 0, 0, HEIGHT);
        g.drawLine(WIDTH - 1, 0, WIDTH - 1, HEIGHT);
    }

    private void drawGameOver(Graphics g) {
        drawCenteredText(g, "GAME OVER", 40, HEIGHT / 2);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String scoreText = "Ваш счёт: " + gamePanel.getScore();
        int width = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, WIDTH / 2 - width / 2, HEIGHT / 2 + 40);
    }
}
