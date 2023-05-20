package com.vasll.sockettictactoe.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Simple wrapper class for a tictactoe player object */
public class Player {
    private final Socket playerSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    public final char charOfPlayer;

    public Player(Socket playerSocket, char charOfPlayer) {
        this.playerSocket = playerSocket;
        this.charOfPlayer = charOfPlayer;
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
