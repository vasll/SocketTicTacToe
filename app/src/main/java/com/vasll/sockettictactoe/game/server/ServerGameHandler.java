package com.vasll.sockettictactoe.game.server;

import android.util.Log;

import com.vasll.sockettictactoe.game.logic.Player;
import com.vasll.sockettictactoe.game.logic.Board;
import com.vasll.sockettictactoe.game.logic.Condition;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ServerGameHandler extends Thread {
    private static final String TAG = "TicTacToe-ServerGameHandler";
    private static final char PLAYER_1_CHAR = 'x';
    private static final char PLAYER_2_CHAR = 'o';

    private final Player player1, player2;
    private final Board board;
    private int currentTurnPlayerId;    // Keeps the id of the user that has the turn

    public ServerGameHandler(Player player1, Player player2){
        this.player1 = player1;
        this.player2 = player2;
        this.board = new Board(PLAYER_1_CHAR, PLAYER_2_CHAR);
        this.currentTurnPlayerId = 1; // First player to play is player1
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "Starting ServerGameHandler");
            broadcastHandshake(); // Send information about the game
            broadcastBoard(); // Send initial board state to all players

            // Handle the moves from clients from another Thread
            Thread threadPlayer1 = new Thread(new PlayerIOHandler(player1, PLAYER_1_CHAR));
            threadPlayer1.start();
            Thread threadPlayer2 = new Thread(new PlayerIOHandler(player2, PLAYER_2_CHAR));
            threadPlayer2.start();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /** 'board' Message
     * Sends the current board as a JSON */
    private void broadcastBoard() throws IOException, JSONException {
        Log.i(TAG, "broadcastBoard()");
        JSONObject json = new JSONObject()
                .put("message_type", "board")
                .put("board", board.toJsonArray())
                .put("current_turn_player_id", currentTurnPlayerId);

        Log.i(TAG, json.toString());
        player1.getOutputStream().writeObject(json.toString());
        player2.getOutputStream().writeObject(json.toString());
    }

    /** 'board' Message
     * Sends the current board as a JSON */
    private void sendBoard(Player p) throws IOException, JSONException {
        Log.i(TAG, "sendBoard()");
        JSONObject json = new JSONObject()
                .put("message_type", "board")
                .put("board", board.toJsonArray())
                .put("current_turn_player_id", currentTurnPlayerId);

        Log.i(TAG, json.toString());
        p.getOutputStream().writeObject(json.toString());
    }

    /** 'condition' Message
     * Sends the win/lose condition to each of the players */
    private void sendWinCondition(Player winner, Player loser) throws IOException, JSONException {
        Log.i(TAG, "sendWinCondition()");
        JSONObject winJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.WIN.literal());

        JSONObject loseJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.LOSE.literal());

        winner.getOutputStream().writeObject(winJson.toString());
        loser.getOutputStream().writeObject(loseJson.toString());
    }

    /** 'condition' Message
     * Sends the win/lose condition to each of the players  */
    private void broadcastDrawCondition() throws IOException, JSONException {
        Log.i(TAG, "broadcastDrawCondition()");
        JSONObject winJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.DRAW.literal());

        player1.getOutputStream().writeObject(winJson.toString());
        player2.getOutputStream().writeObject(winJson.toString());
    }

    /** 'handshake' Message
     * Sends information about the new game. Each player will receive what their
     * ID is (either 1 or 2) and what the id of the enemy player is. */
    private void broadcastHandshake() throws IOException, JSONException {
        Log.i(TAG, "broadcastHandshake()");
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

    /** This is stupid and will break if one day player ids are different from 1 and 2 */
    private int getNextTurnPlayerId(int playerId){
        if(playerId==1){
            return 2;
        } else if (playerId==2) {
            return 1;
        } else{
            return -1;
        }
    }

    private class PlayerIOHandler implements Runnable {
        private static final String TAG = "PlayerIOHandler";
        private final Player player;
        private final char charOfPlayer;

        public PlayerIOHandler(Player player, char charOfPlayer) {
            this.player = player;
            this.charOfPlayer = charOfPlayer;
        }

        @Override
        public void run() {
            Log.i(TAG, "Starting PlayerIOHandler");
            while(true){
                try {
                    JSONObject message = new JSONObject((String) player.getInputStream().readObject());
                    int player_id = message.getInt("player_id");
                    int row = message.getInt("row");
                    int col = message.getInt("col");

                    // Check if the Player that wants to do the move has the right to do so
                    if (player_id != currentTurnPlayerId) { continue; }

                    // Make the move and check if it is valid, if it isn't go back at the top of the loop
                    Log.i(TAG, "Player "+player_id+" making move on ("+row+", "+col+")");
                    boolean isMoveValid = board.makeMove(charOfPlayer, row, col);
                    if(!isMoveValid){
                        Log.i(TAG, "Player "+player_id+" invalid move detected");
                        sendBoard(player);  // Re-send the board to the player
                        continue;
                    }
                    boolean hasPlayerWon = board.hasPlayerWon(charOfPlayer);
                    if(!hasPlayerWon){
                        if(board.isFull()){
                            broadcastDrawCondition();
                            break;
                        }
                    }else {
                        // TODO Send win condition
                    }

                    currentTurnPlayerId = getNextTurnPlayerId(player_id);
                    broadcastBoard();
                } catch (IOException | ClassNotFoundException | JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
