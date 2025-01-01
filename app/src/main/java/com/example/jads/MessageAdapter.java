package com.example.jads;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DATE_HEADER = 0;
    private static final int VIEW_TYPE_MESSAGE = 1;

    private final List<Object> itemList = new ArrayList<>();
    private final String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.currentUserId = currentUserId;
        groupMessagesByDate(messageList);
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) instanceof String) {
            return VIEW_TYPE_DATE_HEADER;
        }
        return VIEW_TYPE_MESSAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).bind((String) itemList.get(position));
        } else if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).bind((Message) itemList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateMessages(List<Message> messageList) {
        groupMessagesByDate(messageList);
        notifyDataSetChanged();
    }

    private void groupMessagesByDate(List<Message> messageList) {
        itemList.clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String lastDate = "";

        for (Message message : messageList) {
            String messageDate = dateFormat.format(new Date(message.getTimestamp()));

            if (!messageDate.equals(lastDate)) {
                itemList.add(messageDate);
                lastDate = messageDate;
            }
            itemList.add(message);
        }
    }

    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView dateTextView;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(String date) {
            dateTextView.setText(date);
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView sentMessageTextView, receivedMessageTextView, timeTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            sentMessageTextView = itemView.findViewById(R.id.sentMessageTextView);
            receivedMessageTextView = itemView.findViewById(R.id.receivedMessageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }

        public void bind(Message message) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String time = timeFormat.format(new Date(message.getTimestamp()));

            // Determine if the message is sent by the current user
            if (message.getSenderId().equals(currentUserId)) {
                sentMessageTextView.setVisibility(View.VISIBLE);
                receivedMessageTextView.setVisibility(View.GONE);
                timeTextView.setVisibility(View.VISIBLE);

                sentMessageTextView.setText(message.getText());
                timeTextView.setText(time);
            } else {

                sentMessageTextView.setVisibility(View.GONE);
                receivedMessageTextView.setVisibility(View.VISIBLE);
                timeTextView.setVisibility(View.VISIBLE);

                receivedMessageTextView.setText(message.getText());
                timeTextView.setText(time);
            }
        }
    }
}
