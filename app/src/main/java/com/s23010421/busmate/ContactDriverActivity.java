package com.s23010421.busmate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ContactDriverActivity for EEI4369 BusMate Project
 * Allows passengers to communicate with bus drivers
 * Author: V.P.N. Hansaka (S23010421)
 */
public class ContactDriverActivity extends AppCompatActivity {

    private static final String TAG = "ContactDriver";

    // UI Components
    private Button buttonBack;
    private TextView textViewDriverName;
    private TextView textViewRouteInfo;
    private TextView textViewDriverStatus;
    private LinearLayout layoutQuickMessages;
    private LinearLayout layoutMessageHistory;
    private EditText editTextMessage;
    private Button buttonSendMessage;
    private Button buttonEmergencyContact;
    private CardView cardViewDriverInfo;

    // Driver and Route Data
    private String driverName;
    private String routeNumber;
    private String busId;
    private List<ChatMessage> messageHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "ContactDriverActivity onCreate started");
            setContentView(R.layout.activity_contact_driver);

            // Get data from intent
            getDriverDataFromIntent();

            // Initialize views
            initializeViews();

            // Setup initial data
            setupDriverInfo();

            // Setup click listeners
            setupClickListeners();

            // Setup quick messages
            setupQuickMessages();

            // Load message history
            loadMessageHistory();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorAndFinish("Error loading contact driver screen");
        }
    }

    /**
     * Get driver data from intent
     */
    private void getDriverDataFromIntent() {
        try {
            Intent intent = getIntent();
            driverName = intent.getStringExtra("driver_name");
            routeNumber = intent.getStringExtra("route_number");
            busId = intent.getStringExtra("bus_id");

            // Set defaults if data is missing
            if (driverName == null) driverName = "Kamal Silva";
            if (routeNumber == null) routeNumber = "245";
            if (busId == null) busId = "CM-5847";

            Log.d(TAG, "Driver data loaded - Name: " + driverName + ", Route: " + routeNumber);

        } catch (Exception e) {
            Log.e(TAG, "Error getting driver data: " + e.getMessage(), e);
            // Set default values
            driverName = "Kamal Silva";
            routeNumber = "245";
            busId = "CM-5847";
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        buttonBack = findViewById(R.id.buttonBack);
        textViewDriverName = findViewById(R.id.textViewDriverName);
        textViewRouteInfo = findViewById(R.id.textViewRouteInfo);
        textViewDriverStatus = findViewById(R.id.textViewDriverStatus);
        layoutQuickMessages = findViewById(R.id.layoutQuickMessages);
        layoutMessageHistory = findViewById(R.id.layoutMessageHistory);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        buttonEmergencyContact = findViewById(R.id.buttonEmergencyContact);
        cardViewDriverInfo = findViewById(R.id.cardViewDriverInfo);

        // Initialize message history
        messageHistory = new ArrayList<>();
    }

    /**
     * Setup driver information
     */
    private void setupDriverInfo() {
        try {
            if (textViewDriverName != null) {
                textViewDriverName.setText(driverName);
            }

            if (textViewRouteInfo != null) {
                textViewRouteInfo.setText("Route #" + routeNumber + " Driver");
            }

            if (textViewDriverStatus != null) {
                textViewDriverStatus.setText("Online");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up driver info: " + e.getMessage(), e);
        }
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        try {
            // Back button
            if (buttonBack != null) {
                buttonBack.setOnClickListener(v -> {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
            }

            // Send message button
            if (buttonSendMessage != null) {
                buttonSendMessage.setOnClickListener(v -> sendMessage());
            }

            // Emergency contact button
            if (buttonEmergencyContact != null) {
                buttonEmergencyContact.setOnClickListener(v -> initiateEmergencyContact());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Setup quick message buttons
     */
    private void setupQuickMessages() {
        try {
            if (layoutQuickMessages == null) return;

            String[] quickMessages = {
                    "I am at the bus stop waiting",
                    "How long until you arrive?",
                    "Please wait, I'm running late",
                    "I left something on the bus"
            };

            for (String message : quickMessages) {
                Button quickButton = createQuickMessageButton(message);
                layoutQuickMessages.addView(quickButton);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up quick messages: " + e.getMessage(), e);
        }
    }

    /**
     * Create quick message button
     */
    private Button createQuickMessageButton(String message) {
        Button button = new Button(this);
        button.setText(message);
        button.setTextSize(14);
        button.setTextColor(getResources().getColor(android.R.color.black));
        button.setBackground(createQuickButtonBackground());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> sendQuickMessage(message));

        return button;
    }

    /**
     * Create background for quick message buttons
     */
    private android.graphics.drawable.Drawable createQuickButtonBackground() {
        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setColor(getResources().getColor(android.R.color.white));
        drawable.setStroke(2, getResources().getColor(android.R.color.black));
        drawable.setCornerRadius(8);
        return drawable;
    }

    /**
     * Send quick message
     */
    private void sendQuickMessage(String message) {
        try {
            // Add to message history
            ChatMessage chatMessage = new ChatMessage(message, true, getCurrentTimestamp());
            messageHistory.add(chatMessage);

            // Add driver response (simulated)
            addDriverResponse(message);

            // Update message display
            updateMessageDisplay();

            Toast.makeText(this, "Quick message sent", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error sending quick message: " + e.getMessage(), e);
        }
    }

    /**
     * Send custom message
     */
    private void sendMessage() {
        try {
            if (editTextMessage == null) return;

            String message = editTextMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add to message history
            ChatMessage chatMessage = new ChatMessage(message, true, getCurrentTimestamp());
            messageHistory.add(chatMessage);

            // Clear input
            editTextMessage.setText("");

            // Add driver response (simulated)
            addDriverResponse(message);

            // Update message display
            updateMessageDisplay();

            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error sending message: " + e.getMessage(), e);
        }
    }

    /**
     * Add simulated driver response
     */
    private void addDriverResponse(String userMessage) {
        try {
            String response = generateDriverResponse(userMessage);

            // Add with slight delay to simulate real response
            new android.os.Handler().postDelayed(() -> {
                ChatMessage driverMessage = new ChatMessage(response, false, getCurrentTimestamp());
                messageHistory.add(driverMessage);
                updateMessageDisplay();
            }, 2000);

        } catch (Exception e) {
            Log.e(TAG, "Error adding driver response: " + e.getMessage(), e);
        }
    }

    /**
     * Generate appropriate driver response
     */
    private String generateDriverResponse(String userMessage) {
        String message = userMessage.toLowerCase();

        if (message.contains("waiting") || message.contains("bus stop")) {
            return "Yes, I'm on schedule. ETA is 5 minutes to your stop.";
        } else if (message.contains("how long") || message.contains("arrive")) {
            return "I'll be there in about 3-4 minutes. Thank you for waiting.";
        } else if (message.contains("late") || message.contains("running")) {
            return "No problem, I'll wait for you at the stop.";
        } else if (message.contains("left") || message.contains("forgot")) {
            return "I'll check for any items left behind. What did you leave?";
        } else {
            return "Thank you for your message. I'll assist you shortly.";
        }
    }

    /**
     * Load message history (simulated for demo)
     */
    private void loadMessageHistory() {
        try {
            // Add sample conversation for demo
            messageHistory.add(new ChatMessage(
                    "Hello, I'm heading to the bus stop now. Are you on schedule?",
                    true, "Today, 10:45 AM"
            ));

            messageHistory.add(new ChatMessage(
                    "Yes, I'm on schedule. ETA is 5 minutes to your stop.",
                    false, "Today, 10:45 AM"
            ));

            updateMessageDisplay();

        } catch (Exception e) {
            Log.e(TAG, "Error loading message history: " + e.getMessage(), e);
        }
    }

    /**
     * Update message display
     */
    private void updateMessageDisplay() {
        try {
            if (layoutMessageHistory == null) return;

            // Clear existing messages
            layoutMessageHistory.removeAllViews();

            // Add each message
            for (ChatMessage message : messageHistory) {
                View messageView = createMessageView(message);
                layoutMessageHistory.addView(messageView);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating message display: " + e.getMessage(), e);
        }
    }

    /**
     * Create message view for chat
     */
    private View createMessageView(ChatMessage message) {
        LinearLayout messageContainer = new LinearLayout(this);
        messageContainer.setOrientation(LinearLayout.VERTICAL);
        messageContainer.setPadding(16, 8, 16, 8);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 4, 0, 4);
        messageContainer.setLayoutParams(containerParams);

        // Message text
        TextView messageText = new TextView(this);
        messageText.setText(message.text);
        messageText.setTextSize(16);
        messageText.setPadding(12, 8, 12, 8);

        // Style based on sender
        if (message.isFromUser) {
            messageText.setTextColor(getResources().getColor(android.R.color.white));
            messageText.setBackgroundColor(getResources().getColor(android.R.color.black));
            messageContainer.setGravity(android.view.Gravity.END);
        } else {
            messageText.setTextColor(getResources().getColor(android.R.color.black));
            messageText.setBackgroundColor(getResources().getColor(android.R.color.white));
            messageContainer.setGravity(android.view.Gravity.START);
        }

        // Timestamp
        TextView timestampText = new TextView(this);
        timestampText.setText(message.timestamp);
        timestampText.setTextSize(12);
        timestampText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        timestampText.setPadding(12, 4, 12, 4);

        messageContainer.addView(messageText);
        messageContainer.addView(timestampText);

        return messageContainer;
    }

    /**
     * Initiate emergency contact
     */
    private void initiateEmergencyContact() {
        try {
            // In real implementation, this would call emergency services
            // For demo, show emergency options

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Emergency Contact")
                    .setMessage("This will immediately alert:\n" +
                            "• Bus company emergency line\n" +
                            "• Your emergency contacts\n" +
                            "• Local authorities if needed\n\n" +
                            "Continue with emergency contact?")
                    .setPositiveButton("Yes, Emergency", (dialog, which) -> {
                        // Simulate emergency contact
                        Toast.makeText(this, "Emergency services contacted", Toast.LENGTH_LONG).show();

                        // In real app, would call actual emergency numbers
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:119")); // Emergency number
                        startActivity(callIntent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Error initiating emergency contact: " + e.getMessage(), e);
            Toast.makeText(this, "Error contacting emergency services", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get current timestamp
     */
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("Today, hh:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Show error and finish activity
     */
    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Chat message data class
     */
    private static class ChatMessage {
        String text;
        boolean isFromUser;
        String timestamp;

        ChatMessage(String text, boolean isFromUser, String timestamp) {
            this.text = text;
            this.isFromUser = isFromUser;
            this.timestamp = timestamp;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ContactDriverActivity destroyed");
    }
}
