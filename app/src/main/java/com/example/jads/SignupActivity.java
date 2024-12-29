package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText signupFirstName, signupLastName, signupEmail, signupPhoneNumber, signupPassword, signupUsername;
    Button signupButton;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference unverifiedUsersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Handle system UI insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        signupFirstName = findViewById(R.id.firstnameEt);
        signupLastName = findViewById(R.id.lastnameEt);
        signupEmail = findViewById(R.id.emailaddressEt);
        signupPhoneNumber = findViewById(R.id.phonenumberEt);
        signupPassword = findViewById(R.id.passwordEt);
        signupUsername = findViewById(R.id.usernameEt);
        signupButton = findViewById(R.id.signupButton);

        // Initialize Firebase Auth and Realtime Database references
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        unverifiedUsersReference = database.getReference("unverified_users");

        signupButton.setOnClickListener(v -> {
            String firstName = signupFirstName.getText().toString().trim();
            String lastName = signupLastName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String phoneNumber = signupPhoneNumber.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();
            String username = signupUsername.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(password)) {
                Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();

                                // Send email verification
                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                Toast.makeText(SignupActivity.this, "Verification email sent. Please verify your email.", Toast.LENGTH_SHORT).show();

                                                // Save user data to "unverified_users" node
                                                HelperClass helperClass = new HelperClass(firstName, lastName, email, username, phoneNumber, null);
                                                unverifiedUsersReference.child(userId).setValue(helperClass)
                                                        .addOnCompleteListener(databaseTask -> {
                                                            if (databaseTask.isSuccessful()) {
                                                                // Schedule deletion for unverified users
                                                                scheduleAccountDeletion(firebaseUser);

                                                                // Redirect to login screen
                                                                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                                                                finish();
                                                            } else {
                                                                Toast.makeText(SignupActivity.this, "Failed to save user data: " + databaseTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(SignupActivity.this, "Failed to send verification email: " + verificationTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void scheduleAccountDeletion(FirebaseUser user) {
        new Thread(() -> {
            try {
                Thread.sleep(24 * 60 * 60 * 1000); // 24 heure ?? ou moin
                user.reload(); // Reload the user to check verification status
                if (!user.isEmailVerified()) {
                    // Delete the unverified account
                    unverifiedUsersReference.child(user.getUid()).removeValue()
                            .addOnCompleteListener(removeTask -> {
                                if (removeTask.isSuccessful()) {
                                    user.delete().addOnCompleteListener(deleteTask -> {
                                        if (deleteTask.isSuccessful()) {
                                            runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Unverified account deleted.", Toast.LENGTH_SHORT).show());
                                        } else {
                                            runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Failed to delete FirebaseAuth account: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show());
                                        }
                                    });
                                } else {
                                    runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Failed to delete unverified user entry: " + removeTask.getException().getMessage(), Toast.LENGTH_SHORT).show());
                                }
                            });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
