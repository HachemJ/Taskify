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
    DatabaseReference reference;

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

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        // Handle signup button click
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

            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Send email verification
                                firebaseUser.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                // Save user details to Firebase Database
                                                HelperClass helperClass = new HelperClass(firstName, lastName, email, username, phoneNumber, password); // Username not needed
                                                reference.child(firebaseUser.getUid()).setValue(helperClass) // Save using UID as key
                                                        .addOnCompleteListener(databaseTask -> {
                                                            if (databaseTask.isSuccessful()) {
                                                                Toast.makeText(SignupActivity.this, "Sign-up successful! Please verify your email.", Toast.LENGTH_SHORT).show();
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
}
