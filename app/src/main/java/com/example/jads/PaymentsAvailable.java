package com.example.jads;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PaymentsAvailable extends AppCompatActivity {

    private CheckBox cardCheckBox, paypalCheckBox;
    private LinearLayout cardOptionLayout, paypalOptionLayout;
    private TextView cardDetailsTextView, paypalDetailsTextView;
    private Button continueButton;

    // Flag to determine if the current user is the poster of the post
    private boolean isSameUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_available);

        // Retrieve postId to fetch available payment methods
        String postId = getIntent().getStringExtra("postId");

        initializeViews();
        setupListeners();
        updateContinueButtonState();

        // Fetch available payment methods for the post
        if (postId != null) {
            fetchAvailablePaymentMethods(postId);
        }
    }

    /**
     * Initializes all the views used in the layout.
     */
    private void initializeViews() {
        // Initialize CheckBoxes
        cardCheckBox = findViewById(R.id.cardCheckBox);
        paypalCheckBox = findViewById(R.id.paypalCheckBox);

        // Initialize option layouts
        cardOptionLayout = findViewById(R.id.cardOptionLayout);
        paypalOptionLayout = findViewById(R.id.paypalOptionLayout);

        // Initialize details TextViews
        cardDetailsTextView = findViewById(R.id.cashDetailsTextView);
        paypalDetailsTextView = findViewById(R.id.paypalDetailsTextView);

        // Initialize the Continue button
        continueButton = findViewById(R.id.continueButton);

        // Hide details sections initially
        cardDetailsTextView.setVisibility(View.GONE);
        paypalDetailsTextView.setVisibility(View.GONE);

        // Initially, disable the continue button
        setContinueButtonEnabled(false);
    }

    /**
     * Sets up listeners for the CheckBoxes and the "Continue" button.
     */
    private void setupListeners() {
        cardCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                paypalCheckBox.setChecked(false); // Deselect PayPal if Card is selected
            }
            toggleDetails(cardDetailsTextView, isChecked);
            updateContinueButtonState();
        });

        paypalCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cardCheckBox.setChecked(false); // Deselect Card if PayPal is selected
            }
            toggleDetails(paypalDetailsTextView, isChecked);
            updateContinueButtonState();
        });

        continueButton.setOnClickListener(v -> {
            if (cardCheckBox.isChecked() || paypalCheckBox.isChecked()) {
                Toast.makeText(this, "Proceeding to next step...", Toast.LENGTH_SHORT).show();
                // Navigate to the next screen or perform the desired action
            }
        });
    }


    /**
     * Updates the state of the "Continue" button based on the selected payment methods.
     */
    private void updateContinueButtonState() {
        boolean isAnyPaymentMethodSelected = cardCheckBox.isChecked() || paypalCheckBox.isChecked();
        setContinueButtonEnabled(isAnyPaymentMethodSelected);
    }

    /**
     * Enables or disables the "Continue" button and changes its appearance.
     *
     * @param isEnabled Whether the button should be enabled.
     */
    private void setContinueButtonEnabled(boolean isEnabled) {
        continueButton.setEnabled(isEnabled);
        continueButton.setBackgroundTintList(getResources().getColorStateList(
                isEnabled ? android.R.color.black : android.R.color.darker_gray
        ));
        continueButton.setTextColor(getResources().getColor(
                isEnabled ? android.R.color.white : android.R.color.black
        ));
    }

    /**
     * Fetches available payment methods for the given postId from the database.
     *
     * @param postId The ID of the post to fetch payment methods for.
     */
    private void fetchAvailablePaymentMethods(String postId) {
        DatabaseReference paymentMethodsRef = FirebaseDatabase.getInstance()
                .getReference("posts")
                .child(postId)
                .child("paymentMethods");

        paymentMethodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Boolean cash = snapshot.child("cash").getValue(Boolean.class);
                    Boolean whish = snapshot.child("whish").getValue(Boolean.class);

                    // Update CheckBox states and disable if unavailable
                    if (cash != null) {
                        cardCheckBox.setChecked(cash);
                        cardCheckBox.setEnabled(cash);
                        cardCheckBox.setAlpha(cash ? 1.0f : 0.5f); // Grey out if unavailable
                    }

                    if (whish != null) {
                        paypalCheckBox.setChecked(whish);
                        paypalCheckBox.setEnabled(whish);
                        paypalCheckBox.setAlpha(whish ? 1.0f : 0.5f); // Grey out if unavailable
                    }
                } else {
                    Toast.makeText(PaymentsAvailable.this, "No payment methods available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentsAvailable.this, "Failed to fetch payment methods: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    /**
     * Updates the UI based on whether the current user is the poster of the post.
     */
    private void updateUIBasedOnUser() {
        if (isSameUser) {
            setAcceptMode();
        } else {
            setPayMode();
        }
    }

    /**
     * Configures the UI for "Accept" mode (when the user is the poster).
     */
    private void setAcceptMode() {
        cardCheckBox.setText("Accept Cash");
        paypalCheckBox.setText("Accept Whish Money");

        cardDetailsTextView.setText("Accepting cash allows the buyer to pay at delivery.");
        paypalDetailsTextView.setText("Accepting Whish Money allows secure payments through the platform.");
    }

    /**
     * Configures the UI for "Pay" mode (when the user is not the poster).
     */
    private void setPayMode() {
        cardCheckBox.setText("Pay with Cash");
        paypalCheckBox.setText("Pay with Whish Money");

        cardDetailsTextView.setText("Paying with cash allows you to pay at delivery.");
        paypalDetailsTextView.setText("Paying with Whish Money connects to your Whish account for secure payment.");
    }
}
