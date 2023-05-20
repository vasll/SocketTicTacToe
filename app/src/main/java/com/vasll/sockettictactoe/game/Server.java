package com.vasll.sockettictactoe.game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(
        Server.class.getName()
    );
    private static final int PORT = 8888;

    public static void main(String[] args) {
        LOGGER.info("Starting ServerSocket on port '"+PORT+"'...");

        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            LOGGER.info("ServerSocket started successfully");

            // TODO listen for incoming connections and send them the list of active games in JSON

            LOGGER.info("Waiting for player 1...");
            Socket socketPlayer1 = serverSocket.accept();
            LOGGER.info("Player 1 connected!");


            LOGGER.info("Waiting for player 2...");
            Socket socketPlayer2 = serverSocket.accept();
            LOGGER.info("Player 2 connected!");

            GameHandler gameHandler = new GameHandler(
                new Player(socketPlayer1, 'x'), new Player(socketPlayer2, 'o')
            );
            gameHandler.start();
        } catch (IOException e) {
            LOGGER.severe("Error with connection");
            throw new RuntimeException(e);
        }
    }
}
