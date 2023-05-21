package com.vasll.sockettictactoe.game.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Wrapper class for a TicTacToe player's socket (client-side) */
public class PlayerSocket {
    private final Socket playerSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public PlayerSocket(Socket playerSocket) {
        this.playerSocket = playerSocket;

        try {
            this.outputStream = new ObjectOutputStream(playerSocket.getOutputStream());
            this.inputStream = new ObjectInputStream(playerSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getPlayerSocket() {
        return playerSocket;
    }

    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }

    public ObjectInputStream getInputStream() {
        return inputStream;
    }
}
