package by.krypach.arkanoid.service;

import by.krypach.arkanoid.core.GamePanel;
import by.krypach.arkanoid.models.*;

import java.awt.*;

import static by.krypach.arkanoid.core.GamePanel.HEIGHT;
import static by.krypach.arkanoid.core.GamePanel.WIDTH;

public class RenderSystem {
    private final GamePanel gamePanel;

    public RenderSystem(GamePanel panel) {
        this.gamePanel = panel;
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
        for (Brick brick : gamePanel.getBricks()) {
            if (!brick.isDestroyed()) { // Changed from isAlive() to isDestroyed()
                brick.draw(g);
            }
        }
    }

    private void renderBalls(Graphics g) {
        gamePanel.getBalls().forEach(ball -> ball.draw(g));
    }

    private void renderPaddle(Graphics g) {
        gamePanel.getPaddle().draw(g);
    }

    private void renderBonuses(Graphics g) {
        gamePanel.getBonusManager().getActiveBonuses().forEach(bonus -> bonus.draw(g));
    }

    private void renderUI(Graphics g) {
        renderHUD(g);
        renderGameState(g);
        renderBorders(g);
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
        if (gamePanel.isPaused() && gamePanel.isLevelCompleted()) {
            drawCenteredText(g, "Уровень " + (gamePanel.getCurrentLevelNumber() - 1) + " пройден!", 40, HEIGHT / 2 - 50);
            drawCenteredText(g, "Переход на уровень " + gamePanel.getCurrentLevelNumber(), 30, HEIGHT / 2 + 20);
        }

        if (gamePanel.getDeathAnimationCounter() > 0) {
            g.setColor(new Color(255, 0, 0, 70));
            g.fillRect(0, 0, WIDTH, HEIGHT);
            gamePanel.setDeathAnimationCounter(gamePanel.getDeathAnimationCounter() - 1);
        }

        if (gamePanel.isPaused()) {
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
            if (gamePanel.isLevelCompleted() && gamePanel.getCurrentLevelNumber() > 10) {
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