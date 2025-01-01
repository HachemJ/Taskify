package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ViewProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference usersReference, passwordResetRequests;

    EditText emailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_profile);

        emailField = findViewById(R.id.emailaddressEt);
        auth = FirebaseAuth.getInstance(); // Ensure auth is initialized
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        passwordResetRequests = database.getReference("password_reset_requests");

        findViewById(R.id.resetPasswordButton).setOnClickListener(v -> handlePasswordReset());
    }

    private void handlePasswordReset() {
        String email = emailField.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Null check for usersReference and auth
        if (usersReference == null || auth == null) {
            Toast.makeText(this, "Firebase services are not initialized. Please try again later.", Toast.LENGTH_SHORT).show();
            return;
        }

        usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    String userId = userSnapshot.getKey();

                    if (userId != null) {
                        passwordResetRequests.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot resetSnapshot) {
                                if (resetSnapshot.exists()) {
                                    Long lastResetTimestamp = resetSnapshot.child("timestamp").getValue(Long.class);
                                    if (lastResetTimestamp != null && System.currentTimeMillis() - lastResetTimestamp < 24 * 60 * 60 * 1000) {
                                        Toast.makeText(ViewProfileActivity.this, "You can only reset your password once a day.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else {
                                    // Log the reset request
                                    HashMap<String, Object> resetLog = new HashMap<>();
                                    resetLog.put("timestamp", System.currentTimeMillis());
                                    passwordResetRequests.child(userId).setValue(resetLog);

                                    // Send the password reset email
                                    auth.sendPasswordResetEmail(email).addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(ViewProfileActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(ViewProfileActivity.this, "Failed to send reset email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ViewProfileActivity.this, "Error checking reset requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(ViewProfileActivity.this, "Error: User not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProfileActivity.this, "Error checking user: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
