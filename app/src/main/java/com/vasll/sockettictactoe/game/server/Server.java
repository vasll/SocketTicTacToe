package com.vasll.sockettictactoe.game.server;

import android.util.Log;

import com.vasll.sockettictactoe.game.logic.PlayerSocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**To be used as a local Server
 * It waits for two players to connect and starts a ServerGameHandler to manage the game */
public class Server extends Thread {
    private static final String TAG = "Server";
    public final int port;
    private ServerSocket serverSocket;
    private DiscoveryServer discoveryServer;

    public Server(int port){
        this.port = port;
    }

    // TODO implement UDP server for lobby system
    @Override
    public void run() {
        try {
            Log.i(TAG, "Starting ServerSocket on port "+port+"...");
            serverSocket = new ServerSocket(port);
            Log.i(TAG, "Connection to ServerSocket OK");

            // Start the DiscoveryServer
            discoveryServer = new DiscoveryServer(port);
            discoveryServer.start();

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

            // Join serverGameHandler and wait for game to finish
            serverGameHandler.join();
            discoveryServer.interrupt();
        } catch (IOException e){
            Log.e(TAG, "IOException", e);
        } catch (InterruptedException e) {
            Log.e(TAG, "InterruptedException", e);
        }
    }
}
