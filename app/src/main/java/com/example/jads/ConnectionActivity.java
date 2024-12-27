package com.example.jads;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.content.*;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConnectionActivity extends AppCompatActivity {

    Button loginButton;
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

        TextView registerNowText = findViewById(R.id.textView);

        // Text to display
        String fullText = "New to La Secte? Register Now >";

        // Create a SpannableString to modify part of the text
        SpannableString spannableString = new SpannableString(fullText);

        // Apply color to the clickable part
        int start = fullText.indexOf("Register Now >");
        int end = start + "Register Now >".length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Make the clickable part clickable
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(ConnectionActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the text to the TextView
        registerNowText.setText(spannableString);

        // Enable clicking
        registerNowText.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConnectionActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}