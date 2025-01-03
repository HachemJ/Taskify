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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText emailAddressField, passwordField;
    Button loginButton;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference usersReference, passwordResetRequests, unverifiedUsersReference;

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
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);

        // Set up "Don't have an account? Sign up >"
        String fullText = "Don't have an account? Sign up >";
        SpannableString spannableString = new SpannableString(fullText);

        int start = fullText.indexOf("Sign up >");
        int end = start + "Sign up >".length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        registerNowText.setText(spannableString);
        registerNowText.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

        // Set up "Forgot your password? Click here"
        String forgotPasswordTextFull = "Forgot your password? Click here";
        SpannableString forgotPasswordSpannable = new SpannableString(forgotPasswordTextFull);

        int forgotStart = forgotPasswordTextFull.indexOf("Click here");
        int forgotEnd = forgotStart + "Click here".length();
        forgotPasswordSpannable.setSpan(new ForegroundColorSpan(Color.BLUE), forgotStart, forgotEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        forgotPasswordSpannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                handlePasswordReset();
            }
        }, forgotStart, forgotEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        forgotPasswordText.setText(forgotPasswordSpannable);
        forgotPasswordText.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());

        // Initialize fields and buttons
        emailAddressField = findViewById(R.id.emailAddressEt);
        passwordField = findViewById(R.id.passwordEt);
        loginButton = findViewById(R.id.loginButton);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        unverifiedUsersReference = database.getReference("unverified_users");
        passwordResetRequests = database.getReference("password_reset_requests");

        loginButton.setOnClickListener(v -> {
            String email = emailAddressField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Handle login
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                                // Move verified user from unverified_users to users if necessary
                                moveUserToVerified(firebaseUser.getUid());

                                // Fetch and store the FCM token
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(tokenTask -> {
                                            if (tokenTask.isSuccessful()) {
                                                String fcmToken = tokenTask.getResult();
                                                storeFcmToken(firebaseUser.getUid(), fcmToken);
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Failed to fetch FCM token", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void handlePasswordReset() {
        String email = emailAddressField.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query the "users" node to match the email
        usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Email exists, retrieve userId
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    String userId = userSnapshot.getKey();

                    if (userId != null) {
                        // Check if a reset request already exists for the user
                        passwordResetRequests.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot resetSnapshot) {
                                if (resetSnapshot.exists()) {
                                    long lastResetTimestamp = resetSnapshot.child("timestamp").getValue(Long.class);
                                    if (System.currentTimeMillis() - lastResetTimestamp < 24 * 60 * 60 * 1000) {
                                        Toast.makeText(LoginActivity.this, "You can only reset your password once a day.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                // Send the password reset email
                                auth.sendPasswordResetEmail(email).addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();

                                        // Log the reset request with a timestamp
                                        HashMap<String, Object> resetLog = new HashMap<>();
                                        resetLog.put("email", email);
                                        resetLog.put("timestamp", ServerValue.TIMESTAMP);
                                        passwordResetRequests.child(userId).setValue(resetLog);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Failed to send reset email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(LoginActivity.this, "Error checking reset requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Email is not registered.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error checking email: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void moveUserToVerified(String userId) {
        unverifiedUsersReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    usersReference.child(userId).setValue(snapshot.getValue()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            unverifiedUsersReference.child(userId).removeValue();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Error moving user to verified: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void storeFcmToken(String userId, String token) {
        DatabaseReference userRef = database.getReference("users").child(userId);
        userRef.child("fcmToken").setValue(token)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Token stored successfully
                        Toast.makeText(this, "FCM token updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        // Failed to store token
                        Toast.makeText(this, "Failed to update FCM token", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
