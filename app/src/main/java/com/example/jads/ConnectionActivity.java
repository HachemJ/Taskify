package com.example.jads;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ConnectionActivity extends AppCompatActivity {

    Button loginButton, guestButton;
    TextView registerNowText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        registerNowText = findViewById(R.id.textView);
        setUpClickableText();
        TextView guestTextView = findViewById(R.id.guestTextView); // Reference new TextView
        setUpGuestClickableText(guestTextView);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(ConnectionActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Check if the user is already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User is already signed in, redirect to MainActivity
            startActivity(new Intent(ConnectionActivity.this, MainActivity.class));
            finish();
        }
    }

    private void setUpClickableText() {
        String fullText = "New to Taskify? Register Now >";
        SpannableString spannableString = new SpannableString(fullText);
        int start = fullText.indexOf("Register Now >");
        int end = start + "Register Now >".length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(ConnectionActivity.this, SignupActivity.class));
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        registerNowText.setText(spannableString);
        registerNowText.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }
    private void setUpGuestClickableText(TextView guestTextView) {
        String fullText = "Enter as a guest";
        SpannableString spannableString = new SpannableString(fullText);
        int start = fullText.indexOf("guest");
        int end = start + "guest".length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(ConnectionActivity.this, MainActivity.class);
                intent.putExtra("isGuest", true); // Pass a flag indicating guest mode
                startActivity(intent);
                finish(); // Close ConnectionActivity
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        guestTextView.setText(spannableString);
        guestTextView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }
}
