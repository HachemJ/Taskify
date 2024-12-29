package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignupActivity extends AppCompatActivity {

    EditText signupFirstName, signupLastName, signupEmail, signupPassword, signupUsername;
    Button signupButton;
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference usersReference, unverifiedUsersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signupFirstName = findViewById(R.id.firstnameEt);
        signupLastName = findViewById(R.id.lastnameEt);
        signupEmail = findViewById(R.id.emailaddressEt);
        signupPassword = findViewById(R.id.passwordEt);
        signupUsername = findViewById(R.id.usernameEt);
        signupButton = findViewById(R.id.signupButton);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        unverifiedUsersReference = database.getReference("unverified_users");

        signupButton.setOnClickListener(v -> {
            String firstName = signupFirstName.getText().toString().trim();
            String lastName = signupLastName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();
            String username = signupUsername.getText().toString().trim();

            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
                Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.length() < 4) {
                Toast.makeText(SignupActivity.this, "Username must be at least 4 characters long", Toast.LENGTH_SHORT).show();
                return;
            }

            Query usernameQuery = usersReference.orderByChild("username").equalTo(username);
            usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(SignupActivity.this, "Username is already taken. Please choose another one.", Toast.LENGTH_SHORT).show();
                    } else {
                        createUser(firstName, lastName, email, password, username);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("SignupActivity", "Database error: " + databaseError.getMessage());
                    Toast.makeText(SignupActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void createUser(String firstName, String lastName, String email, String password, String username) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    String userId = firebaseUser.getUid();
                    firebaseUser.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Verification email sent. Please verify your email.", Toast.LENGTH_SHORT).show();
                            HelperClass helperClass = new HelperClass(firstName, lastName, email, username, null);
                            unverifiedUsersReference.child(userId).setValue(helperClass).addOnCompleteListener(databaseTask -> {
                                if (databaseTask.isSuccessful()) {
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
}
