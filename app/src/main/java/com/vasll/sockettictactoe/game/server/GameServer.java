package com.vasll.sockettictactoe.game.server;

import android.util.Log;

import com.vasll.sockettictactoe.game.logic.PlayerSocket;
import com.vasll.sockettictactoe.game.logic.Board;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * GameServer for the SocketTicTacToe Android game
 * Waits for a connection from 2 sockets (players) and when the connection is established
 * the game will start.
 */
public class GameServer extends Thread {
    private static final String TAG = "GameServer";
    private static final char PLAYER_1_CHAR = 'X';
    private static final char PLAYER_2_CHAR = 'O';
    private ServerSocket serverSocket;
    private DiscoveryServer discoveryServer;

    private final int maxRounds;   // How many rounds the game should last
    private PlayerSocket socketPlayer1, socketPlayer2;
    private final Board board;
    private int currentTurnPlayerId;  // Keeps the id of the user that has the turn
    private int player1Score = 0, player2Score = 0;
    private int currentRound = 1; // Rounds start from 1
    public final int port;

    private PlayerIOHandler player1IOHandler;
    private PlayerIOHandler player2IOHandler;

    public GameServer(int port, int maxRounds) {
        this.port = port;
        this.board = new Board(PLAYER_1_CHAR, PLAYER_2_CHAR);
        this.maxRounds = maxRounds;
        this.currentTurnPlayerId = 1; // First player to play is player1
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "Starting ServerSocket on port "+port+"...");
            serverSocket = new ServerSocket(port);
            Log.i(TAG, "Connection to ServerSocket OK");

            // Start the DiscoveryServer
            discoveryServer = new DiscoveryServer(port);
            discoveryServer.start();

            Log.i(TAG, "Waiting for Player1...");
            socketPlayer1 = new PlayerSocket(serverSocket.accept());
            Log.i(TAG, "Player1 connected!");

            Log.i(TAG, "Waiting for Player2...");
            socketPlayer2 = new PlayerSocket(serverSocket.accept());
            Log.i(TAG, "Player2 connected!");

            broadcastSendGame(); // Send information about the game
            broadcastBoard(); // Send initial board state to all players
        } catch (IOException e) {
            Log.w(TAG, "IOException", e);
            return;
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
        }

        // Handle the moves from clients from other Threads
        player1IOHandler = new PlayerIOHandler(socketPlayer1, PLAYER_1_CHAR);
        player1IOHandler.start();
        player2IOHandler = new PlayerIOHandler(socketPlayer2, PLAYER_2_CHAR);
        player2IOHandler.start();

        Log.i(TAG, "ServerGameHandler OK");
    }

    /** 'start_game' Message
     * Sends information about the new game. Each player will receive what their
     * ID is (either 1 or 2) and what the id of the enemy player is. */
    private void broadcastSendGame() throws IOException, JSONException {
        JSONObject jsonPlayer1 = new JSONObject()   // Message for player1
                .put("message_type", "start_game")
                .put("player_id", 1).put("enemy_id", 2)
                .put("max_rounds", maxRounds);

        JSONObject jsonPlayer2 = new JSONObject()   // Message for player2
                .put("message_type", "start_game")
                .put("player_id", 2).put("enemy_id", 1)
                .put("max_rounds", maxRounds);

        Log.d(TAG, "broadcastSendGame() - sending message: "+jsonPlayer1);
        socketPlayer1.getDataOutputStream().writeUTF(jsonPlayer1.toString());
        socketPlayer1.getDataOutputStream().flush();

        Log.d(TAG, "broadcastSendGame() - sending message: "+jsonPlayer2);
        socketPlayer2.getDataOutputStream().writeUTF(jsonPlayer2.toString());
        socketPlayer2.getDataOutputStream().flush();
    }

    /** 'board' Message
     * Sends the current board as a JSON */
    private void broadcastBoard() throws IOException, JSONException {
        JSONObject json = new JSONObject()
            .put("message_type", "board")
            .put("board", board.toJsonArray())
            .put("next_turn_player_id", currentTurnPlayerId);

        Log.d(TAG, "broadcastBoard() - Sending message (broadcast): "+json);
        socketPlayer1.getDataOutputStream().writeUTF(json.toString());
        socketPlayer1.getDataOutputStream().flush();
        socketPlayer2.getDataOutputStream().writeUTF(json.toString());
        socketPlayer2.getDataOutputStream().flush();
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

    /** 'end_round' Message
     * Signals that the current round has ended */
    private void broadcastEndRound() throws JSONException, IOException {
        JSONObject json = new JSONObject()
            .put("message_type", "end_round")
            .put("player_1_score", player1Score)
            .put("player_2_score", player2Score)
            .put("current_round_count", currentRound);

        Log.d(TAG, "broadcastEndRound() - Sending message (broadcast): "+json);
        socketPlayer1.getDataOutputStream().writeUTF(json.toString());
        socketPlayer1.getDataOutputStream().flush();
        socketPlayer2.getDataOutputStream().writeUTF(json.toString());
        socketPlayer2.getDataOutputStream().flush();
    }

    public boolean isGameFinished() {
        return currentRound == maxRounds;
    }

    private void broadcastEndGame() throws JSONException, IOException {
        JSONObject json = new JSONObject()
            .put("message_type", "end_game")
            .put("player_1_score", player1Score)
            .put("player_2_score", player2Score);

        Log.d(TAG, "broadcastEndGameAndExit() - Sending message (broadcast): "+json);
        socketPlayer1.getDataOutputStream().writeUTF(json.toString());
        socketPlayer1.getDataOutputStream().flush();
        socketPlayer2.getDataOutputStream().writeUTF(json.toString());
        socketPlayer2.getDataOutputStream().flush();
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

    public void close() {
        if (discoveryServer!=null) {
            discoveryServer.close();
        }
        if (player1IOHandler!=null) {
            player1IOHandler.close();
        }
        if (player2IOHandler!=null) {
            player2IOHandler.close();
        }
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Log.w(TAG, "idk", e);
        }
    }

    /** Handles the IO of a PlayerSocket server-side */
    private class PlayerIOHandler extends Thread {
        private static final String TAG = "Server-PlayerIOHandler";
        private final PlayerSocket playerSocket;
        private final char charOfPlayer;
        private boolean isCloseRequested = false;

        public PlayerIOHandler(PlayerSocket playerSocket, char charOfPlayer) {
            this.playerSocket = playerSocket;
            this.charOfPlayer = charOfPlayer;
        }

        @Override
        public void run() {
            try {
                Log.i(TAG, "PlayerIOHandler started");

                while (!Thread.currentThread().isInterrupted()) {
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
                            currentRound += 1;
                            if(isGameFinished()) {
                                broadcastBoard();
                                broadcastEndGame();
                                GameServer.this.close();
                                return;
                            }
                            board.clear();
                            broadcastEndRound();
                        }
                    } else {
                        if(currentTurnPlayerId==1) {
                            player1Score += 1;
                        } else if (currentTurnPlayerId==2) {
                            player2Score += 1;
                        }
                        currentRound += 1;
                        if(isGameFinished()) {
                            broadcastBoard();
                            broadcastEndGame();
                            GameServer.this.close();
                            return;
                        }
                        board.clear();
                        broadcastEndRound();
                    }

                    currentTurnPlayerId = getNextTurnPlayerId(player_id);
                    broadcastBoard();
                }
            } catch (IOException e) {
                if (isCloseRequested) {
                    Log.i(TAG, "Closing GameServer");
                    return;
                }
                Log.e(TAG, "IOException", e);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
            }
        }

        public void close() {
            isCloseRequested = true;
            try {
                playerSocket.getSocket().close();
            } catch (IOException ignored) {}
        }
    }
}
