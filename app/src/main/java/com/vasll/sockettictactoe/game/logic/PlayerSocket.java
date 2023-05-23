package com.vasll.sockettictactoe.game.logic;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Wrapper class for a TicTacToe player's socket
 * Honestly this is pretty stupid and a regular Socket would have been.
 * With this there is no need to create DataOutput/DataInput streams manually and keep
 * them in the GameServer (saves a good 4 lines of code if we exclude the try/catch lmao)
 * */
public class PlayerSocket {
    private final Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    public PlayerSocket(Socket socket) {
        this.socket = socket;
        try {
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.inputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            Log.e("PlayerSocket", "Exception while creating DataOutput/InputStream(s)", e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public DataOutputStream getDataOutputStream() {
        return outputStream;
    }

    public DataInputStream getDataInputStream() {
        return inputStream;
    }
}
