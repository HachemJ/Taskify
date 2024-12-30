package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PostDetailActivity extends AppCompatActivity {

    private TextView postTitle;
    private Button chatButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Initialize views
        postTitle = findViewById(R.id.postTitle);
        chatButton = findViewById(R.id.chatButton);

        // Retrieve postId and posterUserId from intent extras
        String postId = getIntent().getStringExtra("postId");
        String posterUserId = getIntent().getStringExtra("posterUserId");

        // Validate the posterUserId
        if (posterUserId == null || posterUserId.isEmpty()) {
            Toast.makeText(this, "Error loading post details. Please try again.", Toast.LENGTH_SHORT).show();
            finish(); // Exit the activity if the posterUserId is missing
            return;
        }

        // Example: Set post title (you can fetch post details from the database)
        postTitle.setText("Detailed view for Post ID: " + postId);

        // Chat button click listener
        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(PostDetailActivity.this, ChatActivity.class);
            intent.putExtra("posterUserId", posterUserId); // Pass the poster's user ID
            startActivity(intent);
        });
    }
}
