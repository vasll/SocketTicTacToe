package com.vasll.sockettictactoe.game.server;

import android.util.Log;

import com.vasll.sockettictactoe.game.logic.PlayerSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private static final String TAG = "TicTacToe-Server";
    public final int port;
    private ServerSocket serverSocket;

    public Server(int port){
        this.port = port;
    }

    @Override
    public void run() {
        Log.i(TAG, "Starting server on port "+port+"...");

        try {
            serverSocket = new ServerSocket(port);
            Log.i(TAG, "Started successfully");

            // In an infinite loop
            // 1. Accept all connections
            // 2. Wait for the 'info' or 'connect' message_type, if no message
            // is received in 5 seconds, disconnect from the socket.

            // 'info' message
            // 1. Return the JSON containing server information and disconnect from the socket

            // 'connect' message
            // 1. Save the socket into an ArrayList
            // 2. If the ArrayList is now full start the ServerGameHandler
            // 3. If not wait for another player to join

            Log.i(TAG, "Waiting for Player1...");
            Socket socketPlayer1 = serverSocket.accept();
            Log.i(TAG, "Player1 connected!");


            Log.i(TAG, "Waiting for Player2...");
            Socket socketPlayer2 = serverSocket.accept();
            Log.i(TAG, "Player2 connected!");

            ServerGameHandler serverGameHandler = new ServerGameHandler(
                new PlayerSocket(socketPlayer1), new PlayerSocket(socketPlayer2)
            );
            serverGameHandler.start();
            serverGameHandler.join();
        } catch (IOException e){
            Log.e(TAG, "Error with serverSocket");
        } catch (InterruptedException ignored) {

        }
    }
}
