package com.example.jads;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
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

import com.google.android.material.slider.Slider;

import java.io.IOException;

public class AddPostDialog extends DialogFragment {

    private EditText titleEditText, descriptionEditText, priceEditText;
    private Button saveButton, addImageButton;
    private ImageView postImageView;
    private Slider priceSlider;
    private static final int IMAGE_PICK_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_post_dialog, container, false);

        titleEditText = view.findViewById(R.id.postTitleEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        saveButton = view.findViewById(R.id.saveButton);
        postImageView = view.findViewById(R.id.postImageView);
        addImageButton = view.findViewById(R.id.addImageButton);
        priceSlider = view.findViewById(R.id.settingsMission_changeShakeDif_slider);
        priceEditText = view.findViewById(R.id.editTextNumber);

        // Slider Label Formatter with Dollar Sign
        priceSlider.setLabelFormatter(value -> "$" + (int) value);

        // Apply custom InputFilter to enforce values between 0 and 100
        priceEditText.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});

        // Sync Slider -> EditText
        priceSlider.addOnChangeListener((slider, value, fromUser) -> {
            int intValue = (int) value;
            priceEditText.setText(String.valueOf(intValue));
        });

        // Sync EditText -> Slider with Leading Zeros Trimmed
        priceEditText.addTextChangedListener(new TextWatcher() {
            boolean editing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editing) return; // Avoid recursion
                editing = true;

                try {
                    // Remove leading zeros
                    String text = s.toString().replaceFirst("^0+(?!$)", "");
                    int value = Integer.parseInt(text);

                    // Clamp the value between 0 and 100
                    if (value > 100) value = 100;

                    // Update EditText and Slider
                    priceEditText.setText(String.valueOf(value));
                    priceEditText.setSelection(priceEditText.getText().length());
                    priceSlider.setValue(value);
                } catch (NumberFormatException e) {
                    // Handle invalid input gracefully (e.g., empty string)
                    priceEditText.setText("0");
                    priceEditText.setSelection(priceEditText.getText().length());
                    priceSlider.setValue(0);
                }

                editing = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        saveButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Post saved successfully", Toast.LENGTH_SHORT).show();
            dismiss();
        });

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

    // Custom InputFilter to enforce values between min and max
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
                // Combine the old text with the new text
                String input = dest.subSequence(0, dstart).toString() + source.toString() + dest.subSequence(dend, dest.length());
                int value = Integer.parseInt(input);

                // Check if the value is within range
                if (value >= min && value <= max) {
                    return null; // Accept the input
                }
            } catch (NumberFormatException e) {
                // Reject invalid input
            }

            return ""; // Reject the input
        }
    }
}
