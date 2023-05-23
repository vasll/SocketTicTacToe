package com.vasll.sockettictactoe.game.listeners;

public interface BoardListener {
    void onBoardUpdate(char[][] board, int nextTurnPlayerId);
}
