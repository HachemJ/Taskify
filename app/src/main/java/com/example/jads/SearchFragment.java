package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference postsReference;
    private boolean isGuest;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        isGuest = getArguments() != null && getArguments().getBoolean("isGuest", false);
        Log.d("SearchFragment", "isGuest retrieved: " + isGuest);
        postAdapter = new PostAdapter(postList, isGuest);
        recyclerView.setAdapter(postAdapter);

        postsReference = FirebaseDatabase.getInstance().getReference("posts");

        fetchPosts();
        setupSearchView();

        // Initialize and set up the "Open Chats" button
        Button openChatsButton = view.findViewById(R.id.openChatsButton);
        openChatsButton.setOnClickListener(v -> {
            if (isGuest) {
                // Display a toast if the user is a guest
                Toast.makeText(requireContext(), "You are in guest mode. Chat access is restricted.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getContext(), ChatsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }


    private void fetchPosts() {
        postsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        // Set the postId using the Firebase key
                        post.setPostId(postSnapshot.getKey());
                        postList.add(post);
                    }
                }
                Collections.shuffle(postList);
                // Update adapter to reflect the complete post list
                isGuest = getArguments() != null && getArguments().getBoolean("isGuest", false);
                postAdapter = new PostAdapter(postList,isGuest);
                recyclerView.setAdapter(postAdapter);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                postAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                postAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}
