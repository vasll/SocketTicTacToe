package com.vasll.sockettictactoe;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.vasll.sockettictactoe.databinding.ActivityMainBinding;
import com.vasll.sockettictactoe.databinding.ActivityTestBinding;
import com.vasll.sockettictactoe.game.client.GameClient;
import com.vasll.sockettictactoe.game.logic.Move;
import com.vasll.sockettictactoe.game.server.GameServer;

import java.util.ArrayList;


// TODO this is here just for debugging
public class TestActivity extends AppCompatActivity {
    private final static String TAG = "TestActivity";
    private ActivityTestBinding binding;
    private GameServer gameServer;
    private GameClient gameClient1, gameClient2;
    private ArrayList<ArrayList<Button>> btnGridClient1, btnGridClient2;

    private int currentRoundCount = 0;
    private int player1Score = 0;
    private int player2Score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load UI components
        loadBtnGridClient1();
        loadBtnGridClient2();

        // Add listeners
        binding.btnStartServer.setOnClickListener(v -> onBtnStartServer());
        binding.btnStartClient1.setOnClickListener(v -> onBtnStartClient1());
        binding.btnStartClient2.setOnClickListener(v -> onBtnStartClient2());
        binding.btnOpenLobbyActivity.setOnClickListener(v -> {
            startActivity(new Intent(TestActivity.this, LobbyActivity.class));
        });

        new Thread(() -> {
            try {
                while(true){
                    Log.d(TAG, "Active thread count: "+Thread.activeCount());
                    sleep(1000);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void onBtnStartServer() {
        int port = Integer.parseInt(binding.etPort.getText().toString());
        gameServer = new GameServer(port, 1);
        gameServer.start();
    }

    private void onBtnStartClient1() {
        int port = Integer.parseInt(binding.etPort.getText().toString());
        String ip = binding.etIP.getText().toString();
        gameClient1 = new GameClient(ip, port);
        gameClient1.addBoardUpdateListener((board, nextTurnPlayerId) -> {
            runOnUiThread(() -> {
                updateBoard(board, btnGridClient1);
            });
        });

        gameClient1.addRoundListener((player1Score, player2Score, currentRoundCount) -> {
            this.currentRoundCount = currentRoundCount;
            this.player1Score = player1Score;
            this.player2Score = player2Score;
            runOnUiThread(() ->
                    Toast.makeText(this, "Round "+currentRoundCount, Toast.LENGTH_SHORT).show()
            );
        });

        gameClient1.addGameListener((player1Score, player2Score) -> {
            this.player1Score = player1Score;
            this.player2Score = player2Score;
            Log.i(TAG, "The game has ended!");
            runOnUiThread(() ->
                    Toast.makeText(this, "Game has ended!", Toast.LENGTH_SHORT).show()
            );
            // TODO somehow close the resources
        });
        gameClient1.start();
        bindListenersToClient(gameClient1, btnGridClient1);
    }

    private void onBtnStartClient2() {
        int port = Integer.parseInt(binding.etPort.getText().toString());
        String ip = binding.etIP.getText().toString();
        gameClient2 = new GameClient(ip, port);
        gameClient2.addBoardUpdateListener((board, nextTurnPlayerId) -> {
            runOnUiThread(() -> {
                updateBoard(board, btnGridClient2);
            });
        });
        gameClient2.start();
        bindListenersToClient(gameClient2, btnGridClient2);
    }

    private void updateBoard(char[][] board, ArrayList<ArrayList<Button>> btnUiGrid){
        for(int row=0; row<3; row++){
            for(int col=0; col<3; col++){
                btnUiGrid.get(row).get(col).setText(
                        String.valueOf(board[row][col])
                );
            }
        }
    }

    /* This is some boilerplate */
    private void loadBtnGridClient1(){
        ArrayList<Button> btnRow0 = new ArrayList<>();
        btnRow0.add(binding.btnTable0);
        btnRow0.add(binding.btnTable1);
        btnRow0.add(binding.btnTable2);
        ArrayList<Button> btnRow1 = new ArrayList<>();
        btnRow1.add(binding.btnTable3);
        btnRow1.add(binding.btnTable4);
        btnRow1.add(binding.btnTable5);
        ArrayList<Button> btnRow2 = new ArrayList<>();
        btnRow2.add(binding.btnTable6);
        btnRow2.add(binding.btnTable7);
        btnRow2.add(binding.btnTable8);
        btnGridClient1 = new ArrayList<>();
        btnGridClient1.add(btnRow0);
        btnGridClient1.add(btnRow1);
        btnGridClient1.add(btnRow2);
    }

    /* This is some boilerplate */
    private void loadBtnGridClient2(){
        ArrayList<Button> btnRow0 = new ArrayList<>();
        btnRow0.add(binding.btnTable9);
        btnRow0.add(binding.btnTable10);
        btnRow0.add(binding.btnTable11);
        ArrayList<Button> btnRow1 = new ArrayList<>();
        btnRow1.add(binding.btnTable12);
        btnRow1.add(binding.btnTable13);
        btnRow1.add(binding.btnTable14);
        ArrayList<Button> btnRow2 = new ArrayList<>();
        btnRow2.add(binding.btnTable15);
        btnRow2.add(binding.btnTable16);
        btnRow2.add(binding.btnTable17);
        btnGridClient2 = new ArrayList<>();
        btnGridClient2.add(btnRow0);
        btnGridClient2.add(btnRow1);
        btnGridClient2.add(btnRow2);
    }

    /* Other boilerplate yes */
    private void bindListenersToClient(
            GameClient gameClient, ArrayList<ArrayList<Button>> btnUiGrid
    ) {
        for(int row=0; row<3; row++){
            for(int col=0; col<3; col++){
                int finalRow = row;
                int finalCol = col;
                btnUiGrid.get(row).get(col).setOnClickListener(v -> {
                    gameClient.makeMove(new Move(finalRow, finalCol));
                });
            }
        }
    }
}