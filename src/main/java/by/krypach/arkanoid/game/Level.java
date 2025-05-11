package by.krypach.arkanoid.game;
import by.krypach.arkanoid.models.Brick;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Level {
    private final int levelNumber;
    private final List<Brick> bricks;
    private final boolean hasBonuses;
    private final Color baseColor;

    public Level(int levelNumber, List<Brick> bricks, boolean hasBonuses, Color baseColor) {
        this.levelNumber = levelNumber;
        this.bricks = bricks;
        this.hasBonuses = hasBonuses;
        this.baseColor = baseColor;
    }

    public List<Brick> getBricks() {
        return new ArrayList<>(bricks);
    }
}
