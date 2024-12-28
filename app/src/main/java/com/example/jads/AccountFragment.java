package com.example.jads;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        return view;
    }
}
