package com.vasll.sockettictactoe.game.client;

import com.vasll.sockettictactoe.game.listeners.TurnListener;
import com.vasll.sockettictactoe.game.logic.Board;
import com.vasll.sockettictactoe.game.logic.Condition;
import com.vasll.sockettictactoe.game.logic.Move;
import com.vasll.sockettictactoe.game.listeners.BoardUpdateListener;
import com.vasll.sockettictactoe.game.listeners.ConditionListener;
import com.vasll.sockettictactoe.game.logic.Player;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class Client extends Thread {
    private static final String TAG = "TicTacToe-Client";
    public final int port;
    private int playerId, enemyId;
    private int currentTurnPlayerId;

    private Player player;
    private ClientInputHandler clientInputHandler;
    private ClientOutputHandler clientOutputHandler;

    private final List<BoardUpdateListener> boardUpdateListeners = new ArrayList<>();
    private final List<ConditionListener> conditionListeners = new ArrayList<>();
    private final List<TurnListener> turnListeners = new ArrayList<>();

    public Client(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try(Socket socket = new Socket("localhost", port)){
            player = new Player(socket);

            // Handshake check
            // TODO better handshake check
            JSONObject handshake = new JSONObject(
                (String) player.getInputStream().readObject()
            );
            playerId = handshake.getInt("player_id");
            enemyId = handshake.getInt("enemy_id");

            clientInputHandler = new ClientInputHandler(player);
            clientOutputHandler = new ClientOutputHandler(player);
            clientInputHandler.start();
            clientOutputHandler.start();
        } catch (IOException | JSONException | ClassNotFoundException e) {
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

    /** Handles the incoming messages from a TicTacToe Server */
    private class ClientInputHandler extends Thread {
        private final Player player;

        public ClientInputHandler(Player player){
            this.player = player;
        }

        @Override
        public void run() {
            try{
                while (!Thread.currentThread().isInterrupted()) {
                    JSONObject message = new JSONObject(
                        (String) player.getInputStream().readObject()
                    );
                    String message_type = message.getString("message_type");

                    switch (message_type){
                        case "board" -> handleBoardMessage(message);
                        case "condition" -> handleConditionMessage(message);
                        case "disconnect" -> handleDisconnectMessage(message);
                        default -> { /* TODO implement */ }
                    }
                }
            } catch (JSONException | IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        /** Handles the board from the server and updates all the listeners with the new board */
        private void handleBoardMessage(JSONObject message) throws JSONException {
            char[][] board = Board.jsonArrayToBoard(message.getJSONArray("board"));
            int nextTurnPlayerId = message.getInt("current_player_id");

            /* If nextTurnPlayerId is different from currentTurnPlayerId means that
             * the turn has switched. Then we notify all the turnListeners. */
            if(nextTurnPlayerId != currentTurnPlayerId){
                currentTurnPlayerId = nextTurnPlayerId;
                for(TurnListener turnListener : turnListeners){
                    turnListener.onCurrentPlayerIdChanged(currentTurnPlayerId);
                }
            }

            // Notify boardUpdateListeners that board has changed
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

        public void stopHandler(){
            this.interrupt();
        }
    }

    /** Handles the input from the user and sends it as an output to the TicTacToe server */
    private class ClientOutputHandler extends Thread {
        private final Player player;
        private final BlockingQueue<Move> moveQueue;

        public ClientOutputHandler(Player playerSocket) {
            this.player = playerSocket;
            this.moveQueue = new LinkedBlockingQueue<>();
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Move move = moveQueue.take();  // Wait until a move is available
                    player.getOutputStream().writeObject(move.toJsonMessage(playerId).toString());
                    player.getOutputStream().flush();
                }
            } catch (IOException | InterruptedException | JSONException e) {
                throw new RuntimeException(e);
            }
        }

        public void makeMove(Move move) {
            moveQueue.add(move);
        }

        public void stopHandler(){
            this.interrupt();
        }
    }
}
