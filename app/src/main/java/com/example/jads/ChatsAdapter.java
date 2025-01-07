package com.example.jads;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {

    private final Context context;
    private final List<Chat> chatList;
    private final String currentUserId;

    public ChatsAdapter(Context context, List<Chat> chatList, String currentUserId) {
        this.context = context;
        this.chatList = chatList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        // Display the name of the other user
        String otherUserName = chat.getOtherUserFullName();
        holder.fullNameTextView.setText(otherUserName);

        String otherUserId = chat.getOtherUserId(currentUserId);

        // Fetch and display profile picture
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(otherUserId);
        userRef.child("profileImageUrl").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().getValue() != null) {
                String profileImageUrl = task.getResult().getValue(String.class);

                // Use Glide to load the profile image if it exists
                Glide.with(context)
                        .load(profileImageUrl)
                        .circleCrop() // Crop into a circle
                        .placeholder(R.drawable.baseline_person_24) // Fallback placeholder
                        .into(holder.chatUserIcon);
            } else {
                // Set default drawable if no profile image exists
                holder.chatUserIcon.setImageResource(R.drawable.baseline_person_24);
            }
        });

        // Check for unread messages
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chat.getChatId());
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long lastMessageTimestamp = snapshot.child("lastMessageTimestamp").getValue(Long.class);
                Long lastReadTimestamp = snapshot.child("lastRead").child(currentUserId).getValue(Long.class);

                if (lastMessageTimestamp != null && (lastReadTimestamp == null || lastMessageTimestamp > lastReadTimestamp)) {
                    holder.newMessageDot.setVisibility(View.VISIBLE); // Show the dot
                } else {
                    holder.newMessageDot.setVisibility(View.GONE); // Hide the dot
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.newMessageDot.setVisibility(View.GONE); // Hide the dot in case of error
            }
        });

        // Open ChatActivity when a chat is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("otherUserId", otherUserId);
            context.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView fullNameTextView;
        ImageView chatUserIcon;
        ImageView newMessageDot;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            fullNameTextView = itemView.findViewById(R.id.fullNameTv);
            chatUserIcon = itemView.findViewById(R.id.chatUserIcon);
            newMessageDot = itemView.findViewById(R.id.newMessageDot);// Add this line
        }
    }

}