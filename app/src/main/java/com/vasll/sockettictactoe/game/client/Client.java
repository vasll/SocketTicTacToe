package com.vasll.sockettictactoe.game.client;

import com.vasll.sockettictactoe.game.logic.Board;
import com.vasll.sockettictactoe.game.logic.Condition;
import com.vasll.sockettictactoe.game.logic.Move;
import com.vasll.sockettictactoe.game.interfaces.BoardUpdateListener;
import com.vasll.sockettictactoe.game.interfaces.ConditionListener;

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
    private final int port;
    private int playerId, enemyId;
    private int currentPlayerId;

    private ClientPlayer player;
    private ClientInputHandler clientInputHandler;
    private ClientOutputHandler clientOutputHandler;

    private final List<BoardUpdateListener> boardUpdateListeners = new ArrayList<>();
    private final List<ConditionListener> conditionListeners = new ArrayList<>();

    public Client(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try(Socket socket = new Socket("localhost", port)){
            player = new ClientPlayer(socket);

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


        // TODO
        // 1. Connect to socket
        // 2. Check for handshake and get player_id, enemy_id

        // Start 1 thread for the inputStream
        // Start 1 thread for the outputStream
    }

    public void addBoardUpdateListener(BoardUpdateListener boardUpdateListener){
        boardUpdateListeners.add(boardUpdateListener);
    }

    public void addConditionListener(ConditionListener conditionListener){
        conditionListeners.add(conditionListener);
    }

    /** Handles the incoming messages from a TicTacToe Server */
    private class ClientInputHandler extends Thread {
        private final ClientPlayer clientPlayer;

        public ClientInputHandler(ClientPlayer clientPlayer){
            this.clientPlayer = clientPlayer;
        }

        @Override
        public void run() {
            try{
                while (!Thread.currentThread().isInterrupted()) {
                    JSONObject message = new JSONObject(
                        (String) clientPlayer.getInputStream().readObject()
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
            currentPlayerId = message.getInt("current_player_id");
            char[][] board = Board.jsonArrayToBoard(message.getJSONArray("board"));

            for(BoardUpdateListener boardUpdateListener : boardUpdateListeners){
                boardUpdateListener.onBoardUpdate(board);
            }
        }

        /** Handles the condition message that is sent from the server and updates all the listeners */
        private void handleConditionMessage(JSONObject message) throws JSONException {
            String conditionLiteral = message.getString("condition");
            Condition condition = Condition.parse(conditionLiteral);

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
        private final ClientPlayer clientPlayer;
        private final BlockingQueue<Move> moveQueue;

        public ClientOutputHandler(ClientPlayer playerSocket) {
            this.clientPlayer = playerSocket;
            this.moveQueue = new LinkedBlockingQueue<>();
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Move move = moveQueue.take();  // Wait until a move is available
                    clientPlayer.getOutputStream().writeObject(move.toJsonMessage(playerId).toString());
                    clientPlayer.getOutputStream().flush();
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
