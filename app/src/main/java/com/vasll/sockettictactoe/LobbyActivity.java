package com.vasll.sockettictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.vasll.sockettictactoe.databinding.ActivityLobbyBinding;
import com.vasll.sockettictactoe.game.server.DiscoveryServer;
import com.vasll.sockettictactoe.ui.LobbyItemRow;

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
        binding.btnRefreshLobbies.setOnClickListener(v -> {
            new Thread(this::refreshLobbies).start();
        });
    }

    // TODO This code is bad
    private void refreshLobbies(){
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

            // Set a timeout to wait for response (1.5 seconds in this example)
            socket.setSoTimeout(1500);

            try {
                socket.receive(receivePacket);
                String hostIp = receivePacket.getAddress().getHostAddress();

                LobbyActivity.this.runOnUiThread(()->{
                    binding.linearLayout.removeAllViews();
                    LobbyItemRow lobbyItemRow = new LobbyItemRow(this);
                    lobbyItemRow.setIpAddress(hostIp);
                    binding.linearLayout.addView(lobbyItemRow);
                });

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