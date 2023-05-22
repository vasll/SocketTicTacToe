package com.vasll.sockettictactoe.game.server;

import android.util.Log;

import com.vasll.sockettictactoe.game.logic.PlayerSocket;
import com.vasll.sockettictactoe.game.logic.Board;
import com.vasll.sockettictactoe.game.logic.Condition;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class ServerGameHandler extends Thread {
    private static final String TAG = "ServerGameHandler";
    private static final char PLAYER_1_CHAR = 'x';
    private static final char PLAYER_2_CHAR = 'o';

    private final PlayerSocket playerSocket1, playerSocket2;
    private final Board board;
    private int currentTurnPlayerId;  // Keeps the id of the user that has the turn

    public ServerGameHandler(PlayerSocket playerSocket1, PlayerSocket playerSocket2){
        this.playerSocket1 = playerSocket1;
        this.playerSocket2 = playerSocket2;
        this.board = new Board(PLAYER_1_CHAR, PLAYER_2_CHAR);
        this.currentTurnPlayerId = 1; // First player to play is player1
    }

    @Override
    public void run() {
        Log.i(TAG, "Starting ServerGameHandler...");
        try {
            broadcastHandshake(); // Send information about the game
            broadcastBoard(); // Send initial board state to all players
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
        }

        // Handle the moves from clients from other Threads
        Thread threadPlayer1 = new Thread(new PlayerIOHandler(playerSocket1, PLAYER_1_CHAR));
        threadPlayer1.start();
        Thread threadPlayer2 = new Thread(new PlayerIOHandler(playerSocket2, PLAYER_2_CHAR));
        threadPlayer2.start();

        Log.i(TAG, "ServerGameHandler OK");
    }

    /** 'board' Message
     * Sends the current board as a JSON */
    private void broadcastBoard() throws IOException, JSONException {
        JSONObject json = new JSONObject()
            .put("message_type", "board")
            .put("board", board.toJsonArray())
            .put("next_turn_player_id", currentTurnPlayerId);

        Log.d(TAG, "broadcastBoard() - Sending message: "+json);
        playerSocket1.getDataOutputStream().writeUTF(json.toString());
        playerSocket1.getDataOutputStream().flush();
        playerSocket2.getDataOutputStream().writeUTF(json.toString());
        playerSocket2.getDataOutputStream().flush();
    }

    /** 'board' Message
     * Sends the current board as a JSON */
    private void sendBoard(PlayerSocket p) throws IOException, JSONException {
        JSONObject json = new JSONObject()
            .put("message_type", "board")
            .put("board", board.toJsonArray())
            .put("next_turn_player_id", currentTurnPlayerId);

        Log.d(TAG, "sendBoard() - Sending message: "+json);
        p.getDataOutputStream().writeUTF(json.toString());
        p.getDataOutputStream().flush();
    }

    /** 'condition' Message
     * Sends the win/lose condition to each of the players */
    private void sendWinCondition(PlayerSocket winner, PlayerSocket loser) throws IOException, JSONException {
        JSONObject winJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.WIN.literal());

        JSONObject loseJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.LOSE.literal());

        Log.d(TAG, "sendWinCondition() - Sending message to winner: "+winJson);
        winner.getDataOutputStream().writeUTF(winJson.toString());
        winner.getDataOutputStream().flush();

        Log.d(TAG, "sendWinCondition() - Sending message to loser : "+loseJson);
        loser.getDataOutputStream().writeUTF(loseJson.toString());
        loser.getDataOutputStream().flush();
    }

    /** 'condition' Message
     * Sends the win/lose condition to each of the players  */
    private void broadcastDrawCondition() throws IOException, JSONException {
        JSONObject drawJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.DRAW.literal());

        Log.d(TAG, "broadcastDrawCondition() - Sending message (broadcast): "+drawJson);
        playerSocket1.getDataOutputStream().writeUTF(drawJson.toString());
        playerSocket1.getDataOutputStream().flush();
        playerSocket2.getDataOutputStream().writeUTF(drawJson.toString());
        playerSocket2.getDataOutputStream().flush();
    }

    /** 'handshake' Message
     * Sends information about the new game. Each player will receive what their
     * ID is (either 1 or 2) and what the id of the enemy player is. */
    private void broadcastHandshake() throws IOException, JSONException {
        JSONObject jsonPlayer1 = new JSONObject()   // Message for player1
                .put("message_type", "handshake")
                .put("player_id", 1)
                .put("enemy_id", 2)
                .put("starting_player_id", currentTurnPlayerId);

        JSONObject jsonPlayer2 = new JSONObject()   // Message for player2
                .put("message_type", "handshake")
                .put("player_id", 2)
                .put("enemy_id", 1)
                .put("starting_player_id", currentTurnPlayerId);

        Log.d(TAG, "broadcastHandshake() - sending message: "+jsonPlayer1);
        playerSocket1.getDataOutputStream().writeUTF(jsonPlayer1.toString());
        playerSocket1.getDataOutputStream().flush();

        Log.d(TAG, "broadcastHandshake() - sending message: "+jsonPlayer2);
        playerSocket2.getDataOutputStream().writeUTF(jsonPlayer2.toString());
        playerSocket2.getDataOutputStream().flush();
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

    /** Handles the IO of a PlayerSocket server-side */
    private class PlayerIOHandler extends Thread {
        private static final String TAG = "Server-PlayerIOHandler";
        private final PlayerSocket playerSocket;
        private final char charOfPlayer;

        public PlayerIOHandler(PlayerSocket playerSocket, char charOfPlayer) {
            this.playerSocket = playerSocket;
            this.charOfPlayer = charOfPlayer;
        }

        @Override
        public void run() {
            try {
                Log.i(TAG, "PlayerIOHandler started");

                while (!Thread.currentThread().isInterrupted()) {
                    // TODO implement round logic, max round count (i.e max round: 3)
                    Log.d(TAG, "Waiting for message from Client...");
                    String message = playerSocket.getDataInputStream().readUTF();
                    Log.d(TAG, "Got message from Client: "+message);

                    // Unpack message
                    JSONObject json = new JSONObject(message);
                    Log.d(TAG, "Message received as JSONObject: "+json);
                    int player_id = json.getInt("player_id");
                    int row = json.getInt("row");
                    int col = json.getInt("col");

                    // Check if the Player that wants to do the move has the right to do so
                    if (player_id != currentTurnPlayerId) { continue; }

                    // Make the move and check if it is valid, if it isn't go back at the top of the loop
                    boolean isMoveValid = board.makeMove(charOfPlayer, row, col);
                    if(!isMoveValid){
                        Log.i(TAG, "Invalid move detected from player with id: "+player_id);
                        sendBoard(playerSocket);  // Re-send the board to the player
                        continue;
                    }

                    // Check for win/draw condition
                    boolean hasPlayerWon = board.hasPlayerWon(charOfPlayer);
                    if(!hasPlayerWon){
                        if(board.isFull()){
                            broadcastDrawCondition();
                            board.clear();
                            broadcastBoard();
                            continue;
                        }
                    } else {
                        if(currentTurnPlayerId==1) {
                            sendWinCondition(playerSocket1, playerSocket2);
                        } else if (currentTurnPlayerId==2) {
                            sendWinCondition(playerSocket2, playerSocket1);
                        }
                        board.clear();
                        broadcastBoard();
                        continue;
                    }

                    currentTurnPlayerId = getNextTurnPlayerId(player_id);
                    broadcastBoard();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
            }
        }
    }
}
