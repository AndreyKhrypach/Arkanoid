package by.krypach.arkanoid.enums;

import java.awt.*;

public enum BonusType {
    TRAP_SHRINK_PADDLE(Color.MAGENTA),
    PADDLE_EXTEND(Color.GREEN),
    BALL_SPEED_UP(Color.YELLOW),
    EXTRA_BALL(Color.CYAN),
    TIME_SLOW(Color.BLUE),
    EXTRA_LIFE(Color.PINK);

    private final Color color;

    BonusType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    // Метод для проверки, является ли бонус ловушкой
    public boolean isTrap() {
        return this == TRAP_SHRINK_PADDLE;
    }
}
