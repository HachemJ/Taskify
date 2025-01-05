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

        Log.d(TAG, "Current User ID: " + currentUserId);
        Log.d(TAG, "Other User ID: " + otherUserId);
        Log.d(TAG, "Generated Chat ID: " + chatId);

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
                Log.d(TAG, "Sending message: " + messageText);
                sendMessage(messageText);
            } else {
                Log.d(TAG, "Message text is empty.");
            }
        });
    }

    private void loadMessages() {
        Log.d(TAG, "Loading messages...");
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
                Log.d(TAG, "Loaded " + messageList.size() + " messages.");
                messageAdapter.updateMessages(messageList);
                messagesRecyclerView.scrollToPosition(messageList.size() - 1); // Scroll to the latest message
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
            Log.d(TAG, "Generated Message ID: " + messageId);

            // Save message to Firebase
            chatReference.child("messages").child(messageId).setValue(message)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Message successfully saved to Firebase.");
                            updateUserChats(currentUserId, otherUserId, chatId);
                            updateUserChats(otherUserId, currentUserId, chatId);

                            // Send notification to the other user (otherUserId)
                            sendNotificationToOtherUser(otherUserId, currentUserId, text);

                            messageEditText.setText(""); // Clear input field
                        } else {
                            Log.e(TAG, "Failed to send message: " + task.getException());
                        }
                    });
        } else {
            Log.e(TAG, "Failed to generate Message ID.");
        }
    }

    private void updateUserChats(String userId, String otherUserId, String chatId) {
        Log.d(TAG, "Updating user chats for user: " + userId);
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("userChats").child(userId);

        userChatsRef.child(chatId).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User chats successfully updated for user: " + userId);
                    } else {
                        Log.e(TAG, "Failed to update user chats for user: " + userId, task.getException());
                    }
                });
    }

    private String generateChatId(String user1, String user2) {
        Log.d(TAG, "Generating Chat ID for users: " + user1 + ", " + user2);
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private void sendNotificationToOtherUser(String receiverId, String senderId, String message) {
        Log.d(TAG, "Fetching sender data for senderId: " + senderId);

        // Fetch sender's details
        FirebaseDatabase.getInstance().getReference("users").child(senderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot senderSnapshot) {
                        if (senderSnapshot.exists()) {
                            // Fetch first and last name
                            String firstName = senderSnapshot.child("firstName").getValue(String.class);
                            String lastName = senderSnapshot.child("lastName").getValue(String.class);

                            // Log first and last name
                            Log.d(TAG, "Sender First Name: " + firstName);
                            Log.d(TAG, "Sender Last Name: " + lastName);

                            // Construct sender name
                            String senderName = ((firstName != null ? firstName : "Unknown") + " " +
                                    (lastName != null ? lastName : "")).trim();
                            Log.d(TAG, "Constructed Sender Name: " + senderName);

                            // Fetch receiver's FCM token
                            fetchReceiverTokenAndSendNotification(receiverId, senderName, message);
                        } else {
                            Log.e(TAG, "Sender data does not exist for senderId: " + senderId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch sender's name: " + error.getMessage());
                    }
                });
    }

    private void fetchReceiverTokenAndSendNotification(String receiverId, String senderName, String message) {
        Log.d(TAG, "Fetching FCM token for receiverId: " + receiverId);

        FirebaseDatabase.getInstance().getReference("users").child(receiverId).child("fcmToken")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot tokenSnapshot) {
                        if (tokenSnapshot.exists()) {
                            String token = tokenSnapshot.getValue(String.class);
                            if (token != null) {
                                Log.d(TAG, "Fetched FCM Token for receiver: " + receiverId + ", Token: " + token);

                                // Send notification
                                SendNotification fcmSender = new SendNotification();
                                fcmSender.sendPushNotification(
                                        ChatActivity.this,
                                        "New Message from " + senderName, // Sender's full name
                                        message,
                                        token
                                );
                                Log.d(TAG, "Notification sent with title: New Message from " + senderName);
                            } else {
                                Log.e(TAG, "FCM token is null for receiver: " + receiverId);
                            }
                        } else {
                            Log.e(TAG, "FCM token does not exist for receiverId: " + receiverId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch FCM token: " + error.getMessage());
                    }
                });
    }

}
