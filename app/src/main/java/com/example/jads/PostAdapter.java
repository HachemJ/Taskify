package com.example.jads;

import android.content.Intent;
import android.util.Log;
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
    // Filtered list for dynamic searching
    private final boolean isGuest;

    public PostAdapter(List<Post> postList, boolean isGuest) {
        this.postList = postList;
        this.filteredPostList = new ArrayList<>(postList); // Initialize with all posts
        this.isGuest = isGuest;
        Log.d("PostAdapter", "isGuest initialized: " + isGuest);
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
        Log.d("PostAdapter", "onBindViewHolder - isGuest: " + isGuest);

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

        // Display category
        holder.categoryTextView.setText(category);

        // Display price
        holder.priceTextView.setText(price != null ? "$" + price : "Price not specified");

        // Display description
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

        // Load post image using Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.placeholder_image);
        }

        // Fetch and set the profile picture
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users").child(posterUserId);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(holder.itemView.getContext())
                                .load(profileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_account) // Default icon if loading fails
                                .error(R.drawable.ic_account)      // Default icon if there's an error
                                .into(holder.profileImageView);
                        holder.profileImageView.setColorFilter(null); // Remove tint// Ensure the holder has a profileImageView
                    } else {
                        holder.profileImageView.setImageResource(R.drawable.ic_account);
                        holder.profileImageView.setColorFilter(holder.itemView.getContext()
                                .getResources().getColor(R.color.white));

                    }
                } else {
                    holder.profileImageView.setImageResource(R.drawable.ic_account);
                    holder.profileImageView.setColorFilter(holder.itemView.getContext()
                            .getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.profileImageView.setImageResource(R.drawable.ic_account);
                holder.profileImageView.setColorFilter(holder.itemView.getContext()
                        .getResources().getColor(R.color.white)); // Apply tint
            }
        });

        // Fetch and set the full name (firstName + lastName)
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);

                    String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                    holder.fullNameTextView.setText(fullName.trim().isEmpty() ? "Unknown Name" : fullName.trim());
                } else {
                    holder.fullNameTextView.setText("Unknown Name");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.fullNameTextView.setText("Unknown Name");
            }
        });

        // Set card background color based on category
        if ("selling".equalsIgnoreCase(category)) {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.gray));
            holder.learnMoreButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.categoryTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.timeTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.descriptionTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.fullNameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.titleTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.priceTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
        } else if ("looking".equalsIgnoreCase(category)) {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.learnMoreButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.gray));
            holder.categoryTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.timeTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.descriptionTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.fullNameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.titleTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.priceTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
        } else {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.gray));
            holder.learnMoreButton.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.categoryTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.timeTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.descriptionTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.fullNameTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.titleTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
            holder.priceTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
        }

        // Learn More button functionality
        holder.learnMoreButton.setOnClickListener(v -> {
            if (isGuest) {
                // Show a toast if the user is a guest
                Toast.makeText(v.getContext(),
                        "You are in guest mode. Viewing post details is restricted.",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Allow logged-in users to access post details
                Intent intent = new Intent(v.getContext(), PostDetailActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("posterUserId", post.getUserId());
                intent.putExtra("postTitle", post.getTitle());
                intent.putExtra("price", post.getPrice());
                intent.putExtra("description", post.getDescription());
                intent.putExtra("category", post.getCategory());

                if (post.getTags() != null && !post.getTags().isEmpty()) {
                    intent.putExtra("tag1", post.getTags().size() > 0 ? post.getTags().get(0) : "No Tag");
                    intent.putExtra("tag2", post.getTags().size() > 1 ? post.getTags().get(1) : "No Tag");
                }

                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return filteredPostList != null ? filteredPostList.size() : 0;
    }

    @Override
    public android.widget.Filter getFilter() {
        return new android.widget.Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint != null ? constraint.toString().toLowerCase() : "";
                List<Post> filtered = new ArrayList<>();

                if (query.isEmpty()) {
                    filtered = postList;
                } else {
                    for (Post post : postList) {
                        if (post.getTitle() != null && post.getTitle().toLowerCase().contains(query)) {
                            filtered.add(post);
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

    public void updatePosts(List<Post> newPostList) {
        this.postList.clear();
        this.postList.addAll(newPostList);
        this.filteredPostList = new ArrayList<>(newPostList);
        notifyDataSetChanged();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, fullNameTextView, descriptionTextView, timeTextView, tagTest1, tagTest2, priceTextView, categoryTextView;
        Button learnMoreButton;
        ImageView imageView;
        androidx.cardview.widget.CardView cardView;
        ImageView profileImageView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.titleTextView);
            fullNameTextView = itemView.findViewById(R.id.fullNameTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            tagTest1 = itemView.findViewById(R.id.tagTest1);
            tagTest2 = itemView.findViewById(R.id.tagTest2);
            priceTextView = itemView.findViewById(R.id.priceTv);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            imageView = itemView.findViewById(R.id.imageView);
            learnMoreButton = itemView.findViewById(R.id.learnMoreButton);
            cardView = itemView.findViewById(R.id.cardView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }
}
