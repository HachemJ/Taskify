package com.example.jads;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private Button sendButton;

    private DatabaseReference chatReference;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    private String currentUserId;
    private String posterUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Get current user ID and poster user ID from intent
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        posterUserId = getIntent().getStringExtra("posterUserId");

        // Validate the IDs
        if (currentUserId == null || posterUserId == null) {
            Toast.makeText(this, "Invalid chat setup. Please try again.", Toast.LENGTH_SHORT).show();
            finish(); // Exit the activity if IDs are invalid
            return;
        }

        // Generate chat ID and initialize Firebase reference
        String chatId = generateChatId(currentUserId, posterUserId);
        chatReference = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Load messages
        loadMessages();

        // Send button click listener
        sendButton.setOnClickListener(v -> {
            String messageText = messageEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText);
                messageEditText.setText(""); // Clear the input field
            }
        });
    }


    private void loadMessages() {
        chatReference.child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messageList.size() - 1); // Scroll to the latest message
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void sendMessage(String messageText) {
        String messageId = chatReference.child("messages").push().getKey();
        if (messageId != null) {
            HashMap<String, Object> messageData = new HashMap<>();
            messageData.put("senderId", currentUserId);
            messageData.put("receiverId", posterUserId);
            messageData.put("message", messageText);
            messageData.put("timestamp", System.currentTimeMillis());

            chatReference.child("messages").child(messageId).setValue(messageData);
        }
    }

    private String generateChatId(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            throw new IllegalArgumentException("User IDs must not be null");
        }
        return userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

}
