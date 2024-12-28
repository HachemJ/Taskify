package com.example.jads;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WaitingForVerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_verification);

        // Show a message to the user while waiting for verification
        Toast.makeText(this, "Please check your email for the verification link.", Toast.LENGTH_LONG).show();
    }
}
