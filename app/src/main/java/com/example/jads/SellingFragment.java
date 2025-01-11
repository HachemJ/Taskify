package com.example.jads;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;


public class SellingFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference postsReference;
    private String currentUserId;
    private AdView adView;
    private boolean isGuest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selling, container, false);
        // Find AdView and load an ad
        adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        Button openAddPostDialogButton = view.findViewById(R.id.openAddPostDialogButton);
        TextView descriptionTextView = view.findViewById(R.id.sellingDescriptionTv);

        makeBothPartsBoldAndSize(descriptionTextView);
        boolean isGuest = requireActivity().getIntent().getBooleanExtra("isGuest", false);

        openAddPostDialogButton.setOnClickListener(v -> {
            if (isGuest) {
                // Display a toast if the user is a guest
                Toast.makeText(requireContext(), "You are in guest mode. Adding posts is restricted.", Toast.LENGTH_SHORT).show();
            } else {
                if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                    AddPostDialog addPostDialog = new AddPostDialog();
                    Bundle args = new Bundle();
                    args.putString("tabContext", "Selling"); // Pass "Selling" context
                    addPostDialog.setArguments(args);
                    addPostDialog.show(requireActivity().getSupportFragmentManager(), "AddPostDialog");
                } else {
                    // Log or handle cases where the fragment is not properly attached
                    android.util.Log.e("SellingFragment", "Fragment not attached to an activity");
                }
            }
        });


        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            postsReference = FirebaseDatabase.getInstance().getReference("posts");}

        // Initialize PostAdapter
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList,isGuest);
        recyclerView.setAdapter(postAdapter);

        // Fetch posts created by the user in the "Selling" category
        fetchUserSellingPosts();

        return view;
    }

    private void makeBothPartsBoldAndSize(TextView textView) {
        String fullText = textView.getText().toString();
        String part1 = "Looking to sell your services?";
        String part2 = "Start earning and building connections today!";

        int startIndex1 = fullText.indexOf(part1);
        int endIndex1 = startIndex1 + part1.length();
        int startIndex2 = fullText.indexOf(part2);
        int endIndex2 = startIndex2 + part2.length();

        SpannableString spannableString = new SpannableString(fullText);

        if (startIndex1 != -1) {
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex1, endIndex1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new RelativeSizeSpan(1.5f), startIndex1, endIndex1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        if (startIndex2 != -1) {
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex2, endIndex2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannableString);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
    }
    private void fetchUserSellingPosts() {
        // Check if the user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Guest mode: Do nothing
            return;
        }
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference postsReference = FirebaseDatabase.getInstance().getReference("posts");
        postsReference.orderByChild("userId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> userSellingPosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null && "selling".equalsIgnoreCase(post.getCategory())) {
                        post.setPostId(postSnapshot.getKey()); // Set the postId from the key
                        userSellingPosts.add(post);
                    }
                }

                // Update RecyclerView with the filtered posts
                PostAdapter adapter = new PostAdapter(userSellingPosts,isGuest);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("SellingFragment", "Failed to load posts: " + error.getMessage());
            }
        });
    }

}
