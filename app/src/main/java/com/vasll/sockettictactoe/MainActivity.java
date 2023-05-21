package com.vasll.sockettictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.vasll.sockettictactoe.databinding.ActivityMainBinding;
import com.vasll.sockettictactoe.game.client.Client;
import com.vasll.sockettictactoe.game.server.Server;
import com.vasll.sockettictactoe.game.logic.Move;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private Server gameServer;
    private Client gameClient1, gameClient2;
    private ArrayList<ArrayList<Button>> btnGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadBtnGrid();  // Load UI components

        // Add listeners
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
        gameClient1.addBoardUiUpdateListener(this::updateBoard);
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

    private void updateBoard(char[][] board){
        for(int row=0; row<3; row++){
            for(int col=0; col<3; col++){
                btnGrid.get(row).get(col).setText(
                    String.valueOf(board[row][col])
                );
            }
        }
    }

    private void loadBtnGrid(){
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
        btnGrid = new ArrayList<>();
        btnGrid.add(btnRow0);
        btnGrid.add(btnRow1);
        btnGrid.add(btnRow2);
    }
}
