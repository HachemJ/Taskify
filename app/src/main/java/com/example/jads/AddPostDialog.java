package com.example.jads;

import android.content.Intent;
import android.content.res.ColorStateList;
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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

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
    private Button saveButton, addImageButton, addTagButton, paymentMethodButton;
    private ImageView postImageView;
    private Slider priceSlider;
    private LinearLayout tagContainer;
    private static final int IMAGE_PICK_CODE = 100;
    private static final int PAYMENT_METHODS_REQUEST_CODE = 101;

    private final List<String> Tags = new ArrayList<>();
    private final List<String> selectedPaymentMethods = new ArrayList<>();

    private FirebaseAuth auth;
    private DatabaseReference postsReference;
    private StorageReference storageReference;

    private CardView dialogCardView;
    private String tabContext;
    private Uri selectedImageUri;
    private Map<String, Boolean> paymentMethods = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_post_dialog, container, false);

        // Initialize Firebase Auth and Database Reference
        auth = FirebaseAuth.getInstance();
        postsReference = FirebaseDatabase.getInstance().getReference("posts");
        storageReference = FirebaseStorage.getInstance().getReference("post_images");

        // Retrieve post category and tab context from arguments
        if (getArguments() != null) {
            tabContext = getArguments().getString("tabContext", "Unknown");
        }

        // Initialize UI components
        dialogCardView = view.findViewById(R.id.CardView);
        titleEditText = view.findViewById(R.id.postTitleEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        saveButton = view.findViewById(R.id.saveButton);
        postImageView = view.findViewById(R.id.postImageView);
        addImageButton = view.findViewById(R.id.addImageButton);
        priceSlider = view.findViewById(R.id.slider);
        priceEditText = view.findViewById(R.id.editTextNumber);
        newTagEditText = view.findViewById(R.id.newTagEditText);
        addTagButton = view.findViewById(R.id.addTagButton);
        paymentMethodButton = view.findViewById(R.id.paymentMethodsButton);
        tagContainer = view.findViewById(R.id.tagContainer);

        titleEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(44)});
        descriptionEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(192)});
        newTagEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(15) // Limit input to 15 characters
        });

        // Apply dynamic styling based on tab context
        applyStylingBasedOnTabContext();

        // Slider Label Formatter with Dollar Sign
        priceSlider.setLabelFormatter(value -> "$" + (int) value);

        // Apply custom InputFilter to enforce values between 0 and 100
        priceEditText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});

        // Sync Slider -> EditText
        syncSliderWithEditText();

        // Add tag functionality
        addTagButton.setOnClickListener(v -> {
            String tagText = newTagEditText.getText().toString().trim();

            if (!tagText.isEmpty() && !Tags.contains(tagText)) {
                if (Tags.size() < 2) { // Allow only up to 2 tags
                    Tags.add(tagText);
                    addTagToContainer(tagText);
                    newTagEditText.setText("");
                } else {
                    Toast.makeText(getContext(), "You can only add up to 2 tags.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Save post functionality
        saveButton.setOnClickListener(v -> {
            if (isAdded()) { // Ensure the fragment is attached to the activity
                savePost();
            } else {
                Toast.makeText(getContext(), "Fragment not attached. Try again later.", Toast.LENGTH_SHORT).show();
            }
        });

        // Add image functionality
        addImageButton.setOnClickListener(v -> {
            if (isAdded()) { // Ensure the fragment is attached to the activity
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_PICK_CODE);
            } else {
                Toast.makeText(getContext(), "Fragment not attached. Try again later.", Toast.LENGTH_SHORT).show();
            }
        });

        paymentMethodButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PaymentMethodsActivity.class);
            startActivityForResult(intent, PAYMENT_METHODS_REQUEST_CODE);
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
        } else if (requestCode == PAYMENT_METHODS_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            paymentMethods.clear();

            List<String> selectedMethods = data.getStringArrayListExtra("selectedPaymentMethods");
            if (selectedMethods != null) {
                // Update the payment methods map based on user selections
                paymentMethods.put("cash", selectedMethods.contains("cash"));
                paymentMethods.put("whish", selectedMethods.contains("whish"));

                // Show a Toast message to confirm selection
                Toast.makeText(getContext(), "Payment methods updated", Toast.LENGTH_SHORT).show();
            } else {
                // Handle case where no methods were selected
                Toast.makeText(getContext(), "No payment methods selected", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            ViewGroup.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);
            getDialog().getWindow().setAttributes((WindowManager.LayoutParams) layoutParams);
            getDialog().getWindow().setGravity(Gravity.CENTER);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void applyStylingBasedOnTabContext() {
        if ("Selling".equalsIgnoreCase(tabContext)) {
            dialogCardView.setCardBackgroundColor(getResources().getColor(R.color.dark_blue));
            saveButton.setBackgroundColor(getResources().getColor(R.color.black));
            saveButton.setTextColor(getResources().getColor(R.color.white));
            addTagButton.setBackgroundColor(getResources().getColor(R.color.black));
            paymentMethodButton.setBackgroundColor(getResources().getColor(R.color.black));
            addImageButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            descriptionEditText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            priceSlider.setTrackActiveTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
            priceSlider.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
        } else { // Default to "Looking"
            dialogCardView.setCardBackgroundColor(getResources().getColor(R.color.black));
            saveButton.setBackgroundColor(getResources().getColor(R.color.dark_blue));
            saveButton.setTextColor(getResources().getColor(R.color.white));
            addTagButton.setBackgroundColor(getResources().getColor(R.color.dark_blue));
            paymentMethodButton.setBackgroundColor(getResources().getColor(R.color.dark_blue));
            addImageButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark_blue)));
            descriptionEditText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark_blue)));
            priceSlider.setTrackActiveTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark_blue)));
            priceSlider.setThumbTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark_blue)));
        }
    }

    private void syncSliderWithEditText() {
        priceSlider.addOnChangeListener((slider, value, fromUser) -> {
            int intValue = (int) value;
            if (!priceEditText.getText().toString().equals(String.valueOf(intValue))) {
                priceEditText.setText(String.valueOf(intValue));
            }
        });

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
                    int value = text.isEmpty() ? 0 : Integer.parseInt(text);
                    value = Math.min(value, 100);
                    priceEditText.setText(String.valueOf(value));
                    priceSlider.setValue(value);
                } catch (NumberFormatException e) {
                    priceEditText.setText("0");
                    priceSlider.setValue(0);
                }

                editing = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void addTagToContainer(String tagText) {
        LinearLayout tagLayout = new LinearLayout(getContext());
        tagLayout.setOrientation(LinearLayout.HORIZONTAL);
        tagLayout.setPadding(8, 8, 8, 8);
        tagLayout.setBackgroundResource(R.drawable.tag_background);
        tagLayout.setGravity(Gravity.CENTER_VERTICAL);

        TextView tagName = new TextView(getContext());
        tagName.setText(tagText);
        tagName.setTextColor(getResources().getColor(R.color.medium_gray));
        tagName.setPadding(8, 0, 8, 0);
        tagName.setTextSize(14);

        ImageView closeButton = new ImageView(getContext());
        closeButton.setImageResource(android.R.drawable.ic_delete);
        closeButton.setPadding(8, 8, 8, 8);

        if ("Selling".equalsIgnoreCase(tabContext)) {
            closeButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.black)));
        } else if ("Looking".equalsIgnoreCase(tabContext)) {
            closeButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.dark_blue)));
        }

        closeButton.setOnClickListener(v -> {
            Tags.remove(tagText);
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

        // Check if any payment method is selected
        if (!paymentMethods.containsValue(true)) {
            Toast.makeText(getContext(), "Please select at least one payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        List<String> tags = new ArrayList<>(Tags);

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(price)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String postId = postsReference.push().getKey();

        if (postId != null) {
            if (selectedImageUri != null) {
                StorageReference imageRef = storageReference.child("post_images/" + postId + ".jpg");

                imageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            savePostDetails(postId, userId, title, description, price, tags, uri.toString());
                        }))
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        });
            } else {
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
        postDetails.put("paymentMethods", selectedPaymentMethods);
        postDetails.put("category", tabContext);
        postDetails.put("timestamp", System.currentTimeMillis());
        postDetails.put("paymentMethods", paymentMethods);
        if (imageUrl != null) {
            postDetails.put("imageUrl", imageUrl);
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
