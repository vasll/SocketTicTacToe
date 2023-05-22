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
    private int currentRound;  // keeps track of the current round

    public ServerGameHandler(PlayerSocket playerSocket1, PlayerSocket playerSocket2){
        this.playerSocket1 = playerSocket1;
        this.playerSocket2 = playerSocket2;
        this.board = new Board(PLAYER_1_CHAR, PLAYER_2_CHAR);
        this.currentTurnPlayerId = 1; // First player to play is player1
        currentRound = 0;
    }

    @Override
    public void run() {
        Log.i(TAG, "Starting ServerGameHandler");

        // Handle the moves from clients from another Thread
        Thread threadPlayer1 = new Thread(new PlayerIOHandler(playerSocket1, PLAYER_1_CHAR));
        threadPlayer1.start();

        Thread threadPlayer2 = new Thread(new PlayerIOHandler(playerSocket2, PLAYER_2_CHAR));
        threadPlayer2.start();
    }

    /** 'board' Message
     * Sends the current board as a JSON */
    private void broadcastBoard() throws IOException, JSONException {
        Log.i(TAG, "broadcastBoard()");
        JSONObject json = new JSONObject()
                .put("message_type", "board")
                .put("board", board.toJsonArray())
                .put("next_turn_player_id", currentTurnPlayerId);

        Log.i(TAG, "sending message: "+json.toString());
        playerSocket1.getOutputStream().writeUTF(json.toString());
        playerSocket2.getOutputStream().writeUTF(json.toString());
    }

    /** 'board' Message
     * Sends the current board as a JSON */
    private void sendBoard(PlayerSocket p) throws IOException, JSONException {
        Log.i(TAG, "sendBoard()");
        JSONObject json = new JSONObject()
                .put("message_type", "board")
                .put("board", board.toJsonArray())
                .put("next_turn_player_id", currentTurnPlayerId);

        Log.i(TAG, "sending message: "+json.toString());
        p.getOutputStream().writeUTF(json.toString());
    }

    /** 'condition' Message
     * Sends the win/lose condition to each of the players */
    private void sendWinCondition(PlayerSocket winner, PlayerSocket loser) throws IOException, JSONException {
        Log.i(TAG, "sendWinCondition()");
        JSONObject winJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.WIN.literal());

        JSONObject loseJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.LOSE.literal());

        Log.i(TAG, "sending messages: "+winJson.toString());
        Log.i(TAG, "sending messages: "+loseJson.toString());

        winner.getOutputStream().writeUTF(winJson.toString());
        loser.getOutputStream().writeUTF(loseJson.toString());
    }

    /** 'condition' Message
     * Sends the win/lose condition to each of the players  */
    private void broadcastDrawCondition() throws IOException, JSONException {
        Log.i(TAG, "broadcastDrawCondition()");
        JSONObject drawJson = new JSONObject()
            .put("message_type", "condition")
            .put("condition", Condition.DRAW.literal());

        Log.i(TAG, "sending message: "+drawJson.toString());
        playerSocket1.getOutputStream().writeUTF(drawJson.toString());
        playerSocket2.getOutputStream().writeUTF(drawJson.toString());
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

        JSONObject jsonPlayer2 = new JSONObject()   // Message for player2
                .put("message_type", "handshake")
                .put("player_id", 2)
                .put("enemy_id", 1)
                .put("starting_player_id", currentTurnPlayerId);

        Log.i(TAG, "sending messages: "+jsonPlayer1.toString());
        Log.i(TAG, "sending messages: "+jsonPlayer2.toString());

        playerSocket1.getOutputStream().writeUTF(jsonPlayer1.toString());
        playerSocket1.getOutputStream().flush();
        playerSocket2.getOutputStream().writeUTF(jsonPlayer2.toString());
        playerSocket2.getOutputStream().flush();
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

    private class PlayerIOHandler extends Thread {
        private static final String TAG = ServerGameHandler.TAG+"-PlayerIOHandler";
        private final PlayerSocket playerSocket;
        private final char charOfPlayer;

        public PlayerIOHandler(PlayerSocket playerSocket, char charOfPlayer) {
            this.playerSocket = playerSocket;
            this.charOfPlayer = charOfPlayer;
        }

        @Override
        public void run() {
            Log.i(TAG, "Starting PlayerIOHandler");
            try {
                broadcastHandshake(); // Send information about the game
                broadcastBoard(); // Send initial board state to all players

                while(true){
                    // TODO implement round logic, max round count (i.e max round: 3)

                    Log.i(TAG, "Waiting for message...");
                    JSONObject message = new JSONObject(
                        (String) playerSocket.getInputStream().readUTF()
                    );
                    // Unpack message
                    Log.i(TAG, "Got message: "+message);
                    int player_id = message.getInt("player_id");
                    int row = message.getInt("row");
                    int col = message.getInt("col");

                    // Check if the Player that wants to do the move has the right to do so
                    if (player_id != currentTurnPlayerId) { continue; }

                    // Make the move and check if it is valid, if it isn't go back at the top of the loop
                    boolean isMoveValid = board.makeMove(charOfPlayer, row, col);
                    if(!isMoveValid){
                        Log.i(TAG, "Player "+player_id+" invalid move detected");
                        sendBoard(playerSocket);  // Re-send the board to the player
                        continue;
                    }
                    boolean hasPlayerWon = board.hasPlayerWon(charOfPlayer);
                    if(!hasPlayerWon){
                        if(board.isFull()){
                            broadcastDrawCondition();
                            currentRound += 1;
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
                        currentRound += 1;
                        board.clear();
                        broadcastBoard();
                        continue;
                    }

                    currentTurnPlayerId = getNextTurnPlayerId(player_id);
                    broadcastBoard();
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error with PlayerIOHandler");
                throw new RuntimeException(e);
            }
        }
    }
}
