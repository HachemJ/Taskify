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
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity {

    EditText signupFirstName, signupLastName, signupEmail, signupPassword;
    Button signupButton;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference usersReference, unverifiedUsersReference;

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
        signupPassword = findViewById(R.id.passwordEt);
        signupButton = findViewById(R.id.signupButton);

        // Initialize Firebase Auth and Realtime Database references
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        unverifiedUsersReference = database.getReference("unverified_users");

        signupButton.setOnClickListener(v -> {
            String firstName = signupFirstName.getText().toString().trim();
            String lastName = signupLastName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();

            // Validate inputs
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(password)) {
                Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check for unique email
            usersReference.orderByChild("email").equalTo(email).get().addOnCompleteListener(emailTask -> {
                if (emailTask.isSuccessful() && emailTask.getResult().exists()) {
                    Toast.makeText(SignupActivity.this, "Email is already registered. Please use another email.", Toast.LENGTH_SHORT).show();
                } else {
                    createUser(firstName, lastName, email, password);
                }
            });
        });
    }

    private void createUser(String firstName, String lastName, String email, String password) {
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
                                            User user = new User(firstName, lastName, email, null, null, null, null, null, "user");
                                            unverifiedUsersReference.child(userId).setValue(user)
                                                    .addOnCompleteListener(databaseTask -> {
                                                        if (databaseTask.isSuccessful()) {
                                                            // Schedule deletion for unverified users
                                                            scheduleAccountDeletion(userId);

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
    }

    private void scheduleAccountDeletion(String userId) {
        // Pass userId to the Worker
        Data inputData = new Data.Builder()
                .putString("userId", userId)
                .build();

        // Schedule the work to run after 2 minutes
        OneTimeWorkRequest deleteWorkRequest = new OneTimeWorkRequest.Builder(DeleteUnverifiedAccountWorker.class)
                .setInitialDelay(30, TimeUnit.MINUTES)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(this).enqueue(deleteWorkRequest);
    }
}