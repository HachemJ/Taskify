package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText signupFirstName, signupLastName, signupUsername, signupEmail, signupPhoneNumber;
    Button signupButton;

    // Firebase instances
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        // Initialize the EditText fields
        signupFirstName = findViewById(R.id.firstnameEt);
        signupLastName = findViewById(R.id.lastnameEt);
        signupUsername = findViewById(R.id.usernameEt);
        signupEmail = findViewById(R.id.emailaddressEt);
        signupPhoneNumber = findViewById(R.id.phonenumberEt);

        // Initialize the signup button
        signupButton = findViewById(R.id.signupButton);

        // Set the onClickListener for the signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = signupFirstName.getText().toString();
                String lastName = signupLastName.getText().toString();
                String username = signupUsername.getText().toString();
                String email = signupEmail.getText().toString();
                String phoneNumber = signupPhoneNumber.getText().toString();

                // Validate the input fields
                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || username.isEmpty() || phoneNumber.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Save the user's details to Firebase Realtime Database
                HelperClass helperClass = new HelperClass(firstName, lastName, email, username, phoneNumber);
                reference.child(username).setValue(helperClass);
                mAuth.createUserWithEmailAndPassword(email, username + "yyyyyy"); //username + 6 characters instead of password

                // Configure the ActionCodeSettings to manage the email link
                ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                        .setUrl("https://jads-4eb19.firebaseapp.com/finishSignUp") // Set the URL to redirect after clicking the link
                        .setHandleCodeInApp(true)  // Ensure the app handles the link
                        .setAndroidPackageName(
                                "com.example.jads", // The package name of your app
                                true,                // Whether to install the app if it's not installed
                                null)                // Optional fallback URL
                        .build();

                // Send the sign-in link to the user's email
                mAuth.sendSignInLinkToEmail(email, actionCodeSettings)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Notify user of successful sign-up
                                Toast.makeText(SignupActivity.this, "Sign-up successful! Check your email to complete the process.", Toast.LENGTH_SHORT).show();

                                // Navigate to the Login screen
                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SignupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
