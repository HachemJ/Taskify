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

        // Get intent extras
        postId = getIntent().getStringExtra("postId");
        posterUserId = getIntent().getStringExtra("posterUserId");

        // Get current user ID
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Set the post title (this can be dynamic based on the post data)
        if (postId != null) {
            postTitle.setText("Detailed view for Post ID: " + postId);
        } else {
            postTitle.setText("Post details not available.");
        }

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
    private void createOrFetchChat(String currentUserId, String posterUserId) {
        if (currentUserId.equals(posterUserId)) {
            Toast.makeText(this, "You cannot chat with yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        String chatId = currentUserId.compareTo(posterUserId) < 0
                ? currentUserId + "_" + posterUserId
                : posterUserId + "_" + currentUserId;

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);
        Map<String, Object> chatData = new HashMap<>();
        Map<String, Boolean> participants = new HashMap<>();
        participants.put(currentUserId, true);
        participants.put(posterUserId, true);
        chatData.put("participants", participants);

        // Create or fetch the chat
        chatRef.updateChildren(chatData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Add chat to userChats
                addChatToUserChats(currentUserId, posterUserId, chatId);
            } else {
                Toast.makeText(PostDetailActivity.this, "Failed to initialize chat.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addChatToUserChats(String userId1, String userId2, String chatId) {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("userChats");
        userChatsRef.child(userId1).child(chatId).setValue(true);
        userChatsRef.child(userId2).child(chatId).setValue(true);
    }

}
