package com.vasll.sockettictactoe.game.server;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * DiscoveryServer for the SocketTicTacToe Android game
 * This servers listens on a UDP port (has to be the same as the TCP GameServer) and sends back
 * a simple response to notify the caller that a GameClient is running on this device
 */
public class DiscoveryServer extends Thread {
    private static final String TAG = "DiscoveryServer";
    public static final String DISCOVERY_MESSAGE = "SOCKET-TIC-TAC-TOE-GAME";
    public final int port;
    private DatagramSocket socket;

    public DiscoveryServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(port);
            Log.i(TAG, "DiscoveryServer started on port "+port+" (UDP)");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket rxPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                socket.receive(rxPacket);
                String requestMessage = new String(
                    rxPacket.getData(), 0, rxPacket.getLength()
                );
                Log.d(TAG, "Discovery request received: "+requestMessage);

                // Check if the received request matches the discovery message
                if (requestMessage.equals(DISCOVERY_MESSAGE)) {
                    byte[] sendData = DISCOVERY_MESSAGE.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(
                        sendData, sendData.length,
                        rxPacket.getAddress(), rxPacket.getPort()
                    );
                    Log.d(TAG, "Sending response...");
                    socket.send(sendPacket);
                    Log.d(TAG, "Response sent to "+rxPacket.getAddress()+":"+rxPacket.getPort());
                }
            }
        } catch (IOException e) {
            // This exception is raised on this.close()
            // I couldn't come up with a better Thread close/exit mechanism
            Log.i(TAG, "Closing DiscoveryServer");
            socket.close();
        }
    }

    public void close() {
        socket.close();
    }
}