package com.vasll.sockettictactoe.game.logic;

import com.vasll.sockettictactoe.game.server.ServerPlayer;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.Arrays;

/** Represents a TicTacToe game Board, only used server-side for builtin validation */
public class Board {
    private char[][] board;
    public final char charPlayer1;
    public final char charPlayer2;
    public final char charEmpty = ' ';

    public Board(ServerPlayer player1, ServerPlayer player2) {
        this.board = new char[3][3];
        populateEmptyTable();

        this.charPlayer1 = player1.charOfPlayer;
        this.charPlayer2 = player2.charOfPlayer;
    }

    /**
     * Makes a move on the board
     * @return true if the move is valid, false otherwise
     */
    public boolean makeMove(char charOfPlayer, int row, int col){
        if (row < 0 || row >= 3 || col < 0 || col >= 3 || board[row][col] != charEmpty ||
                (charOfPlayer != charPlayer1 && charOfPlayer != charPlayer2)
        ) {
            return false; // Invalid move
        }

        board[row][col] = charOfPlayer;
        return true;
    }

    /**
     * Checks if one of the two players has won the game
     */
    public boolean hasPlayerWon(char charOfPlayer) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == charOfPlayer && board[i][1] == charOfPlayer && board[i][2] == charOfPlayer) {
                return true;
            }
        }

        // Check columns
        for (int j = 0; j < 3; j++) {
            if (board[0][j] == charOfPlayer && board[1][j] == charOfPlayer && board[2][j] == charOfPlayer) {
                return true;
            }
        }

        // Check diagonals
        if (board[0][0] == charOfPlayer && board[1][1] == charOfPlayer && board[2][2] == charOfPlayer) {
            return true;
        }
        if (board[0][2] == charOfPlayer && board[1][1] == charOfPlayer && board[2][0] == charOfPlayer) {
            return true;
        }

        return false;
    }

    /**
     * Can be used to check if the game is draw.
     * @return true if the board is full, false if the board is empty
     */
    public boolean isFull(){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == charEmpty) // Check for empty cells
                    return false;
            }
        }
        return true;
    }

    /** @return the board as a JSONArray */
    public JSONArray toJsonArray() {
        JSONArray jsonBoard = new JSONArray();

        for(int i=0; i<3; i++){
            JSONArray jsonBoardRow = new JSONArray();
            for(int j=0; j<3; j++){
                jsonBoardRow.put(String.valueOf(board[i][j]));
            }
            jsonBoard.put(jsonBoardRow);
        }

        return jsonBoard;
    }

    /** @return string representation of the Board */
    @Override
    public String toString() {
        StringBuilder table = new StringBuilder();

        for(int i=0; i<3; i++){
            StringBuilder row = new StringBuilder();
            for(int j=0; j<3; j++){
                row.append("|").append(board[i][j]).append("|");
            }
            table.append(row).append("\n");
        }

        return table.toString();
    }

    private void populateEmptyTable(){
        for (int i = 0; i < 3; i++) {
            Arrays.fill(board[i], charEmpty);
        }
    }

    public static char[][] jsonArrayToBoard(JSONArray boardArray) throws JSONException {
        char[][] charMatrix = new char[boardArray.length()][];
        for (int i = 0; i < boardArray.length(); i++) {
            JSONArray rowArray = boardArray.getJSONArray(i);
            char[] charRow = new char[rowArray.length()];
            for (int j = 0; j < rowArray.length(); j++) {
                charRow[j] = rowArray.getString(j).charAt(0);
            }
            charMatrix[i] = charRow;
        }
        return charMatrix;
    }
}
