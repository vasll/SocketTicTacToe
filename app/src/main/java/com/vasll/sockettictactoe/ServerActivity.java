package com.vasll.sockettictactoe;

import static com.vasll.sockettictactoe.IntentKeys.SERVER_IP;
import static com.vasll.sockettictactoe.IntentKeys.SERVER_PORT;
import static com.vasll.sockettictactoe.IntentKeys.SERVER_ROUND_COUNT;
import static com.vasll.sockettictactoe.IntentKeys.SOURCE_ACTIVITY_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
        int serverPort = Integer.parseInt(binding.etPort.getText().toString());
        int serverMaxRounds = Integer.parseInt(binding.etRounds.getText().toString());

        Intent intent = new Intent(ServerActivity.this, GameActivity.class);
        intent.putExtra(SOURCE_ACTIVITY_NAME, ServerActivity.class.getName());
        intent.putExtra(SERVER_PORT, serverPort);
        intent.putExtra(SERVER_ROUND_COUNT, serverMaxRounds);
        startActivity(intent);
    }
}