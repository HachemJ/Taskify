package com.example.jads;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        String title = post.getTitle() != null ? post.getTitle() : "No Title";
        String username = post.getUsername() != null ? post.getUsername() : "Unknown User";
        String description = post.getDescription() != null ? post.getDescription() : "No description available";

        holder.titleTextView.setText(title);
        holder.usernameTextView.setText(username);

        // Limit description to 100 characters
        String truncatedDescription = description.length() > 100 ? description.substring(0, 100) + "..." : description;
        holder.bioTextView.setText(truncatedDescription);

        // Click listener for "Learn More"
        holder.learnMoreButton.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Learn more clicked!", Toast.LENGTH_SHORT).show();
        });
    }



    @Override
    public int getItemCount() {
        return postList.size(); // Return the correct size of postList
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, usernameTextView, bioTextView;
        Button learnMoreButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            bioTextView = itemView.findViewById(R.id.bioTextView);
            learnMoreButton = itemView.findViewById(R.id.learnMoreButton);
        }
    }
}
