package com.example.jads;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> implements android.widget.Filterable {

    private final List<Post> postList; // Original list
    private List<Post> filteredPostList; // Filtered list for dynamic searching

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
        this.filteredPostList = new ArrayList<>(postList); // Initialize with all posts
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = filteredPostList.get(position);

        // Safely handle null values
        String title = post.getTitle() != null ? post.getTitle() : "No Title";
        String description = post.getDescription() != null ? post.getDescription() : "No description available";
        Long timestamp = post.getTimestamp();
        String imageUrl = post.getImageUrl();
        String posterUserId = post.getUserId();
        List<String> tags = post.getTags();
        String category = post.getCategory() != null ? post.getCategory() : "Uncategorized";
        String price = post.getPrice(); // Price remains a string
        String postId = post.getPostId(); // Ensure postId is retrieved here

        holder.titleTextView.setText(title);

        // Dynamically display tags
        if (tags != null && !tags.isEmpty()) {
            holder.tagTest1.setVisibility(View.VISIBLE);
            holder.tagTest1.setText(tags.get(0)); // Set first tag

            if (tags.size() >= 2) {
                holder.tagTest2.setVisibility(View.VISIBLE);
                holder.tagTest2.setText(tags.get(1)); // Set second tag
            } else {
                holder.tagTest2.setVisibility(View.GONE);
            }
        } else {
            holder.tagTest1.setVisibility(View.GONE);
            holder.tagTest2.setVisibility(View.GONE);
        }

        // Set the card color based on category
        if ("selling".equalsIgnoreCase(category)) {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.dark_blue));
            holder.learnMoreButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.black)); // Black button for selling
            holder.categoryTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
        } else if ("looking".equalsIgnoreCase(category)) {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.learnMoreButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.dark_blue)); // Blue button for looking
            holder.categoryTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.dark_blue));
        } else {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.darkest_gray));
            holder.learnMoreButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.darkest_gray)); // Default button color
            holder.categoryTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.darkest_gray));
        }

        // Display category
        holder.categoryTextView.setText(category);

        // Display price
        if (price != null && !price.isEmpty()) {
            holder.priceTextView.setText("$" + price); // Format as a string
        } else {
            holder.priceTextView.setText("Price not specified");
        }

        // Limit description to 100 characters
        String truncatedDescription = description.length() > 100 ? description.substring(0, 100) + "..." : description;
        holder.descriptionTextView.setText(truncatedDescription);

        // Format and set the timestamp if it exists
        if (timestamp != null) {
            String formattedDate = new java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                    .format(new java.util.Date(timestamp));
            holder.timeTextView.setText(formattedDate);
        } else {
            holder.timeTextView.setText("Unknown time");
        }

        // Load image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }

        // Fetch and set the username
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(posterUserId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.child("username").getValue(String.class);
                holder.usernameTextView.setText(username != null ? username : "Unknown User");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.usernameTextView.setText("Unknown User");
            }
        });

        // Learn More button functionality
        holder.learnMoreButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
            if (postId == null || postId.isEmpty()) {
                return; // Skip further processing for this item
            }
            intent.putExtra("postId", post.getPostId());
            intent.putExtra("posterUserId", posterUserId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredPostList != null ? filteredPostList.size() : 0; // Return the size of the filtered list
    }

    @Override
    public android.widget.Filter getFilter() {
        return new android.widget.Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint != null ? constraint.toString().toLowerCase() : "";
                List<Post> filtered = new ArrayList<>();

                if (query.isEmpty()) {
                    filtered = postList; // Show all posts if query is empty
                } else {
                    for (Post post : postList) {
                        if (post.getTitle() != null && post.getTitle().toLowerCase().contains(query)) {
                            filtered.add(post); // Add posts matching the title
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredPostList = (List<Post>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView titleTextView, usernameTextView, descriptionTextView, timeTextView, tagTest1, tagTest2, priceTextView, categoryTextView;
        Button learnMoreButton;
        ImageView imageView;
        androidx.cardview.widget.CardView cardView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            tagTest1 = itemView.findViewById(R.id.tagTest1);
            tagTest2 = itemView.findViewById(R.id.tagTest2);
            priceTextView = itemView.findViewById(R.id.priceTv);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            imageView = itemView.findViewById(R.id.imageView);
            learnMoreButton = itemView.findViewById(R.id.learnMoreButton);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
