package com.vasll.sockettictactoe.game.logic;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This exists because in GameClient.ClientOutputHandler I use a BlockingQueue to handle the moves
 * and the Type has to be an Object.
 */
public class Move {
    private final int row;
    private final int col;

    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /** Packs the Move object into a JSON object that can be understood from the TicTacToe server */
    public JSONObject toJsonMessage(int player_id) throws JSONException {
        return new JSONObject()
            .put("message_type", "make_move")
            .put("player_id", player_id)
            .put("row", this.row)
            .put("col", this.col);
    }
}
