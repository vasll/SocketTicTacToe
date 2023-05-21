package com.vasll.sockettictactoe.game.listeners;

public interface TurnListener {
    /** Listens for turn changes in a TicTacToe game. This method will be notified
      * when the id of the user that currently has the turn changes. */
    void onCurrentPlayerIdChanged(int newPlayerId);
}
