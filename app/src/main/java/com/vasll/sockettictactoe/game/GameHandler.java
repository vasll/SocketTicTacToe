package com.vasll.sockettictactoe.game;

import java.io.IOException;
import java.util.logging.Logger;

import org.json.JSONObject;


public class GameHandler extends Thread{
    private static final Logger LOGGER = Logger.getLogger(
        GameHandler.class.getName()
    );
    private Player player1, player2;
    private Board board;
    private int currentTurnPlayerId;


    public GameHandler(Player player1, Player player2){
        this.player1 = player1;
        this.player2 = player2;
        this.board = new Board(player1, player2);
    }

    @Override
    public void run() {
        try {
            currentTurnPlayerId = 1; // First player to play is player1
            broadcastHandshake(); // Send information about the game
            broadcastBoard(); // Send initial board state to all players
            System.out.println(board.toString());

            // Handle the moves from clients
            Thread threadPlayer1 = new Thread(new PlayerRunnable(player1, 2));
            threadPlayer1.start();

            Thread threadPlayer2 = new Thread(new PlayerRunnable(player2, 1));
            threadPlayer2.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** [BOARD] Message
     * Sends the current board as a JSON
     */
    private void broadcastBoard() throws IOException {
        JSONObject json = new JSONObject()
            .put("message_type", "board")
            .put("board", board.toJsonArray())
            .put("current_turn_player_id", currentTurnPlayerId);

        LOGGER.info(json.toString());
        player1.getOutputStream().writeObject(json.toString());
        player2.getOutputStream().writeObject(json.toString());
    }

    /** [BOARD] Message
     * Sends the current board as a JSON
     */
    private void sendBoard(Player p) throws IOException {
        JSONObject json = new JSONObject()
                .put("message_type", "board")
                .put("board", board.toJsonArray())
                .put("current_turn_player_id", currentTurnPlayerId);

        System.out.println(json.toString());
        p.getOutputStream().writeObject(json.toString());
    }

    /** [CONDITION] Message
     * Sends the win/lose condition to each of the players
     */
    private void sendWinCondition(Player winner, Player loser) throws IOException {
        winner.getOutputStream().writeObject(
            new JSONObject().put("message_type", "condition").put("condition", Condition.WIN.literal())
                .toString()
        );
        loser.getOutputStream().writeObject(
            new JSONObject().put("message_type", "condition").put("condition", Condition.LOSE.literal())
                .toString()
        );
    }

    /** [CONDITION] Message
     * Sends the win/lose condition to each of the players
     */
    private void broadcastDrawCondition() throws IOException {
        player1.getOutputStream().writeObject(
            new JSONObject().put("message_type", "condition").put("condition", Condition.DRAW.literal())
                .toString()
        );
        player2.getOutputStream().writeObject(
            new JSONObject().put("message_type", "condition").put("condition", Condition.DRAW.literal())
                .toString()
        );
    }

    /** [HANDSHAKE] Message
     * Sends information about the new game. Each player will receive what their
     * ID is (either 1 or 2) and what the id of the enemy player is.
     */
    private void broadcastHandshake() throws IOException {
        // Message for player1
        JSONObject jsonPlayer1 = new JSONObject()
                .put("message_type", "handshake")
                .put("player_id", 1)
                .put("enemy_id", 2)
                .put("starting_player_id", currentTurnPlayerId);
        player1.getOutputStream().writeObject(jsonPlayer1.toString());

        // Message for player2
        JSONObject jsonPlayer2 = new JSONObject()
                .put("message_type", "handshake")
                .put("player_id", 2)
                .put("enemy_id", 1)
                .put("starting_player_id", currentTurnPlayerId);
        player2.getOutputStream().writeObject(jsonPlayer2.toString());
    }


    private class PlayerRunnable implements Runnable {
        Player player;
        int nextTurnPlayerId;

        public PlayerRunnable(Player player, int nextTurnPlayerId) {
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
                    LOGGER.info("Player "+player_id+" making move on ("+row+", "+col+")");
                    boolean isMoveValid = board.makeMove(player, row, col);
                    if(!isMoveValid){
                        LOGGER.info("Player "+player_id+" invalid move detected");
                        sendBoard(player);  // Re-send the board to the player
                        continue;
                    }
                    boolean hasPlayerWon = board.hasPlayerWon(player);
                    if(!hasPlayerWon){
                        if(board.isFull()){
                            broadcastDrawCondition();
                            break;
                        }
                    }

                    currentTurnPlayerId = nextTurnPlayerId;
                    broadcastBoard();
                    System.out.println(board.toString());
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
