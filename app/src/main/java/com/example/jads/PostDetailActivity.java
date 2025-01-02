package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private TextView postTitle;
    private Button chatButton;
    private Button deletePostButton; // New delete button

    private String currentUserId;
    private String posterUserId;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Initialize views
        postTitle = findViewById(R.id.postTitle);
        chatButton = findViewById(R.id.chatButton);
        deletePostButton = findViewById(R.id.deletePostButton); // Initialize delete button

        // Get intent extras
        postId = getIntent().getStringExtra("postId");
        posterUserId = getIntent().getStringExtra("posterUserId");

        // Log the received postId and posterUserId for debugging
        android.util.Log.d("PostDetailActivity", "Received Post ID: " + postId);
        android.util.Log.d("PostDetailActivity", "Received Poster User ID: " + posterUserId);

        // Get current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Log the currentUserId for debugging
        android.util.Log.d("PostDetailActivity", "Current User ID: " + currentUserId);

        // Set the post title (this can be dynamic based on the post data)
        if (postId != null) {
            postTitle.setText("Detailed view for Post ID: " + postId);
        } else {
            android.util.Log.e("PostDetailActivity", "Post ID is missing.");
            postTitle.setText("Post details not available.");
        }

        // Show delete button only if the current user owns the post
        if (currentUserId != null && currentUserId.equals(posterUserId)) {
            deletePostButton.setVisibility(Button.VISIBLE); // Show the button
        } else {
            deletePostButton.setVisibility(Button.GONE); // Hide the button
        }

        // Delete post logic
        deletePostButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        // Chat button logic
        chatButton.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(this, "You need to log in to start a chat.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (posterUserId == null) {
                Toast.makeText(this, "Unable to identify the poster.", Toast.LENGTH_SHORT).show();
                return;
            }
            createOrInitializeChat(currentUserId, posterUserId);
        });
    }


    private void deletePost() {
        if (postId == null) {
            Toast.makeText(this, "Post ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the post in Firebase
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);

        // Remove the post
        postRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Post deleted successfully.", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity after deletion
            } else {
                Toast.makeText(this, "Failed to delete the post.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrInitializeChat(String currentUserId, String otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            Toast.makeText(this, "You cannot chat with yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable the chat button to prevent multiple clicks
        chatButton.setEnabled(false);

        // Generate chat ID
        String chatId = currentUserId.compareTo(otherUserId) < 0
                ? currentUserId + "_" + otherUserId
                : otherUserId + "_" + currentUserId;

        // Reference to the chat in Firebase
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        // Prepare chat data
        Map<String, Object> chatData = new HashMap<>();
        Map<String, Boolean> participants = new HashMap<>();
        participants.put(currentUserId, true);
        participants.put(otherUserId, true);
        chatData.put("participants", participants);

        // Add the chat to Firebase
        chatRef.updateChildren(chatData).addOnCompleteListener(task -> {
            chatButton.setEnabled(true); // Re-enable the button
            if (task.isSuccessful()) {
                // Open the ChatActivity
                Intent intent = new Intent(PostDetailActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chatId);
                intent.putExtra("otherUserId", otherUserId);
                startActivity(intent);
            } else {
                Toast.makeText(PostDetailActivity.this, "Failed to initialize chat.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showDeleteConfirmationDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Proceed with deletion
                    deletePost();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Close the dialog without deleting
                    dialog.dismiss();
                })
                .create()
                .show();
    }

}
