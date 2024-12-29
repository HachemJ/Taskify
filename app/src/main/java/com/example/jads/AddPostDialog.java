package com.example.jads;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class AddPostDialog extends DialogFragment {

    private EditText titleEditText, bioEditText, priceEditText;
    private Button saveButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for the dialog
        View view = inflater.inflate(R.layout.activity_add_post_dialog, container, false);

        // Initialize the views
        titleEditText = view.findViewById(R.id.titleEditText);
        bioEditText = view.findViewById(R.id.bioEditText);
        priceEditText = view.findViewById(R.id.priceEditText);
        saveButton = view.findViewById(R.id.saveButton);

        // Set up the save button action
        saveButton.setOnClickListener(v -> {
            // Handle saving the post
            String title = titleEditText.getText().toString();
            String bio = bioEditText.getText().toString();
            String price = priceEditText.getText().toString();

            // Here you can handle the logic to save the data (e.g., database or API call)
            dismiss();  // Close the dialog
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Ensure the dialog has the correct width and is centered
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setGravity(Gravity.CENTER);  // Center the dialog
        }
    }
}
