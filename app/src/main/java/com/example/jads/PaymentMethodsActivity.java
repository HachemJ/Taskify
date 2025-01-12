package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaymentMethodsActivity extends AppCompatActivity {

    private CheckBox cardCheckBox, whishCheckBox;
    private LinearLayout cardOptionLayout, whishOptionLayout;
    private TextView cardDetailsTextView, whishDetailsTextView;

    // Flag to determine if the current user is the poster of the post
    private boolean isSameUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        // Initialize Views and listeners
        isSameUser = getIntent().getBooleanExtra("isSameUser", false);
        String postId = getIntent().getStringExtra("postId"); // Retrieve postId for updating the database
        initializeViews();
        setupListeners();

        if (postId != null && !postId.isEmpty()) {
            fetchPaymentMethods(postId); // Fetch and pre-select payment methods
        }

        // Handle the "Continue" button click
        Button continueButton = findViewById(R.id.continueButton); // Replace with your actual button ID
        continueButton.setOnClickListener(v -> handleContinueButtonClick(postId));

        // Handle back press with OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleContinueButtonClick(postId); // Same logic for back press
            }
        });
    }


    /**
     * Handles the logic for the "Continue" button and back press.
     * Updates the payment methods in the database if `postId` is provided.
     *
     * @param postId The ID of the post to update payment methods for.
     */
    private void handleContinueButtonClick(String postId) {
        // Collect selected payment methods
        boolean cashSelected = cardCheckBox.isChecked();
        boolean whishSelected = whishCheckBox.isChecked();

        // If postId is provided, check for changes and update payment methods in the database
        if (postId != null && !postId.isEmpty()) {
            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId).child("paymentMethods");

            postRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Map<String, Boolean> currentMethods = (Map<String, Boolean>) task.getResult().getValue();

                    // Determine if changes were made
                    boolean cashCurrent = currentMethods != null && Boolean.TRUE.equals(currentMethods.get("cash"));
                    boolean whishCurrent = currentMethods != null && Boolean.TRUE.equals(currentMethods.get("whish"));

                    if (cashSelected != cashCurrent || whishSelected != whishCurrent) {
                        // Update only if there's a change
                        Map<String, Object> updatedPaymentMethods = new HashMap<>();
                        updatedPaymentMethods.put("cash", cashSelected);
                        updatedPaymentMethods.put("whish", whishSelected);

                        postRef.setValue(updatedPaymentMethods)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(this, "Payment methods updated successfully.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to update payment methods.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    // Handle failure in fetching current payment methods
                    Toast.makeText(this, "Failed to fetch current payment methods.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // If postId is not provided, assume new post scenario and proceed
            ArrayList<String> selectedMethods = new ArrayList<>();
            if (cashSelected) selectedMethods.add("cash");
            if (whishSelected) selectedMethods.add("whish");

            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("selectedPaymentMethods", selectedMethods);
            setResult(RESULT_OK, resultIntent);
        }

        finish(); // Finish the activity
    }






    /**
     * Initializes all the views used in the layout.
     */
    private void initializeViews() {
        // Initialize CheckBoxes
        cardCheckBox = findViewById(R.id.cardCheckBox);
        whishCheckBox = findViewById(R.id.whishCheckBox);

        // Initialize option layouts
        cardOptionLayout = findViewById(R.id.cardOptionLayout);
        whishOptionLayout = findViewById(R.id.whishOptionLayout);

        // Initialize details TextViews
        cardDetailsTextView = findViewById(R.id.cashDetailsTextView);
        whishDetailsTextView = findViewById(R.id.whishDetailsTextView);

        // Hide details sections initially
        cardDetailsTextView.setVisibility(View.GONE);
        whishDetailsTextView.setVisibility(View.GONE);
    }
    private void fetchPaymentMethods(String postId) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(postId).child("paymentMethods");

        postRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, Boolean> paymentMethods = (Map<String, Boolean>) task.getResult().getValue();

                // Default to false if data does not exist
                boolean cashAvailable = paymentMethods != null && Boolean.TRUE.equals(paymentMethods.get("cash"));
                boolean whishAvailable = paymentMethods != null && Boolean.TRUE.equals(paymentMethods.get("whish"));

                // Update checkboxes
                cardCheckBox.setChecked(cashAvailable);
                whishCheckBox.setChecked(whishAvailable);
            } else {
                // Default to unchecked
                cardCheckBox.setChecked(false);
                whishCheckBox.setChecked(false);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch payment methods", Toast.LENGTH_SHORT).show();
            // Default to unchecked
            cardCheckBox.setChecked(false);
            whishCheckBox.setChecked(false);
        });
    }


    /**
     * Sets up listeners for the CheckBoxes to toggle the visibility of details.
     */
    private void setupListeners() {
        cardCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDetails(cardDetailsTextView, isChecked));
        whishCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDetails(whishDetailsTextView, isChecked));
    }

    /**
     * Toggles the visibility of a details section based on the CheckBox state.
     *
     * @param detailsTextView The TextView to show or hide.
     * @param isChecked       Whether the CheckBox is checked.
     */
    private void toggleDetails(TextView detailsTextView, boolean isChecked) {
        detailsTextView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }

}
