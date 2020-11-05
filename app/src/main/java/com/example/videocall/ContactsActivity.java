package com.example.videocall;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.StringUtils;

public class ContactsActivity extends AppCompatActivity {

    BottomNavigationView navView;

    RecyclerView contactList;

    DatabaseReference usersRef;

    private String currentUserId;

    FirebaseAuth auth;

    private String userId;

    private String calledByUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        contactList = findViewById(R.id.contacts_list);
        contactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

    }

    @Override
    protected void onStart() {
        super.onStart();

        checkReceivingCall();

        validateUser();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(usersRef, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ContactsActivity.ContactViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, ContactsActivity.ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsActivity.ContactViewHolder holder, int position, @NonNull Contacts model) {
                final String callingUserId = getRef(position).getKey();

                usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        holder.videoCallBtn.setOnClickListener(view -> {
                            Intent callingIntent = new Intent(ContactsActivity.this, CallingActivity.class);
                            callingIntent.putExtra("calling_user_id", callingUserId);
                            startActivity(callingIntent);
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                holder.userName.setText(model.getName());
                holder.itemView.setOnClickListener(view -> {
                    userId = getRef(position).getKey();
                    if (StringUtils.equals(currentUserId, userId)) {
                        Intent intent = new Intent(ContactsActivity.this, ProfileActivity.class);
                        intent.putExtra("user_id", userId);
                        intent.putExtra("user_name", model.getName());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public ContactsActivity.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
                return new ContactsActivity.ContactViewHolder(view);
            }
        };
        contactList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        ImageView videoCallBtn;
        ImageView profileImage;
        RelativeLayout cardView;

        public ContactViewHolder(@NonNull View view) {
            super(view);

            userName = view.findViewById(R.id.contact_name);
            videoCallBtn = view.findViewById(R.id.video_call_btn);
            profileImage = view.findViewById(R.id.contact_image);
            cardView = view.findViewById(R.id.contact_card_view);
        }
    }


    @SuppressLint("NonConstantResourceId")
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = item -> {

        switch (item.getItemId()) {
            case R.id.navigation_home:
                Intent mainIntent = new Intent(ContactsActivity.this, ContactsActivity.class);
                startActivity(mainIntent);
                break;

            case R.id.navigation_settings:
                Intent settingsIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.navigation_logout:
                FirebaseAuth.getInstance().signOut();
                Intent logoutIntent = new Intent(ContactsActivity.this, RegistrationActivity.class);
                startActivity(logoutIntent);
                finish();
                break;

            default:
        }
        return true;
    };

    private void validateUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference();

        databaseReference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Intent settingIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    startActivity(settingIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkReceivingCall() {

        usersRef.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("Ringing")) {
                    calledByUser = snapshot.child("callFromUser").getValue().toString();
                    Intent callingIntent = new Intent(ContactsActivity.this, CallingActivity.class);
                    callingIntent.putExtra("calling_user_id", calledByUser);
                    startActivity(callingIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}