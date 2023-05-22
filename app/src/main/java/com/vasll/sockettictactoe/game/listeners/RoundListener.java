package com.vasll.sockettictactoe.game.listeners;

import org.json.JSONException;
import org.json.JSONObject;

public interface RoundListener {
    void onNextRound(int player1Score, int player2Score, int currentRoundCount);
}