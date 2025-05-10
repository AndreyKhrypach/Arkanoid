package by.krypach.arkanoid.service;

import by.krypach.arkanoid.core.GamePanel;
import by.krypach.arkanoid.models.*;
import java.awt.*;
import java.util.List;

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

    private void renderBackground(Graphics g) {
        g.setColor(gamePanel.getCurrentBackground());
        g.fillRect(0, 0, GamePanel.WIDTH, GamePanel.HEIGHT);
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
        g.drawString(scoreText, GamePanel.WIDTH - scoreWidth - 20, 30);
    }

    private void renderGameState(Graphics g) {
        // ... (переносим всю логику отрисовки состояний из GamePanel)
    }

    private void renderBorders(Graphics g) {
        g.setColor(Color.YELLOW);
        g.drawLine(0, 0, GamePanel.WIDTH, 0);
        g.drawLine(0, GamePanel.HEIGHT - 1, GamePanel.WIDTH, GamePanel.HEIGHT - 1);
        g.drawLine(0, 0, 0, GamePanel.HEIGHT);
        g.drawLine(GamePanel.WIDTH - 1, 0, GamePanel.WIDTH - 1, GamePanel.HEIGHT);
    }
}