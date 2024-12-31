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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SellingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selling, container, false);

        Button openAddPostDialogButton = view.findViewById(R.id.openAddPostDialogButton);
        TextView descriptionTextView = view.findViewById(R.id.sellingDescriptionTv);

        makeBothPartsBoldAndSize(descriptionTextView);

        openAddPostDialogButton.setOnClickListener(v -> {
            AddPostDialog addPostDialog = new AddPostDialog();
            Bundle args = new Bundle();
            args.putString("tabContext", "Selling"); // Pass "Selling" context
            addPostDialog.setArguments(args);
            addPostDialog.show(getParentFragmentManager(), "AddPostDialog");
        });

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
}
