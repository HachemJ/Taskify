package com.example.jads;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    private TextView userNameTextView;
    private DatabaseReference reference;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);

        // Initialize views
        userNameTextView = view.findViewById(R.id.user_name);
        LinearLayout logoutButton = view.findViewById(R.id.logoutButton); // Reference to the Log Out button layout

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Get the current logged-in user
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // Get the UID of the logged-in user
            String uid = currentUser.getUid();

            // Query Firebase Realtime Database for the user data
            reference = FirebaseDatabase.getInstance().getReference("users").child(uid);

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String firstName = snapshot.child("firstName").getValue(String.class);
                        String lastName = snapshot.child("lastName").getValue(String.class);
                        if (firstName != null && lastName != null) {
                            userNameTextView.setText(firstName + " " + lastName);
                        } else {
                            userNameTextView.setText("User");
                            Toast.makeText(getContext(), "First or last name not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        userNameTextView.setText("Guest User");
                        Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            userNameTextView.setText("Guest User");
            Toast.makeText(getContext(), "No user is logged in", Toast.LENGTH_SHORT).show();
        }

        // Set up the logout button
        logoutButton.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Perform logout
                    auth.signOut();
                    Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                    // Redirect to login screen
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
