package com.vasll.sockettictactoe.game.listeners;

public interface RoundListener {
    void onNextRound(int player1Score, int player2Score, int currentRoundCount);
}