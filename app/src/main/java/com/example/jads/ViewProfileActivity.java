package com.example.jads;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ViewProfileActivity extends AppCompatActivity {

    // Firebase references
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersReference, passwordResetRequests;

    // UI elements
    private EditText emailField, firstNameField, lastNameField;
    private TextView fullNameTv, reviewScoreTv;
    private RatingBar ratingBar;
    private Button resetPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // Initialize UI elements
        fullNameTv = findViewById(R.id.fullNameTv);
        emailField = findViewById(R.id.emailAddressEt);
        firstNameField = findViewById(R.id.firstnameEt);
        lastNameField = findViewById(R.id.lastnameEt);
        reviewScoreTv = findViewById(R.id.reviewScoreTv);
        ratingBar = findViewById(R.id.ratingBar);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        // Initialize Firebase references
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        passwordResetRequests = database.getReference("password_reset_requests");

        // Set RatingBar value dynamically
        try {
            float rating = Float.parseFloat(reviewScoreTv.getText().toString());
            ratingBar.setRating(rating);
        } catch (NumberFormatException e) {
            ratingBar.setRating(0); // Default to 0 if parsing fails
        }

        // Fetch user data from Firebase
        fetchUserData();

        // Handle password reset
        resetPasswordButton.setOnClickListener(v -> handlePasswordReset());
    }

    private void fetchUserData() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();

        usersReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch the data from Firebase
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    // Populate fields
                    firstNameField.setText(firstName != null ? firstName : "");
                    lastNameField.setText(lastName != null ? lastName : "");
                    emailField.setText(email != null ? email : "");

                    // Update fullName TextView
                    String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                    fullNameTv.setText(fullName.trim().isEmpty() ? "N/A" : fullName.trim());
                } else {
                    Toast.makeText(ViewProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProfileActivity.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePasswordReset() {
        String email = emailField.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
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
                                }

                                // Log the reset request
                                HashMap<String, Object> resetLog = new HashMap<>();
                                resetLog.put("timestamp", ServerValue.TIMESTAMP);
                                resetLog.put("email", email);
                                passwordResetRequests.child(userId).setValue(resetLog);

                                // Send password reset email
                                auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ViewProfileActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ViewProfileActivity.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ViewProfileActivity.this, "Error checking reset requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ViewProfileActivity.this, "Failed to retrieve user ID.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ViewProfileActivity.this, "Email not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewProfileActivity.this, "Error checking email: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
