package com.vasll.sockettictactoe.game;

import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Client {
    private final Logger LOGGER = Logger.getLogger(
        Client.class.getName()
    );
    private static final int PORT = 8888;
    private static int player_id, enemy_id;
    private static ObjectInputStream inputStream;
    private static ObjectOutputStream outputStream;
    private static int currentTurnPlayerId;

    public static void main(String[] args) {
        try(Socket socket = new Socket("localhost", PORT)) {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            // Get the handshake
            JSONObject handshake = new JSONObject((String) inputStream.readObject());
            // TODO check if message_type is handshake
            player_id = handshake.getInt("player_id");
            enemy_id = handshake.getInt("enemy_id");

            System.out.println("You are player "+player_id);

            // Incoming messages handler
            while(true){
                JSONObject message = new JSONObject((String) inputStream.readObject());
                System.out.println(message.toString());
                String message_type = message.getString("message_type");

                switch (message_type){
                    case "board" -> handleBoardMessage(message);
                    case "condition" -> handleConditionMessage(message);
                    case "disconnect" -> handleDisconnectMessage(message);
                    default -> { /* TODO implement */ }
                }
            }

            /*
            while(true) {
                // Prompt the player for their move
                int row = promptForInput("Enter the row (0-2): ");
                int col = promptForInput("Enter the column (0-2): ");

                // Send the player's move to the server
                output.writeInt(row);
                output.writeInt(col);
                output.flush();

                // Receive updated game state from the server

                printBoard((char[][]) input.readObject());
            }*/


        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private static void handleBoardMessage(JSONObject message) throws IOException {
        currentTurnPlayerId = message.getInt("current_turn_player_id");
        char[][] board = Board.jsonArrayToBoard(message.getJSONArray("board"));
        printBoard(board);

        // Prompt the player for their move
        int row = promptForInput("Enter the row (0-2): ");
        int col = promptForInput("Enter the column (0-2): ");

        JSONObject json = new JSONObject()
            .put("message_type", "make_move")
            .put("player_id", player_id)
            .put("row", row)
            .put("col", col);

        outputStream.writeObject(json.toString());

        // TODO implement
        // Get the board from the JSON
        // Print the board
        // Ask the user for the next move
        // Send move to server
    }

    private static void handleConditionMessage(JSONObject message){
        // TODO implement
    }

    private static void handleDisconnectMessage(JSONObject message){
        // TODO implement
    }

    private static int promptForInput(String message) throws IOException {
        System.out.print(message);
        int input = System.in.read();
        System.in.skip(System.in.available()); // Clear the input buffer
        return Character.getNumericValue(input);
    }

    private static void printBoard(char[][] board) {
        System.out.println("Board:");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print("|"+board[i][j]+"|");
            }
            System.out.println();
        }
        System.out.println();
    }
}
