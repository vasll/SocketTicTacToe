package com.vasll.sockettictactoe.game.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Wrapper class for a TicTacToe player's socket (server-side) */
public class ServerPlayer {
    private final Socket playerSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    public final char charOfPlayer;

    public ServerPlayer(Socket playerSocket, char charOfPlayer) {
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
