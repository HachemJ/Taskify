package com.example.jads;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FAQActivity extends AppCompatActivity {

    private RecyclerView faqRecyclerView;
    private FAQAdapter faqAdapter;
    private List<FAQItem> faqList;
    private EditText questionInput, answerInput;
    private Button addButton, toggleRoleButton;

    private DatabaseReference faqDatabaseRef;
    private DatabaseReference userDatabaseRef;
    private FirebaseAuth auth;

    private static final String TAG = "FAQActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqactivity);

        // Initialize Firebase references
        faqDatabaseRef = FirebaseDatabase.getInstance().getReference("faqs");
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        faqRecyclerView = findViewById(R.id.faqRecyclerView);
        faqRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        questionInput = findViewById(R.id.questionInput);
        answerInput = findViewById(R.id.answerInput);
        addButton = findViewById(R.id.addButton);
        toggleRoleButton = findViewById(R.id.toggleRoleButton); // New Button for toggling roles

        // Initialize the FAQ list and adapter
        faqList = new ArrayList<>();
        faqAdapter = new FAQAdapter(faqList);
        faqRecyclerView.setAdapter(faqAdapter);

        // Fetch FAQs from Firebase
        fetchFAQs();

        // Check user role to determine visibility of input fields
        checkUserRole();

        // Add button functionality
        addButton.setOnClickListener(v -> addFAQ());

        // Toggle role functionality
        toggleRoleButton.setOnClickListener(v -> toggleUserRole());
    }

    private void fetchFAQs() {
        faqDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                faqList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FAQItem faq = snapshot.getValue(FAQItem.class);
                    if (faq != null) {
                        faqList.add(faq);
                    }
                }
                faqAdapter.notifyDataSetChanged(); // Update RecyclerView
                if (faqList.isEmpty()) {
                    Toast.makeText(FAQActivity.this, "No FAQs found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FAQActivity.this, "Failed to fetch FAQs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "DatabaseError: " + error.getMessage());
            }
        });
    }

    private void addFAQ() {
        String question = questionInput.getText().toString().trim();
        String answer = answerInput.getText().toString().trim();

        if (question.isEmpty() || answer.isEmpty()) {
            Toast.makeText(this, "Please enter both question and answer.", Toast.LENGTH_SHORT).show();
            return;
        }

        String faqId = faqDatabaseRef.push().getKey();
        FAQItem newFAQ = new FAQItem(question, answer);

        faqDatabaseRef.child(faqId).setValue(newFAQ)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "FAQ added successfully!", Toast.LENGTH_SHORT).show();
                    questionInput.setText("");
                    answerInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add FAQ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Add FAQ Error: " + e.getMessage());
                });
    }

    private void checkUserRole() {
        String userId = auth.getCurrentUser().getUid();
        userDatabaseRef.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                if ("moderator".equals(role)) {
                    enableModeratorFeatures();
                } else {
                    disableModeratorFeatures();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking user role: " + error.getMessage());
                Toast.makeText(FAQActivity.this, "Failed to check user role.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleUserRole() {
        String userId = auth.getCurrentUser().getUid();
        userDatabaseRef.child(userId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentRole = snapshot.getValue(String.class);
                String newRole = "user".equals(currentRole) ? "moderator" : "user";

                userDatabaseRef.child(userId).child("role").setValue(newRole)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(FAQActivity.this, "Role changed to: " + newRole, Toast.LENGTH_SHORT).show();
                            checkUserRole(); // Re-check role to update UI
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FAQActivity.this, "Failed to change role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Role change error: " + e.getMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error toggling role: " + error.getMessage());
                Toast.makeText(FAQActivity.this, "Failed to toggle role.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableModeratorFeatures() {
        questionInput.setVisibility(View.VISIBLE);
        answerInput.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);
    }

    private void disableModeratorFeatures() {
        questionInput.setVisibility(View.GONE);
        answerInput.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
    }
}
