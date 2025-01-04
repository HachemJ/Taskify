package com.example.jads;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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

    private static final String TAG = "ConnectionActivity";

    Button loginButton;
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

        // Handle notification redirection
        if (getIntent().getExtras() != null) {
            String userId = getIntent().getExtras().getString("userId");
            String chatId = getIntent().getExtras().getString("chatId");
            Log.d(TAG, "Notification intent received, redirecting to ChatsActivity with userId: " + userId);
            redirectToChats(userId, chatId);
            return; // Stop further execution
        }

        // Check if the user is already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Log.d(TAG, "User already signed in, redirecting to MainActivity.");
            startActivity(new Intent(ConnectionActivity.this, MainActivity.class));
            finish();
            return;
        }

        // Initialize UI components
        registerNowText = findViewById(R.id.textView);
        setUpClickableText();

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(ConnectionActivity.this, LoginActivity.class);
            startActivity(intent);
        });
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

    private void redirectToChats(String userId, String chatId) {
        Intent intent = new Intent(ConnectionActivity.this, ChatsActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("chatId", chatId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}