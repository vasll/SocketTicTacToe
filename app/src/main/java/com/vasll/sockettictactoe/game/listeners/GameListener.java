package com.vasll.sockettictactoe.game.listeners;

import org.json.JSONException;
import org.json.JSONObject;

public interface GameListener {
    void onGameEnd(int player1Score, int player2Score);
}
