package by.krypach.arkanoid.game;

import by.krypach.arkanoid.models.Brick;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Level {
    private int levelNumber;
    private final List<Brick> bricks;
    private final boolean hasBonuses;
    private boolean levelCompleted;
    private final Color backgroundColor;

    public Level(int levelNumber, List<Brick> bricks, boolean hasBonuses, Color backgroundColor) {
        this.levelNumber = levelNumber;
        this.bricks = new ArrayList<>(bricks); // Защитная копия
        this.hasBonuses = hasBonuses;
        this.backgroundColor = backgroundColor;
    }

    // Геттеры для всех полей
    public int getLevelNumber() {
        return levelNumber;
    }

    public List<Brick> getBricks() {
        return new ArrayList<>(bricks); // Возвращаем защитную копию
    }

    public boolean hasBonuses() {
        return hasBonuses;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isCompleted() {
        return bricks.stream().noneMatch(Brick::isAlive);
    }

    public boolean isLevelCompleted() {
        return levelCompleted;
    }

    public void setLevelCompleted(boolean levelCompleted) {
        this.levelCompleted = levelCompleted;
    }
}
