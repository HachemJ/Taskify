package com.example.jads;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private List<Message> messageList;
    private MessageAdapter messageAdapter;

    private String currentUserId, otherUserId, chatId;

    private DatabaseReference chatReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Get data from intent
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUserId = getIntent().getStringExtra("otherUserId");
        chatId = generateChatId(currentUserId, otherUserId);

        Log.d(TAG, "Chat ID: " + chatId);

        // Initialize Firebase reference
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
                messageAdapter.updateMessages(messageList);
                messagesRecyclerView.scrollToPosition(messageList.size() - 1); // Scroll to latest message
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load messages: " + error.getMessage());
                Toast.makeText(ChatActivity.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage(String text) {
        String messageId = chatReference.child("messages").push().getKey();
        if (messageId != null) {
            Message message = new Message(currentUserId, text, System.currentTimeMillis());

            // Save message to Firebase
            chatReference.child("messages").child(messageId).setValue(message)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Message sent: " + text);
                            updateUserChats(currentUserId, otherUserId, chatId);
                            updateUserChats(otherUserId, currentUserId, chatId);
                            messageEditText.setText(""); // Clear input field
                        } else {
                            Log.e(TAG, "Failed to send message: " + task.getException());
                        }
                    });
        }
    }

    private void updateUserChats(String userId, String otherUserId, String chatId) {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("userChats").child(userId);

        userChatsRef.child(chatId).setValue(true) // Save chat ID for the user
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "UserChats updated for user: " + userId);
                    } else {
                        Log.e(TAG, "Failed to update UserChats for user: " + userId, task.getException());
                    }
                });
    }

    private String generateChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
    private ValueEventListener messagesListener;
    private DatabaseReference messagesRef;

    private void attachMessagesListener() {
        messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages");
        messagesListener = messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Handle message data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error querying messages: " + error.getMessage());
            }
        });
    }

}
