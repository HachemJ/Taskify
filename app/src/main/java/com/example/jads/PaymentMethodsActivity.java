package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentMethodsActivity extends AppCompatActivity {

    private CheckBox cardCheckBox, paypalCheckBox;
    private LinearLayout cardOptionLayout, paypalOptionLayout;
    private TextView cardDetailsTextView, paypalDetailsTextView;

    // Flag to determine if the current user is the poster of the post
    private boolean isSameUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        // Retrieve the flag to determine the user context
        isSameUser = getIntent().getBooleanExtra("isSameUser", false);

        initializeViews();
        setupListeners();
        updateUIBasedOnUser();
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

        // Hide details sections initially
        cardDetailsTextView.setVisibility(View.GONE);
        paypalDetailsTextView.setVisibility(View.GONE);
    }

    /**
     * Sets up listeners for the CheckBoxes to toggle the visibility of details.
     */
    private void setupListeners() {
        cardCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDetails(cardDetailsTextView, isChecked));
        paypalCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDetails(paypalDetailsTextView, isChecked));
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
        // Update CheckBox labels
        cardCheckBox.setText("Accept Cash");
        paypalCheckBox.setText("Accept Whish Money");

        // Update details content
        cardDetailsTextView.setText("Accepting cash allows the buyer to pay at delivery.");
        paypalDetailsTextView.setText("Accepting Whish Money allows secure payments through the platform.");
    }

    /**
     * Configures the UI for "Pay" mode (when the user is not the poster).
     */
    private void setPayMode() {
        // Update CheckBox labels
        cardCheckBox.setText("Pay with Cash");
        paypalCheckBox.setText("Pay with Whish Money");

        // Update details content
        cardDetailsTextView.setText("Paying with cash allows you to pay at delivery.");
        paypalDetailsTextView.setText("Paying with Whish Money connects to your Whish account for secure payment.");
    }
}
