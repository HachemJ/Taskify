package com.example.jads;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PaymentsAvailable extends AppCompatActivity {

    private CheckBox cardCheckBox, paypalCheckBox;
    private LinearLayout cardOptionLayout, paypalOptionLayout;
    private TextView cardDetailsTextView, paypalDetailsTextView;
    private Button continueButton;
    private String currentUserId;
    private String postTitle;

    // Flag to determine if the current user is the poster of the post
    private boolean isSameUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments_available);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postTitle = getIntent().getStringExtra("postTitle");
        String posterUserId = getIntent().getStringExtra("posterUserId");
        if (posterUserId == null) {
            Toast.makeText(this, "Error: Poster information is missing.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if posterUserId is null
            return;
        }

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
            if (cardCheckBox.isChecked()) {
                // Show confirmation dialog for Cash option
                showConfirmationDialog();
            } else if (paypalCheckBox.isChecked()) {
                // Proceed with other options if needed
                Toast.makeText(this, "Coming soon !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a confirmation dialog when the "Pay with Cash" option is selected.
     */
    private void showConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Confirm Payment Method");
        builder.setMessage("Are you sure you want to proceed with Cash?\n\n•By clicking YES the seller will be notified in the chat section.\n\n• Your safety is our priority, always meet service providers in public and safe areas.\n\n• Verify the details and scope of the service before agreeing to anything.\n \n• Do not continue until you have confirmed that the service meets your demands and expectations.");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            sendAutomatedMessage();
            // For now, do nothing
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss(); // Close the dialog
        });

        builder.show();
    }

    private void sendAutomatedMessage() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        // Fetch the buyer's details
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String fullName = (firstName != null ? firstName : "Unknown") + " " + (lastName != null ? lastName : "User");

                    // Construct the message
                    String messageText = fullName + " is interested in your service with title : '" + postTitle +  "' using Cash method.";

                    // Send the message
                    sendMessageToPoster(messageText);
                } else {
                    Toast.makeText(PaymentsAvailable.this, "Failed to fetch user details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PaymentsAvailable.this, "Error fetching user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends the constructed message to the poster's chat and redirects to ChatActivity.
     *
     * @param messageText The automated message to be sent.
     */
    private void sendMessageToPoster(String messageText) {
        String posterUserId = getIntent().getStringExtra("posterUserId");
        if (posterUserId == null || currentUserId == null) {
            Toast.makeText(this, "Error: Poster information is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        String chatId = currentUserId.compareTo(posterUserId) < 0
                ? currentUserId + "_" + posterUserId
                : posterUserId + "_" + currentUserId;

        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", Map.of(currentUserId, true, posterUserId, true));

        chatRef.updateChildren(chatData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DatabaseReference messageRef = chatRef.child("messages").push();
                Map<String, Object> messageMap = new HashMap<>();
                messageMap.put("senderId", currentUserId);
                messageMap.put("text", messageText);
                messageMap.put("timestamp", ServerValue.TIMESTAMP);

                messageRef.setValue(messageMap).addOnCompleteListener(messageTask -> {
                    if (messageTask.isSuccessful()) {
                        // Update userChats nodes for both users
                        updateUserChats(currentUserId, posterUserId, chatId);
                        updateUserChats(posterUserId, currentUserId, chatId);

                        sendNotificationToOtherUser(posterUserId, currentUserId, messageText);

                        // Redirect to ChatActivity
                        Intent intent = new Intent(PaymentsAvailable.this, ChatActivity.class);
                        intent.putExtra("chatId", chatId);
                        intent.putExtra("otherUserId", posterUserId);
                        startActivity(intent);
                        finish(); // Close PaymentsAvailable activity
                    } else {
                        Toast.makeText(PaymentsAvailable.this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(PaymentsAvailable.this, "Failed to initialize chat.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Generates a unique chat ID based on the two user IDs.
     */
    private String generateChatId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
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
    private void updateUserChats(String userId, String otherUserId, String chatId) {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("userChats").child(userId);

        userChatsRef.child(chatId).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("PaymentsAvailable", "UserChats updated successfully for userId: " + userId);
            } else {
                Log.e("PaymentsAvailable", "Failed to update UserChats for userId: " + userId, task.getException());
            }
        });
    }
    private void sendNotificationToOtherUser(String receiverId, String senderId, String message) {
        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference("users").child(senderId);

        senderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot senderSnapshot) {
                if (senderSnapshot.exists()) {
                    String firstName = senderSnapshot.child("firstName").getValue(String.class);
                    String lastName = senderSnapshot.child("lastName").getValue(String.class);
                    String senderName = ((firstName != null ? firstName : "Unknown") + " " +
                            (lastName != null ? lastName : "")).trim();

                    DatabaseReference receiverTokenRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(receiverId)
                            .child("fcmToken");

                    receiverTokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot tokenSnapshot) {
                            if (tokenSnapshot.exists()) {
                                String token = tokenSnapshot.getValue(String.class);
                                if (token != null) {
                                    SendNotification fcmSender = new SendNotification();
                                    fcmSender.sendPushNotification(
                                            PaymentsAvailable.this,
                                            "New Message from " + senderName,
                                            message,
                                            token
                                    );
                                    Log.d("PaymentsAvailable", "Notification sent with title: New Message from " + senderName);
                                } else {
                                    Log.e("PaymentsAvailable", "FCM token is null for receiver: " + receiverId);
                                }
                            } else {
                                Log.e("PaymentsAvailable", "FCM token does not exist for receiverId: " + receiverId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("PaymentsAvailable", "Failed to fetch FCM token: " + error.getMessage());
                        }
                    });
                } else {
                    Log.e("PaymentsAvailable", "Sender data does not exist for senderId: " + senderId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PaymentsAvailable", "Failed to fetch sender's name: " + error.getMessage());
            }
        });
    }



}
