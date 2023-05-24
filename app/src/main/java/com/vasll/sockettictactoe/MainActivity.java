package com.vasll.sockettictactoe;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.vasll.sockettictactoe.databinding.ActivityMainBinding;
import com.vasll.sockettictactoe.game.client.GameClient;
import com.vasll.sockettictactoe.game.logic.Move;
import com.vasll.sockettictactoe.game.server.GameServer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnJoinGame.setOnClickListener(v -> onJoinGame());
        binding.btnCreateGame.setOnClickListener(v -> onCreateGame());
    }

    private void onJoinGame() {
        startActivity(new Intent(MainActivity.this, LobbyActivity.class));
    }

    private void onCreateGame() {
        startActivity(new Intent(MainActivity.this, ServerActivity.class));
    }
}
