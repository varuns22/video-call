package com.example.videocall;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegistrationActivity extends AppCompatActivity {

    private CountryCodePicker countryCodePicker;

    private EditText code;
    private Button continueBtn;

    private String checker = "";
    private String phoneNumber = "";

    private RelativeLayout relativeLayout;
    private RelativeLayout codeAuthLayout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private FirebaseAuth mAuth;
    private String verificationId;

    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        progressBar = new ProgressDialog(RegistrationActivity.this);

        EditText phoneText = findViewById(R.id.phoneText);
        code = findViewById(R.id.code);
        continueBtn = findViewById(R.id.continueBtn);
        relativeLayout = findViewById(R.id.phoneAuth);
        codeAuthLayout = findViewById(R.id.codeAuth);

        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneText);


        continueBtn.setOnClickListener(view -> {
            if (continueBtn.getText().equals("Submit") || checker.equals("Code Sent")) {
                String verificationCode = code.getText().toString();
                if (verificationCode.equals("")) {
                    Toast.makeText(RegistrationActivity.this, "Enter the Verification Code", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setTitle("Code Verification ");
                    progressBar.setMessage("Please Wait...");
                    progressBar.setCanceledOnTouchOutside(false);
                    progressBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            } else {
                phoneNumber = countryCodePicker.getFullNumberWithPlus();
                if (!phoneNumber.equals("") && !phoneNumber.isEmpty()) {
                    progressBar.setTitle("Verification Code Sent.");
                    progressBar.setMessage("Please Wait...");
                    progressBar.setCanceledOnTouchOutside(false);
                    progressBar.show();
                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)       // Phone number to verify
                            .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(RegistrationActivity.this)
                            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                } else {
                    Toast.makeText(RegistrationActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(RegistrationActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                relativeLayout.setVisibility(View.VISIBLE);
                continueBtn.setText(R.string.continue_text);
                code.setVisibility(View.GONE);
                codeAuthLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                verificationId = s;

                relativeLayout.setVisibility(View.GONE);
                checker = "Code Sent";
                continueBtn.setText(R.string.submit);
                code.setVisibility(View.VISIBLE);
                codeAuthLayout.setVisibility(View.VISIBLE);
                progressBar.dismiss();
                Toast.makeText(RegistrationActivity.this, "Code Sent", Toast.LENGTH_SHORT).show();
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            Intent homeIntent = new Intent(RegistrationActivity.this, ContactsActivity.class);
            startActivity(homeIntent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.dismiss();
                        Toast.makeText(RegistrationActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        sendUserToMainActivity();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(RegistrationActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }
}


