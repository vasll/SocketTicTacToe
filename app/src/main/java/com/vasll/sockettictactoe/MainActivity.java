package com.vasll.sockettictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.vasll.sockettictactoe.databinding.ActivityMainBinding;
import com.vasll.sockettictactoe.game.client.Client;
import com.vasll.sockettictactoe.game.server.Server;
import com.vasll.sockettictactoe.game.logic.Move;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Server gameServer;
    private Client gameClient1, gameClient2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnStartServer.setOnClickListener(v -> onBtnStartServer());
        binding.btnStartClient1.setOnClickListener(v -> onBtnStartClient1());
        binding.btnStartClient2.setOnClickListener(v -> onBtnStartClient2());
        binding.btnMakeMove.setOnClickListener(v -> makeMove());
    }

    private void onBtnStartServer() {
        int port = Integer.parseInt(binding.etPort.getText().toString());
        gameServer = new Server(port);
        gameServer.start();
    }

    private void onBtnStartClient1() {
        int port = Integer.parseInt(binding.etPort.getText().toString());
        String ip = binding.etIP.getText().toString();
        gameClient1 = new Client(ip, port);
        gameClient1.start();
    }

    private void onBtnStartClient2() {
        int port = Integer.parseInt(binding.etPort.getText().toString());
        String ip = binding.etIP.getText().toString();
        gameClient2 = new Client(ip, port);
        gameClient2.start();
    }

    private void makeMove(){
        gameClient1.makeMove(new Move(1, 1));
        gameClient2.makeMove(new Move(0, 0));
    }
}
