package com.example.jads;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<Message> messageList;
    private final String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Determine if the message is sent by the current user
        if (message.getSenderId().equals(currentUserId)) {
            holder.sentMessageTextView.setVisibility(View.VISIBLE);
            holder.receivedMessageTextView.setVisibility(View.GONE);
            holder.sentMessageTextView.setText(message.getText());
        } else {
            holder.sentMessageTextView.setVisibility(View.GONE);
            holder.receivedMessageTextView.setVisibility(View.VISIBLE);
            holder.receivedMessageTextView.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView sentMessageTextView, receivedMessageTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            sentMessageTextView = itemView.findViewById(R.id.sentMessageTextView);
            receivedMessageTextView = itemView.findViewById(R.id.receivedMessageTextView);
        }
    }
}
