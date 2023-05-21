package com.vasll.sockettictactoe.game.logic;

/**
 * Represents a TicTacToe win condition
 */
public enum Condition {
    WIN {
        public String literal() { return "win"; }
    },
    LOSE {
        public String literal() { return "lose"; }
    },
    DRAW {
        public String literal() { return "draw"; }
    };

    public abstract String literal();

    public static Condition parse(String value) {
        for (Condition condition : Condition.values()) {
            if (condition.literal().equalsIgnoreCase(value)) {
                return condition;
            }
        }
        throw new IllegalArgumentException("Invalid condition: " + value);
    }
}
