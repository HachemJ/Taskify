package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    // Declare the EditText variables for user input fields
    EditText signupFirstName, signupLastName, signupUsername, signupEmail, signupPhoneNumber;

    // Declare the Button variable for the signup button
    Button signupButton;

    // Declare FirebaseDatabase instance for interacting with Firebase
    FirebaseDatabase database;

    // Declare DatabaseReference instance to reference the Firebase database
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity
        setContentView(R.layout.activity_signup);

        // Set a listener to apply window insets, helping to manage padding around system UI like status bar and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Get the insets for system bars (status bar, navigation bar)
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply padding to the main view to avoid content overlapping system bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the EditText fields by finding them from the layout
        signupFirstName = findViewById(R.id.firstnameEt);
        signupLastName = findViewById(R.id.lastnameEt);
        signupUsername = findViewById(R.id.usernameEt);
        signupEmail = findViewById(R.id.emailaddressEt);
        signupPhoneNumber = findViewById(R.id.phonenumberEt);

        // Initialize the Button by finding it from the layout
        signupButton = findViewById(R.id.signupButton);

        // Set an OnClickListener for the signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initialize FirebaseDatabase instance
                database = FirebaseDatabase.getInstance();
                // Get a reference to the "users" node in Firebase Database
                reference = database.getReference("users");

                // Retrieve the text entered by the user in the EditText fields
                String firstName = signupFirstName.getText().toString();
                String lastName = signupLastName.getText().toString();
                String username = signupUsername.getText().toString();
                String email = signupEmail.getText().toString();
                String phoneNumber = signupPhoneNumber.getText().toString();

                // Create a new HelperClass object to store the user's details
                HelperClass helperClass = new HelperClass(firstName, lastName, email, username, phoneNumber);

                // Save the user's details to the Firebase Database under the "users" node
                // The child's key is the username entered by the user
                reference.child(username).setValue(helperClass);

                // Show a Toast message to confirm successful signup
                Toast.makeText(SignupActivity.this, "You have signed up successfully!", Toast.LENGTH_SHORT).show();

                // Create an Intent to start the next activity (ConnectionActivity in this case)
                Intent intent = new Intent(SignupActivity.this, ConnectionActivity.class);
                startActivity(intent); // Start the new activity
            }
        });
    }
}