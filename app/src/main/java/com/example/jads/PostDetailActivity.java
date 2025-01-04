package com.example.jads;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    private TextView postTitleTextView, priceTextView, reviewScoreTextView, reviewCountTextView, descriptionTextView, tagTest1, tagTest2;
    private ImageView profileImageView, imageView;
    private Button chatButton, deletePostButton, payButton;
    private RatingBar ratingBar;

    private String currentUserId;
    private String posterUserId;
    private String postId;
    private String postTitle;
    private String postPrice;
    private String postDescription;
    private String tag1;
    private String tag2;
    private static final int PAYMENT_METHODS_REQUEST_CODE = 102; // Define a new request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        initializeViews();

        // Retrieve data passed from PostAdapter
        retrieveIntentData();

        // Display post details
        displayPostDetails();

        // Fetch and display the post image
        fetchPostImage();

        // Fetch and display poster user details
        fetchPosterDetails();

        // Set up buttons
        setupButtons();
    }

    private void initializeViews() {
        postTitleTextView = findViewById(R.id.titleTextView);
        priceTextView = findViewById(R.id.priceTv);
        reviewScoreTextView = findViewById(R.id.reviewScoreTv);
        reviewCountTextView = findViewById(R.id.reviewCountTv);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        tagTest1 = findViewById(R.id.tagTest1);
        tagTest2 = findViewById(R.id.tagTest2);
        profileImageView = findViewById(R.id.profileImageView);
        imageView = findViewById(R.id.imageView);
        chatButton = findViewById(R.id.chatButton);
        deletePostButton = findViewById(R.id.deletePostButton);
        payButton = findViewById(R.id.payButton);
        ratingBar = findViewById(R.id.ratingBar);
    }

    private void retrieveIntentData() {
        postId = getIntent().getStringExtra("postId");
        posterUserId = getIntent().getStringExtra("posterUserId");
        postTitle = getIntent().getStringExtra("postTitle");
        postPrice = getIntent().getStringExtra("price");
        postDescription = getIntent().getStringExtra("description");
        tag1 = getIntent().getStringExtra("tag1");
        tag2 = getIntent().getStringExtra("tag2");

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    private void displayPostDetails() {
        postTitleTextView.setText(postTitle != null ? postTitle : "Title unavailable");
        priceTextView.setText(postPrice != null ? "$" + postPrice : "Price not specified");
        descriptionTextView.setText(postDescription != null ? postDescription : "Description not available");
        tagTest1.setText(tag1 != null ? tag1 : "No Tag");
        tagTest2.setText(tag2 != null ? tag2 : "No Tag");
    }

    private void fetchPostImage() {
        if (postId == null) {
            Toast.makeText(this, "Post ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference postImageRef = FirebaseDatabase.getInstance().getReference("posts").child(postId).child("imageUrl");

        postImageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageUrl = snapshot.getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(PostDetailActivity.this).load(imageUrl).into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.placeholder_image); // Use your placeholder image resource
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Failed to fetch post image: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPosterDetails() {
        if (posterUserId == null) {
            Toast.makeText(this, "Poster details unavailable.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(posterUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch full name (firstName + lastName)
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");

                    TextView fullNameTextView = findViewById(R.id.FullNameTv);
                    fullNameTextView.setText(fullName.trim().isEmpty() ? "Unknown Name" : fullName.trim());

                    // Fetch review score and number of ratings
                    Double reviewScore = snapshot.child("reviewScore").getValue(Double.class);
                    Long reviewCount = snapshot.child("nbOfRatings").getValue(Long.class);

                    reviewScoreTextView.setText(reviewScore != null ? String.format("%.1f", reviewScore) : "N/A");
                    reviewCountTextView.setText(reviewCount != null ? String.valueOf(reviewCount) : "0");
                    ratingBar.setRating(reviewScore != null ? reviewScore.floatValue() : 0.0f);

                    // Fetch profile image
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        // Load the profile image with Glide
                        Glide.with(PostDetailActivity.this)
                                .load(profileImageUrl)
                                .circleCrop() // Crop to a circle
                                .into(profileImageView);
                        // Set circular background e


                    } else {
                        // Set default image
                        profileImageView.setImageResource(R.drawable.ic_account);
                        // Remove circular background
                        profileImageView.setBackground(null);
                    }
                } else {
                    Toast.makeText(PostDetailActivity.this, "Poster details not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Failed to fetch user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        if (currentUserId != null && currentUserId.equals(posterUserId)) {
            // User's own post
            deletePostButton.setVisibility(Button.VISIBLE);
            deletePostButton.setOnClickListener(v -> showDeleteConfirmationDialog());
            chatButton.setVisibility(Button.GONE);

            payButton.setVisibility(Button.VISIBLE);
            payButton.setText("Modify Payment Method(s)");
            payButton.setOnClickListener(v -> {
                // Redirect to PaymentMethodsActivity with postId and "isSameUser" flag as true
                Intent intent = new Intent(PostDetailActivity.this, PaymentMethodsActivity.class);
                intent.putExtra("isSameUser", true);
                intent.putExtra("postId", postId); // Pass postId to update payment methods
                startActivityForResult(intent, PAYMENT_METHODS_REQUEST_CODE);
            });
        } else {
            // Another user's post
            chatButton.setVisibility(Button.VISIBLE);
            deletePostButton.setVisibility(Button.GONE);

            payButton.setVisibility(Button.VISIBLE);
            payButton.setText("Pay Now");
            payButton.setOnClickListener(v -> {
                // Redirect to PaymentsAvailable activity for non-owners
                Intent intent = new Intent(PostDetailActivity.this, PaymentsAvailable.class);
                intent.putExtra("postId", postId); // Pass the postId to retrieve payment methods
                startActivity(intent);
            });

            chatButton.setOnClickListener(v -> startChat());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYMENT_METHODS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> selectedMethods = data.getStringArrayListExtra("selectedPaymentMethods");

            if (selectedMethods != null) {
                Map<String, Object> updatedPaymentMethods = new HashMap<>();
                updatedPaymentMethods.put("cash", selectedMethods.contains("cash"));
                updatedPaymentMethods.put("whish", selectedMethods.contains("whish"));

                // Update the payment methods in the Firebase database
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId).child("paymentMethods");
                postRef.setValue(updatedPaymentMethods)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Payment methods updated successfully.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to update payment methods.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }



    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes", (dialog, which) -> deletePost())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deletePost() {
        if (postId == null) {
            Toast.makeText(this, "Post ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId);

        postRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Post deleted successfully.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to delete the post.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startChat() {
        if (currentUserId == null || posterUserId == null || currentUserId.equals(posterUserId)) {
            Toast.makeText(this, "Unable to start chat.", Toast.LENGTH_SHORT).show();
            return;
        }

        chatButton.setEnabled(false);

        String chatId = currentUserId.compareTo(posterUserId) < 0
                ? currentUserId + "_" + posterUserId
                : posterUserId + "_" + currentUserId;

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", Map.of(currentUserId, true, posterUserId, true));

        chatRef.updateChildren(chatData).addOnCompleteListener(task -> {
            chatButton.setEnabled(true);
            if (task.isSuccessful()) {
                Intent intent = new Intent(PostDetailActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chatId);
                intent.putExtra("otherUserId", posterUserId);
                startActivity(intent);
            } else {
                Toast.makeText(PostDetailActivity.this, "Failed to initialize chat.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
