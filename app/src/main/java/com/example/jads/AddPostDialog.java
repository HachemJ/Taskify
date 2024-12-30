package com.example.jads;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPostDialog extends DialogFragment {

    private EditText titleEditText, descriptionEditText, priceEditText, newTagEditText;
    private Button saveButton, addImageButton, addTagButton;
    private ImageView postImageView;
    private Slider priceSlider;
    private FlexboxLayout tagContainer;
    private static final int IMAGE_PICK_CODE = 100;

    private final List<String> predefinedTags = new ArrayList<>();

    private FirebaseAuth auth;
    private DatabaseReference postsReference;
    private String postCategory; // Variable to store post category
    private StorageReference storageReference;
    private Uri selectedImageUri;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_post_dialog, container, false);

        // Initialize Firebase Auth and Database Reference
        auth = FirebaseAuth.getInstance();
        postsReference = FirebaseDatabase.getInstance().getReference("posts");
        storageReference = FirebaseStorage.getInstance().getReference("post_images");

        // Retrieve post category from arguments
        if (getArguments() != null) {
            postCategory = getArguments().getString("postCategory", "Unknown");
        }

        // Initialize UI components
        titleEditText = view.findViewById(R.id.postTitleEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        saveButton = view.findViewById(R.id.saveButton);
        postImageView = view.findViewById(R.id.postImageView);
        addImageButton = view.findViewById(R.id.addImageButton);
        priceSlider = view.findViewById(R.id.settingsMission_changeShakeDif_slider);
        priceEditText = view.findViewById(R.id.editTextNumber);
        newTagEditText = view.findViewById(R.id.newTagEditText);
        addTagButton = view.findViewById(R.id.addTagButton);
        tagContainer = view.findViewById(R.id.tagContainer);

        // Add initial tags
        predefinedTags.add("test1");
        predefinedTags.add("test2");
        populatePredefinedTags(); // Add the initial tags to the UI

        // Slider Label Formatter with Dollar Sign
        priceSlider.setLabelFormatter(value -> "$" + (int) value);

        // Apply custom InputFilter to enforce values between 0 and 100
        priceEditText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});

        // Sync Slider -> EditText
        priceSlider.addOnChangeListener((slider, value, fromUser) -> {
            int intValue = (int) value;
            if (!priceEditText.getText().toString().equals(String.valueOf(intValue))) {
                priceEditText.setText(String.valueOf(intValue));
            }
        });

        // Sync EditText -> Slider with Leading Zeros Trimmed
        priceEditText.addTextChangedListener(new TextWatcher() {
            boolean editing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editing) return;
                editing = true;

                try {
                    String text = s.toString().replaceFirst("^0+(?!$)", "");
                    if (text.isEmpty()) text = "0";
                    int value = Integer.parseInt(text);
                    if (value > 100) value = 100;
                    priceEditText.setText(String.valueOf(value));
                    priceEditText.setSelection(priceEditText.getText().length());
                    priceSlider.setValue(value);
                } catch (NumberFormatException e) {
                    priceEditText.setText("0");
                    priceEditText.setSelection(priceEditText.getText().length());
                    priceSlider.setValue(0);
                }

                editing = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add Tag Button Click Listener
        addTagButton.setOnClickListener(v -> {
            String tagText = newTagEditText.getText().toString().trim();
            if (!tagText.isEmpty() && !predefinedTags.contains(tagText)) {
                predefinedTags.add(tagText);
                addTagToContainer(tagText);
                newTagEditText.setText("");
            }
        });

        saveButton.setOnClickListener(v -> savePost());

        addImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                postImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            ViewGroup.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
            getDialog().getWindow().setAttributes((WindowManager.LayoutParams) layoutParams);
            getDialog().getWindow().setGravity(Gravity.CENTER);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void populatePredefinedTags() {
        for (String tag : predefinedTags) {
            addTagToContainer(tag);
        }
    }

    private void addTagToContainer(String tagText) {
        LinearLayout tagLayout = new LinearLayout(getContext());
        tagLayout.setOrientation(LinearLayout.HORIZONTAL);
        tagLayout.setPadding(8, 8, 8, 8);
        tagLayout.setBackgroundResource(R.drawable.tag_background);
        tagLayout.setGravity(Gravity.CENTER_VERTICAL);

        TextView tagName = new TextView(getContext());
        tagName.setText(tagText);
        tagName.setTextColor(getResources().getColor(android.R.color.white));
        tagName.setPadding(8, 0, 8, 0);
        tagName.setTextSize(14);

        ImageView closeButton = new ImageView(getContext());
        closeButton.setImageResource(android.R.drawable.ic_delete);
        closeButton.setPadding(8, 8, 8, 8);
        closeButton.setOnClickListener(v -> {
            predefinedTags.remove(tagText);
            tagContainer.removeView(tagLayout);
        });

        tagLayout.addView(tagName);
        tagLayout.addView(closeButton);
        tagContainer.addView(tagLayout);
    }

    private void savePost() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(getContext(), "You must be logged in to save a post", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        List<String> tags = new ArrayList<>(predefinedTags);

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(price)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String postId = postsReference.push().getKey();

        if (postId != null) {
            if (selectedImageUri != null) {
                // Reference to store the image
                StorageReference imageRef = storageReference.child("post_images/" + postId + ".jpg");

                // Upload the image to Storage
                imageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Call savePostDetails with the image URL
                            savePostDetails(postId, userId, title, description, price, tags, uri.toString());
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Save without an image URL
                savePostDetails(postId, userId, title, description, price, tags, null);
            }
        }
    }


    private void savePostDetails(String postId, String userId, String title, String description, String price, List<String> tags, String imageUrl) {
        Map<String, Object> postDetails = new HashMap<>();
        postDetails.put("userId", userId);
        postDetails.put("title", title);
        postDetails.put("description", description);
        postDetails.put("price", price);
        postDetails.put("tags", tags);
        postDetails.put("category", postCategory); // Add the post category
        postDetails.put("timestamp", System.currentTimeMillis()); // Add the timestamp
        if (imageUrl != null) {
            postDetails.put("imageUrl", imageUrl); // Save the image URL
        }

        postsReference.child(postId).setValue(postDetails)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Post saved successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed to save post", Toast.LENGTH_SHORT).show();
                    }
                });
    }





    private static class InputFilterMinMax implements InputFilter {
        private final int min;
        private final int max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                String input = dest.subSequence(0, dstart).toString() + source.toString() + dest.subSequence(dend, dest.length());
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return null;
                }
            } catch (NumberFormatException ignored) {}
            return "";
        }
    }
}