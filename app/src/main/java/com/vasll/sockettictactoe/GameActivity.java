package com.vasll.sockettictactoe;

import static com.vasll.sockettictactoe.IntentKeys.SERVER_IP;
import static com.vasll.sockettictactoe.IntentKeys.SERVER_PORT;
import static com.vasll.sockettictactoe.IntentKeys.SOURCE_ACTIVITY_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.vasll.sockettictactoe.databinding.ActivityGameBinding;
import com.vasll.sockettictactoe.game.client.GameClient;
import com.vasll.sockettictactoe.game.listeners.GameListener;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;
    private final Intent intent = getIntent();
    private GameClient gameClient;
    private ArrayList<ArrayList<Button>> btnBoard = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fillButtonBoard();

        String sourceActivityName = intent.getStringExtra(SOURCE_ACTIVITY_NAME);
        if(sourceActivityName.equals(LobbyActivity.class.getName())) {
            startFromLobbyActivity();
        }else if(sourceActivityName.equals(ServerActivity.class.getName())) {
            // TODO implement
        }
    }

    /**
     * Starts the game from the LobbyActivity
     * In this case the user wants to join a GameServer that already exists.
     * Attempt a connection to the GameServer with a GameClient on the given IP, PORT from the
     * source activity
     */
    private void startFromLobbyActivity() {
        String serverIp = getIntent().getStringExtra(SERVER_IP);
        int serverPort = getIntent().getIntExtra(SERVER_PORT, 8888);

        gameClient = new GameClient(serverIp, serverPort);

        gameClient.addBoardUpdateListener((board, nextTurnPlayerId) -> {
            runOnUiThread(() -> updateBoard(board));
        });

        gameClient.addRoundListener((player1Score, player2Score, currentRoundCount) -> {
            // TODO also update round count
        });

        gameClient.addGameListener(new GameListener() {
            @Override
            public void onGameStart(int yourPlayerId, int enemyPlayerId, int maxRounds) {

            }

            @Override
            public void onGameEnd(int player1Score, int player2Score) {

            }
        });


        gameClient.start();
    }

    private void updateBoard(char[][] board) {
        for(int row=0; row<3; row++){
            for(int col=0; col<3; col++){
                btnBoard.get(row).get(col).setText(
                    String.valueOf(board[row][col])
                );
            }
        }
    }

    private void updateScores(int player1Score) {

    }



    private void fillButtonBoard() {
        ArrayList<Button> row0 = new ArrayList<>();
        row0.add(binding.btnBoard1);
        row0.add(binding.btnBoard2);
        row0.add(binding.btnBoard3);
        ArrayList<Button> row1 = new ArrayList<>();
        row1.add(binding.btnBoard4);
        row1.add(binding.btnBoard5);
        row1.add(binding.btnBoard6);
        ArrayList<Button> row2 = new ArrayList<>();
        row2.add(binding.btnBoard7);
        row2.add(binding.btnBoard8);
        row2.add(binding.btnBoard9);
        btnBoard.add(row0);
        btnBoard.add(row1);
        btnBoard.add(row2);
    }
}