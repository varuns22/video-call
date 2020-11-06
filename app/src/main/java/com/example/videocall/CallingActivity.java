package com.example.videocall;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {

    private TextView contactName;
    private ImageView declineVideoCallBtn, acceptVideoCallBtn;

    private String receiverUserId = "", receiverUserName = "";
    private String callerUserId = "", callerUserName = "";

    private DatabaseReference usersRef;

    private boolean declineCallButtonClicked = false;

    private String callingId = "", ringingdId = "";

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        callerUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("calling_user_id").toString();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);

        contactName = findViewById(R.id.calling_name);
        declineVideoCallBtn = findViewById(R.id.decline_video_call);
        acceptVideoCallBtn = findViewById(R.id.accept_video_call);

        declineVideoCallBtn.setOnClickListener(view -> {
            mediaPlayer.stop();
            declineCallButtonClicked = true;
            declineCall();
        });

        acceptVideoCallBtn.setOnClickListener(view -> {

            mediaPlayer.stop();

            final HashMap<String, Object> callingPickUpMap = new HashMap<>();
            callingPickUpMap.put("callAccepted", "callAccepted");

            usersRef.child(callerUserId).child("Ringing").updateChildren(callingPickUpMap).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(CallingActivity.this, VideoCallActivity.class);
                    startActivity(intent);
                }
            });
        });

        getAndSetUserProfileInfo();

    }


    private void declineCall() {

        // Decline Call from Caller Side

        usersRef.child(callerUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("callToUser")) {

                    callingId = snapshot.child("callToUser").getValue().toString();

                    usersRef.child(callingId).child("Ringing").removeValue().addOnCompleteListener(task -> usersRef.child(callerUserId).child("Calling").removeValue().addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful()) {
                            startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                            finish();
                        }
                    }));

                } else {
                    startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //Decline Call from Receiver Side

        usersRef.child(callerUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("callFromUser")) {

                    ringingdId = snapshot.child("callFromUser").getValue().toString();

                    usersRef.child(ringingdId).child("Calling").removeValue().addOnCompleteListener(task -> usersRef.child(callerUserId).child("Ringing").removeValue().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                            finish();
                        }
                    }));

                } else {
                    startActivity(new Intent(CallingActivity.this, RegistrationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAndSetUserProfileInfo() {

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(receiverUserId).exists()) {
                    receiverUserName = snapshot.child(receiverUserId).child("name").getValue().toString();
                    contactName.setText(receiverUserName);
                }

                if (snapshot.child(callerUserId).exists()) {
                    callerUserName = snapshot.child(callerUserId).child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        usersRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!declineCallButtonClicked && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing")) {

                    mediaPlayer.start();

                    final HashMap<String, Object> callInfo = new HashMap<>();
                    callInfo.put("uid", callerUserId);
                    callInfo.put("name", callerUserName);
                    callInfo.put("callToUser", receiverUserId);

                    usersRef.child(callerUserId).child("Calling").updateChildren(callInfo).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            final HashMap<String, Object> receiverCallInfo = new HashMap<>();
                            receiverCallInfo.put("callFromUser", callerUserId);

                            usersRef.child(receiverUserId).child("Ringing").updateChildren(receiverCallInfo);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(callerUserId).hasChild("Ringing") && !snapshot.child(callerUserId).hasChild("Calling")) {
                    acceptVideoCallBtn.setVisibility(View.VISIBLE);
                }

                if (snapshot.child(receiverUserId).child("Ringing").hasChild("callAccepted")) {
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this, VideoCallActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}