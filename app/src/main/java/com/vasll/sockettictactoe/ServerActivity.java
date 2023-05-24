package com.vasll.sockettictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.vasll.sockettictactoe.databinding.ActivityLobbyBinding;
import com.vasll.sockettictactoe.databinding.ActivityServerBinding;

public class ServerActivity extends AppCompatActivity {
    private ActivityServerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnStartGame.setOnClickListener(v -> onStartGame());
    }

    private void onStartGame() {
        // TODO implement
        // 1. start the GameActivity by passing the port, round count and source activity
    }
}