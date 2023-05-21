package com.vasll.sockettictactoe.game.server;

import android.util.Log;

import com.vasll.sockettictactoe.game.logic.Board;
import com.vasll.sockettictactoe.game.logic.Condition;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class GameHandler extends Thread {
    private static final String TAG = "TicTacToe-GameHandler";
    private ServerPlayer player1, player2;
    private Board board;
    private int currentTurnPlayerId;

    public GameHandler(ServerPlayer player1, ServerPlayer player2){
        this.player1 = player1;
        this.player2 = player2;
        this.board = new Board(player1, player2);
        this.currentTurnPlayerId = 1; // First player to play is player1
    }

    @Override
    public void run() {
        try {
            broadcastHandshake(); // Send information about the game
            broadcastBoard(); // Send initial board state to all players

            // Handle the moves from clients
            Thread threadPlayer1 = new Thread(new PlayerRunnable(player1, 2));
            threadPlayer1.start();

            Thread threadPlayer2 = new Thread(new PlayerRunnable(player2, 1));
            threadPlayer2.start();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /** [BOARD] Message
     * Sends the current board as a JSON
     */
    private void broadcastBoard() throws IOException, JSONException {
        JSONObject json = new JSONObject()
                .put("message_type", "board")
                .put("board", board.toJsonArray())
                .put("current_turn_player_id", currentTurnPlayerId);

        Log.i(TAG, json.toString());
        player1.getOutputStream().writeObject(json.toString());
        player2.getOutputStream().writeObject(json.toString());
    }

    /** [BOARD] Message
     * Sends the current board as a JSON
     */
    private void sendBoard(ServerPlayer p) throws IOException, JSONException {
        JSONObject json = new JSONObject()
                .put("message_type", "board")
                .put("board", board.toJsonArray())
                .put("current_turn_player_id", currentTurnPlayerId);

        Log.i(TAG, json.toString());
        p.getOutputStream().writeObject(json.toString());
    }

    /** [CONDITION] Message
     * Sends the win/lose condition to each of the players
     */
    private void sendWinCondition(ServerPlayer winner, ServerPlayer loser) throws IOException, JSONException {
        JSONObject winJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.WIN.literal());

        JSONObject loseJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.LOSE.literal());

        winner.getOutputStream().writeObject(winJson.toString());
        loser.getOutputStream().writeObject(loseJson.toString());
    }

    /** [CONDITION] Message
     * Sends the win/lose condition to each of the players
     */
    private void broadcastDrawCondition() throws IOException, JSONException {
        JSONObject winJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.DRAW.literal());

        player1.getOutputStream().writeObject(winJson.toString());
        player2.getOutputStream().writeObject(winJson.toString());
    }

    /** [HANDSHAKE] Message
     * Sends information about the new game. Each player will receive what their
     * ID is (either 1 or 2) and what the id of the enemy player is.
     */
    private void broadcastHandshake() throws IOException, JSONException {
        JSONObject jsonPlayer1 = new JSONObject()   // Message for player1
                .put("message_type", "handshake")
                .put("player_id", 1)
                .put("enemy_id", 2)
                .put("starting_player_id", currentTurnPlayerId);
        player1.getOutputStream().writeObject(jsonPlayer1.toString());

        JSONObject jsonPlayer2 = new JSONObject()   // Message for player2
                .put("message_type", "handshake")
                .put("player_id", 2)
                .put("enemy_id", 1)
                .put("starting_player_id", currentTurnPlayerId);
        player2.getOutputStream().writeObject(jsonPlayer2.toString());
    }


    private class PlayerRunnable implements Runnable {
        private static final String TAG = "TicTacToe-GameHandler-PlayerRunnable";
        ServerPlayer player;
        int nextTurnPlayerId;

        public PlayerRunnable(ServerPlayer player, int nextTurnPlayerId) {
            this.player = player;
            this.nextTurnPlayerId = nextTurnPlayerId;
        }

        @Override
        public void run() {
            while(true){
                try {
                    JSONObject message = new JSONObject((String) player.getInputStream().readObject());
                    int player_id = message.getInt("player_id");
                    int row = message.getInt("row");
                    int col = message.getInt("col");

                    if (player_id != currentTurnPlayerId) {
                        continue;
                    }

                    // Make the move and check if it is valid, if it isn't go back at the top of the loop
                    Log.i(TAG, "Player "+player_id+" making move on ("+row+", "+col+")");
                    boolean isMoveValid = board.makeMove(player.charOfPlayer, row, col);
                    if(!isMoveValid){
                        Log.i(TAG, "Player "+player_id+" invalid move detected");
                        sendBoard(player);  // Re-send the board to the player
                        continue;
                    }
                    boolean hasPlayerWon = board.hasPlayerWon(player.charOfPlayer);
                    if(!hasPlayerWon){
                        if(board.isFull()){
                            broadcastDrawCondition();
                            break;
                        }
                    }

                    currentTurnPlayerId = nextTurnPlayerId;
                    broadcastBoard();
                } catch (IOException | ClassNotFoundException | JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
