package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase references
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        passwordResetRequests = database.getReference("password_reset_requests");

        // Trigger password reset functionality
        handlePasswordReset();
    }

    private void handlePasswordReset() {
        // Query the database for the user associated with the email
        usersReference.orderByChild("email").equalTo(getIntent().getStringExtra("email")).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        Toast.makeText(ViewProfileActivity.this, "You can only reset your password once a day.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                // Send the password reset email
                                auth.sendPasswordResetEmail(getIntent().getStringExtra("email")).addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        Toast.makeText(ViewProfileActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();

                                        // Log the reset request with a timestamp
                                        HashMap<String, Object> resetLog = new HashMap<>();
                                        resetLog.put("timestamp", System.currentTimeMillis());
                                        passwordResetRequests.child(userId).setValue(resetLog);

                                        // Redirect to MainActivity
                                        startActivity(new Intent(ViewProfileActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(ViewProfileActivity.this, "Failed to send reset email: " + emailTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
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
