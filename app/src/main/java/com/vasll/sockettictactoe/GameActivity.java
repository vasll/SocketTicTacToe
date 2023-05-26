package com.vasll.sockettictactoe;

import static com.vasll.sockettictactoe.IntentKeys.SERVER_IP;
import static com.vasll.sockettictactoe.IntentKeys.SERVER_PORT;
import static com.vasll.sockettictactoe.IntentKeys.SERVER_ROUND_COUNT;
import static com.vasll.sockettictactoe.IntentKeys.SOURCE_ACTIVITY_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.vasll.sockettictactoe.databinding.ActivityGameBinding;
import com.vasll.sockettictactoe.game.client.GameClient;
import com.vasll.sockettictactoe.game.listeners.GameListener;
import com.vasll.sockettictactoe.game.logic.Move;
import com.vasll.sockettictactoe.game.server.GameServer;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {
    // private final static String TAG = "MainActivity";
    private ActivityGameBinding binding;
    private GameClient gameClient;
    private GameServer gameServer;
    private final ArrayList<ArrayList<Button>> btnBoard = new ArrayList<>();
    private int clientPlayerId;
    private int maxRounds;
    private int scoreClientPlayer = 0, scoreEnemyPlayer = 0;
    private int lossesClientPlayer = 0, lossesEnemyPlayer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fillButtonBoard();
        setEnabledButtonBoard(false);  // Disable all buttons

        String sourceActivityName = getIntent().getStringExtra(SOURCE_ACTIVITY_NAME);
        if(sourceActivityName.equals(LobbyActivity.class.getName())) {
            startFromLobbyActivity();
        }else if(sourceActivityName.equals(ServerActivity.class.getName())) {
            startFromServerActivity();
        }
    }

    @Override
    protected void onDestroy() {
        if (gameClient!=null){
            gameClient.close();
        }
        if (gameServer!=null){
            gameServer.close();
        }
        super.onDestroy();
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
        bindGameClient(serverIp, serverPort);
    }

    private void startFromServerActivity() {
        int serverPort = getIntent().getIntExtra(SERVER_PORT, 8888);
        int serverMaxRounds = getIntent().getIntExtra(SERVER_ROUND_COUNT, 3);

        gameServer = new GameServer(serverPort, serverMaxRounds);
        gameServer.start();
        bindGameClient("localhost", serverPort);
    }

    private void bindGameClient(String serverIp, int serverPort) {
        gameClient = new GameClient(serverIp, serverPort);

        gameClient.addBoardUpdateListener((board, nextTurnPlayerId) -> {
            runOnUiThread(() -> updateBoard(board));
            if(nextTurnPlayerId==clientPlayerId){
                binding.tvClientId.setTextColor(Color.GREEN);
                binding.tvEnemyId.setTextColor(Color.GRAY);
                runOnUiThread(() -> setEnabledButtonBoard(true));
            } else {
                binding.tvClientId.setTextColor(Color.GRAY);
                binding.tvEnemyId.setTextColor(Color.GREEN);
                runOnUiThread(() -> setEnabledButtonBoard(false));
            }
        });

        gameClient.addRoundListener((player1Score, player2Score, currentRoundCount) -> {
            // Updates the player scores and round count
            // This is bad code but whatever
            runOnUiThread(()-> {
                if(clientPlayerId==1){
                    if(player1Score > scoreClientPlayer) {
                        lossesEnemyPlayer += 1;
                    }
                    if(player2Score > scoreEnemyPlayer) {
                        lossesClientPlayer += 1;

                    }
                    scoreClientPlayer = player1Score;
                    scoreEnemyPlayer += player2Score;

                    binding.tvClientWins.setText("W: "+player1Score);
                    binding.tvEnemyWins.setText("W: "+player2Score);
                    binding.tvClientLosses.setText("L: "+lossesClientPlayer);
                    binding.tvEnemyLosses.setText("L: "+lossesEnemyPlayer);
                }else if(clientPlayerId ==2){
                    if(player2Score > scoreClientPlayer) {
                        lossesEnemyPlayer += 1;
                    }
                    if(player1Score > scoreEnemyPlayer) {
                        lossesClientPlayer += 1;
                    }
                    scoreClientPlayer = player2Score;
                    scoreEnemyPlayer += player1Score;

                    binding.tvClientWins.setText("W: "+player2Score);
                    binding.tvEnemyWins.setText("W: "+player1Score);
                    binding.tvClientLosses.setText("L: "+lossesClientPlayer);
                    binding.tvEnemyLosses.setText("L: "+lossesEnemyPlayer);
                }
                binding.tvRoundCount.setText("Round "+(currentRoundCount+1)+"/"+maxRounds);
            });
        });

        gameClient.addGameListener(new GameListener() {
            @Override
            public void onGameStart(int yourPlayerId, int enemyPlayerId, int maxRounds) {
                clientPlayerId = yourPlayerId;
                GameActivity.this.maxRounds = maxRounds;
                runOnUiThread(()-> {
                    addListenersToBoard();
                    binding.tvClientId.setText("P"+ clientPlayerId);
                    binding.tvEnemyId.setText("P"+ enemyPlayerId);
                    binding.tvRoundCount.setText("Round 1/"+maxRounds);
                });
            }

            @Override
            public void onGameEnd(int player1Score, int player2Score) {
                runOnUiThread(()-> {
                    if(clientPlayerId ==1){
                        binding.tvClientWins.setText("W: "+player1Score);
                        binding.tvEnemyWins.setText("W: "+player2Score);
                    }else if(clientPlayerId ==2){
                        binding.tvClientWins.setText("W: "+player2Score);
                        binding.tvEnemyWins.setText("W: "+player1Score);
                    }
                    binding.tvRoundCount.setText("Round "+maxRounds+"/"+maxRounds);

                    if(player1Score>player2Score) {
                        Toast.makeText(GameActivity.this, "P1 has won!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(GameActivity.this, "P2 has won!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onEnemyDisconnect() {
                setEnabledButtonBoard(false);
                Toast.makeText(GameActivity.this, "Enemy has quit the game.", Toast.LENGTH_LONG)
                    .show();
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

    private void addListenersToBoard() {
        for(int row=0; row<3; row++){
            for(int col=0; col<3; col++){
                int finalRow = row;
                int finalCol = col;
                btnBoard.get(row).get(col).setOnClickListener(v -> {
                    gameClient.makeMove(new Move(finalRow, finalCol));
                });
            }
        }
    }

    private void setEnabledButtonBoard(boolean enabled) {
        for(int row=0; row<3; row++){
            for(int col=0; col<3; col++){
                btnBoard.get(row).get(col).setEnabled(enabled);
            }
        }
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