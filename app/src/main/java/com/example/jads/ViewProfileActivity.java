package com.example.jads;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
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
        setContentView(R.layout.activity_view_profile);

        // Initialize views
        TextView reviewScoreTv = findViewById(R.id.reviewScoreTv);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        emailField = findViewById(R.id.emailaddressEt);

        EditText firstNameField = findViewById(R.id.firstnameEt);
        EditText lastNameField = findViewById(R.id.lastnameEt);
        EditText usernameField = findViewById(R.id.usernameEt);

        // Firebase initialization
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        passwordResetRequests = database.getReference("passwordResetRequests"); // Initialize here

        // Set RatingBar value based on reviewScoreTv
        try {
            float rating = Float.parseFloat(reviewScoreTv.getText().toString());
            ratingBar.setRating(rating); // Set the rating dynamically
        } catch (NumberFormatException e) {
            ratingBar.setRating(0); // Default to 0 if parsing fails
            e.printStackTrace();
        }

        // Fetch user data from Firebase
        fetchUserData(firstNameField, lastNameField, usernameField);

        findViewById(R.id.resetPasswordButton).setOnClickListener(v -> handlePasswordReset());
    }

    private void fetchUserData(EditText firstNameField, EditText lastNameField, EditText usernameField) {
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        usersReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch the data from Firebase
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    // Populate the fields
                    firstNameField.setText(firstName != null ? firstName : "N/A");
                    lastNameField.setText(lastName != null ? lastName : "N/A");
                    usernameField.setText(username != null ? username : "N/A");
                    emailField.setText(email != null ? email : "N/A");
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
                                }

                                // Log the reset request with the email
                                HashMap<String, Object> resetLog = new HashMap<>();
                                resetLog.put("timestamp", ServerValue.TIMESTAMP);
                                resetLog.put("email", email);
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

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(ViewProfileActivity.this, "Error checking reset requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(ViewProfileActivity.this, "Failed to retrieve user ID.", Toast.LENGTH_SHORT).show();
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
