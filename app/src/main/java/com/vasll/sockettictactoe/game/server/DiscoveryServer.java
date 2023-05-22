package com.vasll.sockettictactoe.game.server;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DiscoveryServer extends Thread {
    private static final String TAG = "DiscoveryServer";
    public static final String DISCOVERY_MESSAGE = "SOCKET-TIC-TAC-TOE-GAME";
    public final int port;

    public DiscoveryServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            Log.i(TAG, "DiscoveryServer started on port "+port+" (UDP)");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                socket.receive(receivePacket);
                String requestMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                Log.d(TAG, "Discovery request received: "+requestMessage);

                // Check if the received request matches the discovery message
                if (requestMessage.equals(DISCOVERY_MESSAGE)) {
                    byte[] sendData = DISCOVERY_MESSAGE.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(
                        sendData, sendData.length,
                        receivePacket.getAddress(), receivePacket.getPort()
                    );
                    Log.d(TAG, "Sending response...");
                    socket.send(sendPacket);
                    Log.d(TAG, "Response sent to "+receivePacket.getAddress()+":"+receivePacket.getPort());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}