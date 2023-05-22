package com.vasll.sockettictactoe.game.logic;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/** Wrapper class for a TicTacToe player's socket */
public class PlayerSocket {
    private final Socket playerSocket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public PlayerSocket(Socket playerSocket) {
        this.playerSocket = playerSocket;
        try {
            this.outputStream = new DataOutputStream(playerSocket.getOutputStream());
            this.inputStream = new DataInputStream(playerSocket.getInputStream());
        } catch (IOException e) {
            Log.e("PlayerSocket", "Exception while creating DataOutput/InputStream(s)", e);
        }
    }

    public Socket getPlayerSocket() {
        return playerSocket;
    }

    public DataOutputStream getDataOutputStream() {
        return outputStream;
    }

    public DataInputStream getDataInputStream() {
        return inputStream;
    }
}
