package com.vasll.sockettictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.vasll.sockettictactoe.R;
import com.vasll.sockettictactoe.databinding.ActivityLobbyBinding;
import com.vasll.sockettictactoe.databinding.ActivityMainBinding;
import com.vasll.sockettictactoe.game.server.DiscoveryServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class LobbyActivity extends AppCompatActivity {
    private ActivityLobbyBinding binding;
    private static final String TAG = "LobbyActivity";
    private static final int responseBufferSize = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TODO udp discovery on port 8888
    }

    // TODO pasted in from another project
    private void discoverLobbies(){
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            byte[] sendData = DiscoveryServer.DISCOVERY_MESSAGE.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                sendData, sendData.length,
                InetAddress.getByName("255.255.255.255"), 8888
            );
            socket.send(sendPacket);
            Log.d(TAG, "Discovery message sent");

            byte[] receiveBuffer = new byte[responseBufferSize];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, responseBufferSize);

            // Set a timeout to wait for response (2 seconds in this example)
            socket.setSoTimeout(2000);

            try {
                socket.receive(receivePacket);
                String responseMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                Log.d(TAG, "Received response: " + responseMessage);
            } catch (IOException e) {
                Log.d(TAG, "No response received.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}