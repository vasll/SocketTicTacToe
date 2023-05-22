package com.vasll.sockettictactoe.game.client;

import android.util.Log;
import com.vasll.sockettictactoe.game.listeners.TurnListener;
import com.vasll.sockettictactoe.game.logic.Board;
import com.vasll.sockettictactoe.game.logic.Condition;
import com.vasll.sockettictactoe.game.logic.Move;
import com.vasll.sockettictactoe.game.listeners.BoardUpdateListener;
import com.vasll.sockettictactoe.game.listeners.ConditionListener;
import com.vasll.sockettictactoe.game.logic.PlayerSocket;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Client extends Thread {
    private static final String TAG = "Client";
    public final int port;
    public final String ip;
    private int playerId, enemyId;
    private int currentPlayerId;
    private PlayerSocket playerSocket;

    private ClientInputHandler clientInputHandler;
    private ClientOutputHandler clientOutputHandler;

    private final List<BoardUpdateListener> boardUpdateListeners = new ArrayList<>();
    private final List<ConditionListener> conditionListeners = new ArrayList<>();
    private final List<TurnListener> turnListeners = new ArrayList<>();

    public Client(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "Connecting Socket Client to "+ip+":"+port+"...");
            playerSocket = new PlayerSocket(new Socket(ip, port));
            Log.i(TAG, "Connection to Socket OK");

            // Handshake check
            Log.d(TAG, "Waiting for handshake...");
            JSONObject handshake = new JSONObject(
                (String) playerSocket.getDataInputStream().readUTF()
            );
            // TODO check if handshake is good or not
            Log.d(TAG, "Received handshake: "+handshake);
            playerId = handshake.getInt("player_id");
            enemyId = handshake.getInt("enemy_id");

            clientInputHandler = new ClientInputHandler();
            clientInputHandler.start();

            clientOutputHandler = new ClientOutputHandler();
            clientOutputHandler.start();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void addBoardUpdateListener(BoardUpdateListener boardUpdateListener){
        boardUpdateListeners.add(boardUpdateListener);
    }

    public void addConditionListener(ConditionListener conditionListener){
        conditionListeners.add(conditionListener);
    }

    public void addTurnListener(TurnListener turnListener){
        turnListeners.add(turnListener);
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getEnemyId() {
        return enemyId;
    }

    public void makeMove(Move move) {
        clientOutputHandler.makeMove(move);
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
                        case "condition" -> handleConditionMessage(json);
                        case "disconnect" -> handleDisconnectMessage(json);
                        default -> { /* TODO implement */ }
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

            /* If nextTurnPlayerId is different from currentTurnPlayerId means that
             * the turn has switched. Then we notify all the turnListeners. */
            if(nextTurnPlayerId != currentPlayerId){
                currentPlayerId = nextTurnPlayerId;
                for(TurnListener turnListener : turnListeners){
                    turnListener.onCurrentPlayerIdChanged(currentPlayerId);
                }
            }

            // Notify boardUpdateListeners and update the UI on the main thread
            for(BoardUpdateListener boardUpdateListener : boardUpdateListeners){
                boardUpdateListener.onBoardUpdate(board);
            }
        }

        /** Handles the condition message that is sent from the server and updates all the listeners */
        private void handleConditionMessage(JSONObject message) throws JSONException {
            String conditionLiteral = message.getString("condition");
            Condition condition = Condition.parse(conditionLiteral);

            // Notify conditionListeners that game win condition has changed
            for(ConditionListener conditionListener : conditionListeners) {
                conditionListener.onCondition(condition);
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
                Log.e(TAG, "InterruptedException", e);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException", e);
            }
        }

        public void makeMove(Move move) {
            moveQueue.add(move);
        }
    }
}
