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

        // Initialize views
        titleEditText = view.findViewById(R.id.titleEditText);
        bioEditText = view.findViewById(R.id.bioEditText);
        priceEditText = view.findViewById(R.id.priceEditText);
        saveButton = view.findViewById(R.id.saveButton);

        // Button click to save post
        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString();
            String bio = bioEditText.getText().toString();
            String price = priceEditText.getText().toString();

            // Handle saving the post (e.g., to a database or API)
            dismiss();  // Close the dialog
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            // Get dialog's window and set custom layout parameters
            ViewGroup.LayoutParams layoutParams = getDialog().getWindow().getAttributes();

            // Set width to MATCH_PARENT
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;

            // Set height to a custom value (e.g., 80% of the screen height)
            layoutParams.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);

            // Apply top and bottom margins, for example, 20% of screen height
            getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) layoutParams);

            // Set dialog gravity to center
            getDialog().getWindow().setGravity(Gravity.CENTER);
        }
    }
}
