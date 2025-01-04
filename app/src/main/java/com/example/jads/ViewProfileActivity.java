package com.example.jads;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
    private static final int IMAGE_PICK_CODE = 1000;
    private Uri selectedImageUri;
    private StorageReference storageReference;

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
        storageReference = FirebaseStorage.getInstance().getReference("profile_pictures");

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
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    // Populate fields
                    firstNameField.setText(firstName != null ? firstName : "");
                    lastNameField.setText(lastName != null ? lastName : "");
                    emailField.setText(email != null ? email : "");

                    // Update fullName TextView
                    String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                    fullNameTv.setText(fullName.trim().isEmpty() ? "N/A" : fullName.trim());

                    // Load profile image
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(ViewProfileActivity.this).load(profileImageUrl).into((ImageView) findViewById(R.id.profileImageView));
                    }
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
    public void uploadProfilePicture(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            if (selectedImageUri != null) {
                try {
                    // Display the selected image in the ImageView
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    ImageView profileImageView = findViewById(R.id.profileImageView);
                    profileImageView.setImageBitmap(bitmap);

                    // Upload the image to Firebase Storage
                    uploadImageToFirebase(selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private void uploadImageToFirebase(Uri imageUri) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        StorageReference fileRef = storageReference.child(userId + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Update the user's profile picture URL in the database
                    usersReference.child(userId).child("profileImageUrl").setValue(uri.toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ViewProfileActivity.this, "Profile picture updated successfully.", Toast.LENGTH_SHORT).show();

                                    // Load the image using Glide
                                    Glide.with(ViewProfileActivity.this).load(uri).into((ImageView) findViewById(R.id.profileImageView));
                                } else {
                                    Toast.makeText(ViewProfileActivity.this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }))
                .addOnFailureListener(e -> Toast.makeText(ViewProfileActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show());
    }
    public void showDeleteConfirmationDialog(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile picture?")
                .setPositiveButton("Yes", (dialog, which) -> deleteProfilePicture())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
    private void deleteProfilePicture() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Delete the profile picture from Firebase Storage
        StorageReference fileRef = FirebaseStorage.getInstance().getReference("profile_pictures").child(userId + ".jpg");
        fileRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove the profile image URL from the database
                usersReference.child(userId).child("profileImageUrl").removeValue().addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        Toast.makeText(this, "Profile picture deleted successfully.", Toast.LENGTH_SHORT).show();

                        // Reset the profile image to the default placeholder
                        ImageView profileImageView = findViewById(R.id.profileImageView);
                        profileImageView.setImageResource(R.drawable.ic_account); // Replace with your default placeholder
                    } else {
                        Toast.makeText(this, "Failed to update the database.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to delete profile picture.", Toast.LENGTH_SHORT).show();
            }
        });
    }




}
