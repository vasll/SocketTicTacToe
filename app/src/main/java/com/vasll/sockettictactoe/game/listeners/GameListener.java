package com.vasll.sockettictactoe.game.listeners;

public interface GameListener {
    void onGameStart(int yourPlayerId, int enemyPlayerId, int maxRounds);
    void onGameEnd(int player1Score, int player2Score);
}
