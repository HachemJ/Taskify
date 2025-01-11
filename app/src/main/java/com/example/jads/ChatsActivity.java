package com.example.jads;

import android.os.Bundle;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatsActivity extends AppCompatActivity {

    private static final String TAG = "ChatsActivity";

    private RecyclerView chatsRecyclerView;
    private List<Chat> chatList;
    private ChatsAdapter chatsAdapter;

    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        // Log activity start
        Log.d(TAG, "ChatsActivity started");

        // Check if the user is authenticated
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "No authenticated user found");
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Exit if not logged in
            return;
        }

        // Initialize views and Firebase Auth
        chatsRecyclerView = findViewById(R.id.chatsRecyclerView);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Current user ID: " + currentUserId);

        // Initialize chat list and adapter
        chatList = new ArrayList<>();
        chatsAdapter = new ChatsAdapter(this, chatList, currentUserId);

        // Setup RecyclerView
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatsRecyclerView.setAdapter(chatsAdapter);

        // Load chats from Firebase
        loadChats();
    }

    private void loadChats() {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("userChats").child(currentUserId);

        Log.d(TAG, "Querying userChats for user ID: " + currentUserId);

        userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear(); // Clear the list to avoid duplicates
                if (snapshot.exists()) {
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        String chatId = chatSnapshot.getKey(); // Get the chatId from userChats
                        if (chatId != null) {
                            fetchChatDetails(chatId);
                        }
                    }
                } else {
                    Log.d(TAG, "No chats found for user.");
                    chatsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error querying userChats: " + error.getMessage());
                Toast.makeText(ChatsActivity.this, "Failed to load chats: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchChatDetails(String chatId) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot chatSnapshot) {
                if (!chatSnapshot.exists() || !chatSnapshot.child("participants").hasChild(currentUserId)) {
                    // Initialize the chat if it doesn't exist or the current user is not a participant
                    initializeChat(chatId);
                } else {
                    String otherUserId = getOtherUserId(chatSnapshot.child("participants"));
                    if (otherUserId != null) {
                        fetchUserDetailsAndAddChat(chatId, otherUserId, chatSnapshot.child("participants").getValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching chat details: " + error.getMessage());
            }
        });
    }




    /**
     * Extract the other user's ID from the participants node
     */
    private String getOtherUserId(DataSnapshot participantsSnapshot) {
        for (DataSnapshot participant : participantsSnapshot.getChildren()) {
            if (!participant.getKey().equals(currentUserId)) {
                return participant.getKey(); // Return the other user's ID
            }
        }
        Log.w(TAG, "No other user ID found for participants node.");
        return null;
    }

    /**
     * Fetch user details and add the chat to the list
     */
    private void fetchUserDetailsAndAddChat(String chatId, String otherUserId, Object participants) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String otherUserFullName = "Unknown User";

                if (userSnapshot.exists()) {
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);

                    if (firstName != null && lastName != null) {
                        otherUserFullName = firstName + " " + lastName;
                    }
                }

                Log.d(TAG, "Fetched user details for user ID: " + otherUserId + ", Name: " + otherUserFullName);

                // Check if chat already exists in the list
                boolean chatExists = false;
                for (Chat chat : chatList) {
                    if (chat.getChatId().equals(chatId)) {
                        chatExists = true;
                        break;
                    }
                }

                if (!chatExists) {
                    // Cast participants object to Map<String, Boolean>
                    @SuppressWarnings("unchecked")
                    Map<String, Boolean> participantsMap = (Map<String, Boolean>) participants;

                    // Add chat to the list after user data is fetched
                    chatList.add(new Chat(chatId, otherUserId, otherUserFullName, participantsMap));
                    chatsAdapter.notifyDataSetChanged(); // Notify adapter after adding
                } else {
                    Log.d(TAG, "Chat with ID " + chatId + " already exists in the list.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load user data: " + error.getMessage());
                Toast.makeText(ChatsActivity.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private ValueEventListener chatsListener;
    private DatabaseReference chatsRef;

    private void attachChatsListener() {
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        chatsListener = chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Handle chat data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error querying chats: " + error.getMessage());
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        for (Chat chat : chatList) {
            refreshChatList(chat.getChatId());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachChatsListener(); // Detach listeners to prevent duplicate updates
    }

    private void detachChatsListener() {
        // Detach `chatsRef` listener if it exists
        if (chatsListener != null && chatsRef != null) {
            chatsRef.removeEventListener(chatsListener);
        }

        // Detach `userChatsRef` listener if it exists
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("userChats").child(currentUserId);
        if (chatsListener != null) {
            userChatsRef.removeEventListener(chatsListener);
        }
    }
    private void refreshChatList(String chatId) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot chatSnapshot) {
                if (chatSnapshot.exists()) {
                    Long lastMessageTimestamp = chatSnapshot.child("lastMessageTimestamp").getValue(Long.class);
                    Long lastRead = chatSnapshot.child("lastRead").child(currentUserId).getValue(Long.class);

                    // Handle null values for timestamps
                    lastMessageTimestamp = (lastMessageTimestamp != null) ? lastMessageTimestamp : 0L;
                    lastRead = (lastRead != null) ? lastRead : 0L;

                    boolean hasUnreadMessages = lastMessageTimestamp > lastRead;

                    // Update the corresponding chat in the list
                    for (Chat chat : chatList) {
                        if (chat.getChatId().equals(chatId)) {
                            chat.setHasUnreadMessages(hasUnreadMessages);
                            break;
                        }
                    }
                    chatsAdapter.notifyDataSetChanged();
                } else {
                    initializeChat(chatId); // Initialize the chat if it doesn't exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error refreshing chat list: " + error.getMessage());
            }
        });
    }


    private void initializeChat(String chatId) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        // Find the other user's ID
        String otherUserId = getOtherUserIdFromChatId(chatId);
        if (otherUserId == null) {
            Log.e(TAG, "Failed to determine other user ID from chatId: " + chatId);
            return;
        }

        // Set up chat participants
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", Map.of(currentUserId, true, otherUserId, true));

        chatRef.updateChildren(chatData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Chat initialized successfully for chatId: " + chatId);
                fetchUserDetailsAndAddChat(chatId, otherUserId, chatData.get("participants"));
            } else {
                Log.e(TAG, "Failed to initialize chat for chatId: " + chatId, task.getException());
            }
        });
    }

    private String getOtherUserIdFromChatId(String chatId) {
        // Extract the other user's ID based on the chatId format
        String[] userIds = chatId.split("_");
        if (userIds.length != 2) {
            return null;
        }
        return userIds[0].equals(currentUserId) ? userIds[1] : userIds[0];
    }







}
