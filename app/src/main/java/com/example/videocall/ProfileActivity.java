package com.example.videocall;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @NonNull
    private String recieverUserId;

    @NonNull
    private String recieverUserName;

    private TextView username;

    private Button callBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        recieverUserId = getIntent().getExtras().get("user_id").toString();
        recieverUserName = getIntent().getExtras().get("user_name").toString();

        username = findViewById(R.id.profile_user_name);
        callBtn = findViewById(R.id.profile_video_call_btn);

        username.setText(recieverUserName);

    }
}