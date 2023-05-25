package com.vasll.sockettictactoe.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.vasll.sockettictactoe.R;

/** A row that shows the IP of a found game and has a button that can be pressed to join the game */
public class LobbyItemRow extends ConstraintLayout {
    private TextView tvIpAddress;
    private Button btnJoinLobby;

    public LobbyItemRow(@NonNull Context context) {
        super(context);
        initializeViews(context);
    }

    public LobbyItemRow(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LobbyItemRow(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.lobby_item_row, this);

        tvIpAddress = findViewById(R.id.tvIpAddress);
        btnJoinLobby = findViewById(R.id.btnJoinLobby);
    }

    public void setIpAddress(String ipAddress) {
        tvIpAddress.setText(ipAddress);
    }

    public void setOnClickBtnJoinLobbyListener(OnClickListener clickListener) {
        btnJoinLobby.setOnClickListener(clickListener);
    }
}
