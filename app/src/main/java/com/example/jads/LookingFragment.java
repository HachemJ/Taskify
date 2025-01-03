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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LookingFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference postsReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_looking, container, false);

        // Existing UI elements
        Button openAddPostDialogButton = view.findViewById(R.id.openAddPostDialogButton);
        TextView descriptionTextView = view.findViewById(R.id.lookingDescriptionTv);

        makeBothPartsBoldAndSize(descriptionTextView);

        openAddPostDialogButton.setOnClickListener(v -> {
            try {
                if (requireActivity().getSupportFragmentManager().findFragmentByTag("AddPostDialog") == null) {
                    AddPostDialog addPostDialog = new AddPostDialog();
                    Bundle args = new Bundle();
                    args.putString("tabContext", "Looking"); // Pass "Looking" context
                    addPostDialog.setArguments(args);
                    addPostDialog.show(requireActivity().getSupportFragmentManager(), "AddPostDialog");
                } else {
                    android.util.Log.w("LookingFragment", "AddPostDialog is already open.");
                }
            } catch (IllegalStateException e) {
                android.util.Log.e("LookingFragment", "Error showing AddPostDialog", e);
                Toast.makeText(requireContext(), "Cannot open Add Post dialog. Try again later.", Toast.LENGTH_SHORT).show();
            }
        });



        // RecyclerView setup
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        recyclerView.setAdapter(postAdapter);

        // Fetch posts
        fetchLookingPosts();

        return view;
    }

    private void fetchLookingPosts() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference postsReference = FirebaseDatabase.getInstance().getReference("posts");
        postsReference.orderByChild("userId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> userLookingPosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null && "looking".equalsIgnoreCase(post.getCategory())) {
                        post.setPostId(postSnapshot.getKey()); // Set the postId from the key
                        userLookingPosts.add(post);
                    }
                }

                // Update RecyclerView with the filtered posts
                postAdapter.updatePosts(userLookingPosts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                android.util.Log.e("LookingFragment", "Failed to load posts: " + error.getMessage());
            }
        });
    }

    private void makeBothPartsBoldAndSize(TextView textView) {
        String fullText = textView.getText().toString();
        String part1 = "Looking for a service or task?";
        String part2 = "Post now and connect with others to get what you need!";

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
}
