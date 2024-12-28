package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.ActionCodeSettings;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button loginButton;
    private EditText emailEditText;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth and DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        loginButton = findViewById(R.id.loginButton); // Ensure this button exists in your XML layout
        emailEditText = findViewById(R.id.usernameOrEmailEt); // Ensure this EditText exists in your layout

        // If the button is clicked, manually process the sign-in
        loginButton.setOnClickListener(v -> {
            // Retrieve the email from the user input (EditText)
            String email = emailEditText.getText().toString().trim();

            // Validate email
            if (email.isEmpty()) {
                emailEditText.setError("Email cannot be empty");
                return;
            }

            // Verify if the email exists in Firebase Authentication
            verifyEmailExistsAndSendLink(email);
        });
    }

    /**
     * Verifies whether the email exists in Firebase Authentication or Realtime Database.
     * If exists, sends the sign-in link.
     * @param email The email to check.
     */
    private void verifyEmailExistsAndSendLink(String email) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Check if the email is registered in Firebase Authentication
                if (task.getResult().getSignInMethods().isEmpty()) {
                    // Email not registered in Firebase Authentication, now check in Realtime Database
                    checkEmailInRealtimeDatabase(email);
                } else {
                    // Email exists in Firebase Authentication, send sign-in link
                    // Show the WaitingActivity (loading screen)
                    Intent waitingIntent = new Intent(LoginActivity.this, WaitingForVerificationActivity.class);
                    startActivity(waitingIntent);

                    sendSignInLinkToEmail(email);
                }
            } else {
                // Handle errors with fetching sign-in methods
                String errorMessage = "Error verifying email: " + task.getException().getMessage();
                showError(errorMessage);
            }
        });
    }

    /**
     * Checks if the email exists in Firebase Realtime Database.
     * @param email The email to check.
     */
    private void checkEmailInRealtimeDatabase(String email) {
        mDatabase.child("users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Email exists in Realtime Database, proceed with sign-in
                    // Show the WaitingActivity (loading screen)
                    Intent waitingIntent = new Intent(LoginActivity.this, WaitingForVerificationActivity.class);
                    startActivity(waitingIntent);

                    sendSignInLinkToEmail(email);
                } else {
                    // Email not found in Realtime Database
                    showError("No account found with this email in the Realtime Database");
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                // Handle errors when querying the Realtime Database
                String errorMessage = "Error checking Realtime Database: " + databaseError.getMessage();
                showError(errorMessage);
            }
        });
    }

    /**
     * Sends the sign-in link to the provided email.
     * @param email The email to send the link to.
     */
    private void sendSignInLinkToEmail(String email) {
        // Create ActionCodeSettings to specify where the user will be redirected after signing in
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://jads-4eb19.firebaseapp.com/verify")  // URL where user will be redirected after clicking the link
                .setHandleCodeInApp(true)                 // Ensures the link is handled in your app
                .build();

        // Send the sign-in link to the provided email with the ActionCodeSettings
        mAuth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Proceed with the sign-in process, email has been sent
                        Toast.makeText(LoginActivity.this, "Sign-in link sent", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle failure to send the email
                        String errorMessage = "Failed to send sign-in link: " + task.getException().getMessage();
                        showError(errorMessage);
                    }
                });
    }

    /**
     * Displays a Toast with the error message.
     * @param message The error message to show.
     */
    private void showError(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
        // Optionally, log the error for debugging
        android.util.Log.e("LoginActivity", message);
    }
}
