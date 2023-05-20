package com.vasll.sockettictactoe.game;

/**
 * Represents a tictactoe win condition
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
}
