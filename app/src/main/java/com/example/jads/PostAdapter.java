package com.example.jads;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // Safely handle null values
        String title = post.getTitle() != null ? post.getTitle() : "No Title";
        String username = post.getUsername() != null ? post.getUsername() : "Unknown User";
        String description = post.getDescription() != null ? post.getDescription() : "No description available";
        Long timestamp = post.getTimestamp();
        String imageUrl = post.getImageUrl();
        String posterUserId = post.getUserId(); // Retrieve the poster's user ID

        holder.titleTextView.setText(title);
        holder.usernameTextView.setText(username);

        // Limit description to 100 characters
        String truncatedDescription = description.length() > 100 ? description.substring(0, 100) + "..." : description;
        holder.descriptionTextView.setText(truncatedDescription);

        // Format and set the timestamp if it exists
        if (timestamp != null) {
            String formattedDate = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                    .format(new java.util.Date(timestamp));
            holder.timeTextView.setText(formattedDate);
        } else {
            holder.timeTextView.setText("Unknown time"); // Default text if timestamp is missing
        }

        // Load image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                    .error(R.drawable.error_image) // Error image if the URL is invalid
                    .into(holder.imageView); // Bind the imageView
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image); // Fallback image if no URL is provided
        }

        // Click listener for "Learn More"
        holder.learnMoreButton.setOnClickListener(v -> {
            // Open the PostDetailActivity and pass the post ID and posterUserId
            Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
            intent.putExtra("postId", post.getPostId()); // Pass the post ID
            intent.putExtra("posterUserId", posterUserId); // Pass the poster's user ID
            v.getContext().startActivity(intent);
        });
    }




    @Override
    public int getItemCount() {
        return postList.size(); // Return the correct size of postList
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, usernameTextView, descriptionTextView, timeTextView;
        Button learnMoreButton;
        public ImageView imageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView); // Link to description TextView in XML
            timeTextView = itemView.findViewById(R.id.timeTextView); // Link to time TextView in XML
            imageView = itemView.findViewById(R.id.imageView); // Add this
            learnMoreButton = itemView.findViewById(R.id.learnMoreButton);
        }
    }



}
