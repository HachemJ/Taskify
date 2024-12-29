package com.example.jads;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;

public class LookingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_looking, container, false);

        // Find the button in the layout
        Button openAddPostDialogButton = view.findViewById(R.id.openAddPostDialogButton);
        TextView descriptionTextView = view.findViewById(R.id.textView2);

        // Make both parts of the text bold, the first larger, and aligned to the left
        makeBothPartsBoldAndSize(descriptionTextView);

        // Set a click listener on the button to open the AddPostDialog
        openAddPostDialogButton.setOnClickListener(v -> {
            // Check if the fragment is attached to an activity
            if (isAdded()) {
                // Create an instance of the AddPostDialog
                AddPostDialog addPostDialog = new AddPostDialog();

                // Show the dialog using the parent fragment manager
                addPostDialog.show(getParentFragmentManager(), "AddPostDialog");
            } else {
                // Log the error if the fragment is not attached
                Log.e("LookingFragment", "Fragment not attached to Activity.");
            }
        });

        return view;
    }

    private void makeBothPartsBoldAndSize(TextView textView) {
        String fullText = textView.getText().toString();

        // Define the parts you want to make bold
        String part1 = "Looking for a service or task?";
        String part2 = "Post now and connect with others to get what you need!";

        // Find the start and end indices for both parts
        int startIndex1 = fullText.indexOf(part1);
        int endIndex1 = startIndex1 + part1.length();

        int startIndex2 = fullText.indexOf(part2);
        int endIndex2 = startIndex2 + part2.length();

        // Apply bold and larger font size to both parts of the text
        SpannableString spannableString = new SpannableString(fullText);

        // Bold the first part and make it larger
        if (startIndex1 != -1) {
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex1, endIndex1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new RelativeSizeSpan(1.5f), startIndex1, endIndex1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  // 1.5x size
        }

        // Bold the second part
        if (startIndex2 != -1) {
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex2, endIndex2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Set the modified spannable text back to the TextView
        textView.setText(spannableString);

        // Align the text to the left
        textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
    }
}
