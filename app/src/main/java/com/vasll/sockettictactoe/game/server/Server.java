package com.vasll.sockettictactoe.game.server;

import android.util.Log;

import com.vasll.sockettictactoe.game.logic.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private static final String TAG = "TicTacToe-Server";
    public final int port;

    public Server(int port){
        this.port = port;
    }

    @Override
    public void run() {
        Log.i(TAG, "Starting on port "+port+"...");

        try(ServerSocket serverSocket = new ServerSocket(port)){
            Log.i(TAG, "Started successfully");

            Log.i(TAG, "Waiting for Player1...");
            Socket socketPlayer1 = serverSocket.accept();
            Log.i(TAG, "Player1 connected!");


            Log.i(TAG, "Waiting for Player2...");
            Socket socketPlayer2 = serverSocket.accept();
            Log.i(TAG, "Player2 connected!");

            ServerGameHandler serverGameHandler = new ServerGameHandler(
                new Player(socketPlayer1), new Player(socketPlayer2)
            );
            serverGameHandler.start();
        } catch (IOException e){
            Log.e(TAG, "Error with serverSocket");
        }
    }
}
