package com.example.jads;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    EditText usernameField, passwordField;
    Button loginButton;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView registerNowText = findViewById(R.id.textView);

        // Text to display
        String fullText = "Don't have an account? Sign up >";

        // Create a SpannableString to modify part of the text
        SpannableString spannableString = new SpannableString(fullText);

        // Apply color to the clickable part
        int start = fullText.indexOf("Sign up >");
        int end = start + "Sign up >".length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Make the clickable part clickable
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the text to the TextView
        registerNowText.setText(spannableString);

        // Enable clicking
        registerNowText.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.passwordEt); // Ensure this ID exists in your XML
        loginButton = findViewById(R.id.loginButton);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        loginButton.setOnClickListener(v -> {
            String email = usernameField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                                String userId = firebaseUser.getUid();
                                reference.child(userId).get().addOnCompleteListener(databaseTask -> {
                                    if (databaseTask.isSuccessful() && databaseTask.getResult().exists()) {
                                        DataSnapshot snapshot = databaseTask.getResult();
                                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "User data not found in database", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else if (firebaseUser != null && !firebaseUser.isEmailVerified()) {
                                Toast.makeText(LoginActivity.this, "Please verify your email before logging in", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
