package com.vasll.sockettictactoe.game.client;

import android.util.Log;

import com.vasll.sockettictactoe.game.listeners.GameListener;
import com.vasll.sockettictactoe.game.listeners.RoundListener;
import com.vasll.sockettictactoe.game.logic.Board;
import com.vasll.sockettictactoe.game.logic.Move;
import com.vasll.sockettictactoe.game.listeners.BoardListener;
import com.vasll.sockettictactoe.game.logic.PlayerSocket;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class GameClient extends Thread {
    private static final String TAG = "GameClient";
    public final int port;
    public final String ip;
    private int playerId, enemyId, maxRounds;
    private PlayerSocket playerSocket;

    private ClientInputHandler clientInputHandler;
    private ClientOutputHandler clientOutputHandler;

    private final List<BoardListener> boardListeners = new ArrayList<>();
    private final List<RoundListener> roundListeners = new ArrayList<>();
    private final List<GameListener> gameListeners = new ArrayList<>();

    public GameClient(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "Connecting Socket Client to "+ip+":"+port+"...");
            playerSocket = new PlayerSocket(new Socket(ip, port));
            Log.i(TAG, "Connection to Socket OK");

            // Wait for game_start
            Log.d(TAG, "Waiting for game_start...");
            JSONObject gameStart = new JSONObject(
                (String) playerSocket.getDataInputStream().readUTF()
            );
            // TODO check if message is good or not
            Log.d(TAG, "Received game_start: "+gameStart);
            playerId = gameStart.getInt("player_id");
            enemyId = gameStart.getInt("enemy_id");
            maxRounds = gameStart.getInt("max_rounds");

            for(GameListener gameListener : gameListeners){
                gameListener.onGameStart(playerId,enemyId, maxRounds);
            }

            clientInputHandler = new ClientInputHandler();
            clientInputHandler.start();

            clientOutputHandler = new ClientOutputHandler();
            clientOutputHandler.start();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeMove(Move move) {
        clientOutputHandler.makeMove(move);
    }

    private void cleanUp() {
        Log.d(TAG, "Cleaning up Client resources and exiting...");
        clientInputHandler.interrupt();
        clientOutputHandler.interrupt();
        this.interrupt();
    }

    // Listeners
    public void addBoardUpdateListener(BoardListener boardListener) {
        boardListeners.add(boardListener);
    }

    public void addRoundListener(RoundListener roundListener) {
        roundListeners.add(roundListener);
    }

    public void addGameListener(GameListener gameListener) {
        gameListeners.add(gameListener);
    }

    // Other getters
    public int getPlayerId() {
        return playerId;
    }

    public int getEnemyId() {
        return enemyId;
    }

    /** Handles the incoming messages from a TicTacToe Server */
    private class ClientInputHandler extends Thread {
        private static final String TAG = "ClientInputHandler";

        @Override
        public void run() {
            try{
                while (!Thread.currentThread().isInterrupted()) {
                    String rawMessage = playerSocket.getDataInputStream().readUTF();
                    Log.d(TAG, "Message received: "+rawMessage);

                    JSONObject json = new JSONObject(rawMessage);
                    Log.d(TAG, "Message received as JSONObject: "+json);

                    String message_type = json.getString("message_type");
                    switch (message_type){
                        case "board" -> handleBoardMessage(json);
                        case "disconnect" -> handleDisconnectMessage(json);
                        case "end_round" -> handleEndRoundMessage(json);
                        case "end_game"-> {
                            handleEndGameMessage(json);
                            cleanUp();
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
            }
        }

        /** Handles the board from the server and updates all the listeners with the new board */
        private void handleBoardMessage(JSONObject message) throws JSONException {
            char[][] board = Board.jsonArrayToBoard(message.getJSONArray("board"));
            int nextTurnPlayerId = message.getInt("next_turn_player_id");

            // Notify boardUpdateListeners
            for(BoardListener boardListener : boardListeners){
                boardListener.onBoardUpdate(board, nextTurnPlayerId);
            }
        }

        private void handleEndRoundMessage(JSONObject message) throws JSONException {
            int player1Score = message.getInt("player_1_score");
            int player2Score = message.getInt("player_2_score");
            int currentRoundCount = message.getInt("current_round_count");

            for(RoundListener roundListener : roundListeners){
                roundListener.onNextRound(player1Score, player2Score, currentRoundCount);
            }
        }

        private void handleEndGameMessage(JSONObject message) throws JSONException {
            int player1Score = message.getInt("player_1_score");
            int player2Score = message.getInt("player_2_score");

            for(GameListener gameListener : gameListeners){
                gameListener.onGameEnd(player1Score, player2Score);
            }
        }

        private void handleDisconnectMessage(JSONObject message) throws JSONException {
            // TODO implement
        }
    }

    /** Handles the input from the user and sends it as an output to the TicTacToe server */
    private class ClientOutputHandler extends Thread {
        private final BlockingQueue<Move> moveQueue = new LinkedBlockingQueue<>();;

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Move move = moveQueue.take();  // Wait until a move is available
                    playerSocket.getDataOutputStream().writeUTF(
                        move.toJsonMessage(playerId).toString()
                    );
                    playerSocket.getDataOutputStream().flush();
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException", e);
            } catch (InterruptedException e) {
                Log.d(TAG, "Got Thread interrupt");
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
            }
        }

        public void makeMove(Move move) {
            moveQueue.add(move);
        }
    }
}
