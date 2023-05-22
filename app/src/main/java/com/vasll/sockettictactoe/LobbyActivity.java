package com.vasll.sockettictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.vasll.sockettictactoe.R;
import com.vasll.sockettictactoe.databinding.ActivityLobbyBinding;
import com.vasll.sockettictactoe.databinding.ActivityMainBinding;
import com.vasll.sockettictactoe.game.server.DiscoveryServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class LobbyActivity extends AppCompatActivity {
    private ActivityLobbyBinding binding;
    private static final String TAG = "LobbyActivity";
    private static final int responseBufferSize = 1024;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnDiscoverLobbies.setOnClickListener(v ->
            new Thread(this::discoverLobbies).start()
        );
    }

    // TODO This is just some bad temporary code
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

                // TODO move this in other function
                LobbyActivity.this.runOnUiThread(()->{
                    TextView tv = new TextView(this);
                    tv.setText("Ip: "+receivePacket.getAddress().toString());
                    binding.linearLayout.addView(tv);
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