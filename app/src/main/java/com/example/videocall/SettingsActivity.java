package com.example.videocall;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    BottomNavigationView navView;

    private EditText userNameTxt;

    private DatabaseReference userRef;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        Button saveBtn = findViewById(R.id.save_settings_btn);
        userNameTxt = findViewById(R.id.settings_user_name);

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        progressDialog = new ProgressDialog(this);

        saveBtn.setOnClickListener(view -> saveUser());

        getUserInfo();
    }

    private void saveUser() {
        final String userName = userNameTxt.getText().toString();

        if (!userName.equals("")) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    progressDialog.setTitle("Account Settings");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();
                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                    userMap.put("name", userName);

                    userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(userMap).addOnCompleteListener((task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                            startActivity(intent);
                            finish();

                            progressDialog.dismiss();

                            Toast.makeText(SettingsActivity.this, "User Name has been  successfully updated.", Toast.LENGTH_SHORT).show();
                        }
                    }));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Toast.makeText(SettingsActivity.this, "User Name is Required", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserInfo() {
        userRef.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userNameFromDB = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    userNameTxt.setText(userNameFromDB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = item -> {

        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent mainIntent = new Intent(SettingsActivity.this, ContactsActivity.class);
                startActivity(mainIntent);
                break;

            case R.id.navigation_settings:
                Intent settingsIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.navigation_logout:
                FirebaseAuth.getInstance().signOut();
                Intent logoutIntent = new Intent(SettingsActivity.this, RegistrationActivity.class);
                startActivity(logoutIntent);
                finish();
                break;

            default:
        }
        return true;
    };
}