package com.example.jads;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;

public class AddPostDialog extends DialogFragment {

    private EditText titleEditText, descriptionEditText, priceEditText;
    private Button saveButton, addImageButton; // Added addImageButton
    private ImageView postImageView;          // Added postImageView
    private static final int IMAGE_PICK_CODE = 100; // Request code for image selection

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_post_dialog, container, false);

        // Initialize existing views
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        saveButton = view.findViewById(R.id.saveButton);

        // Initialize new views for the image feature
        postImageView = view.findViewById(R.id.postImageView);
        addImageButton = view.findViewById(R.id.addImageButton);

        // Save button functionality (unchanged)
        saveButton.setOnClickListener(v -> {
            String title = titleEditText != null ? titleEditText.getText().toString() : "";
            String bio = descriptionEditText.getText().toString();
            String price = priceEditText != null ? priceEditText.getText().toString() : "";

            // Handle saving the post
            dismiss();
        });

        // Add Image Button functionality
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
            Uri selectedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);

                // Set the selected image in the ImageView
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

            // Set width to MATCH_PARENT
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

            // Set height to a custom value (e.g., 85% of the screen height)
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);

            getDialog().getWindow().setAttributes((WindowManager.LayoutParams) layoutParams);
            getDialog().getWindow().setGravity(Gravity.CENTER);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
