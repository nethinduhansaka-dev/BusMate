package com.s23010421.busmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * MoreActivity - Comprehensive settings and account management screen
 * Provides access to all app features, settings, and logout functionality
 */
public class MoreActivity extends AppCompatActivity {

    // UI component declarations
    private TextView textViewUserName;
    private TextView textViewUserEmail;
    private TextView textViewMembershipStatus;
    private TextView textViewPointsBalance;
    private ImageView imageViewProfilePicture;

    // FIXED: Remove duplicate variable declarations
    // Individual menu items (removed duplicates)
    private LinearLayout layoutUserProfile;
    private LinearLayout layoutPasswordRecovery;
    private LinearLayout layoutMembershipStatus;
    private LinearLayout layoutLiveBusTracking;
    private LinearLayout layoutBusRouteInfo;
    private LinearLayout layoutTicketPurchase;
    private LinearLayout layoutPaymentGateway;
    private LinearLayout layoutTripHistory;
    private LinearLayout layoutInteractiveMap;
    private LinearLayout layoutEmergencyPanel;
    private LinearLayout layoutNotificationCenter;
    private LinearLayout layoutRatingInterface;
    private LinearLayout layoutAppAppearance;
    private LinearLayout layoutPrivacySecurity;
    private LinearLayout layoutHelpAndSupport; // FIXED: Renamed to avoid conflict
    private LinearLayout layoutAboutBusMate;

    // Logout button
    private CardView cardViewLogout;

    // Database and session management
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "BusMatePrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        // Initialize database and preferences
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize UI components
        initializeViews();

        // Load user profile data
        loadUserProfileData();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        // Header components
        textViewUserName = findViewById(R.id.textViewUserName);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        textViewMembershipStatus = findViewById(R.id.textViewMembershipStatus);
        textViewPointsBalance = findViewById(R.id.textViewPointsBalance);
        imageViewProfilePicture = findViewById(R.id.imageViewProfilePicture);

        // Individual menu items
        layoutUserProfile = findViewById(R.id.layoutUserProfile);
        layoutPasswordRecovery = findViewById(R.id.layoutPasswordRecovery);
        layoutMembershipStatus = findViewById(R.id.layoutMembershipStatus);
        layoutLiveBusTracking = findViewById(R.id.layoutLiveBusTracking);
        layoutBusRouteInfo = findViewById(R.id.layoutBusRouteInfo);
        layoutTicketPurchase = findViewById(R.id.layoutTicketPurchase);
        layoutPaymentGateway = findViewById(R.id.layoutPaymentGateway);
        layoutTripHistory = findViewById(R.id.layoutTripHistory);
        layoutInteractiveMap = findViewById(R.id.layoutInteractiveMap);
        layoutEmergencyPanel = findViewById(R.id.layoutEmergencyPanel);
        layoutNotificationCenter = findViewById(R.id.layoutNotificationCenter);
        layoutRatingInterface = findViewById(R.id.layoutRatingInterface);
        layoutAppAppearance = findViewById(R.id.layoutAppAppearance);
        layoutPrivacySecurity = findViewById(R.id.layoutPrivacySecurity);
        layoutHelpAndSupport = findViewById(R.id.layoutHelpAndSupport); // FIXED: Updated ID
        layoutAboutBusMate = findViewById(R.id.layoutAboutBusMate);

        // Logout button
        cardViewLogout = findViewById(R.id.cardViewLogout);
    }

    /**
     * Load user profile data from database and preferences
     */
    private void loadUserProfileData() {
        long userId = sharedPreferences.getLong(KEY_USER_ID, -1);
        String userEmail = sharedPreferences.getString(KEY_EMAIL, "");

        if (userId != -1) {
            Cursor profileCursor = databaseHelper.getPassengerProfile(userId);

            if (profileCursor != null && profileCursor.moveToFirst()) {
                int nameIndex = profileCursor.getColumnIndex(DatabaseHelper.COL_FULL_NAME);

                if (nameIndex >= 0) {
                    String userName = profileCursor.getString(nameIndex);
                    textViewUserName.setText(userName != null ? userName : "BusMate User");
                } else {
                    textViewUserName.setText("BusMate User");
                }

                profileCursor.close();
            } else {
                textViewUserName.setText("BusMate User");
            }
        } else {
            textViewUserName.setText("BusMate User");
        }

        // Set email
        textViewUserEmail.setText(userEmail.isEmpty() ? "user@busmate.com" : userEmail);

        // Set membership status (could be calculated based on usage)
        textViewMembershipStatus.setText("Premium Member");
        textViewPointsBalance.setText("1,250 Points");
    }

    /**
     * Set up click listeners for all menu items
     */
    private void setupClickListeners() {
        // Account Management Section
        layoutUserProfile.setOnClickListener(v -> navigateToProfile());
        layoutPasswordRecovery.setOnClickListener(v -> navigateToPasswordRecovery());
        layoutMembershipStatus.setOnClickListener(v -> showMembershipInfo());

        // Bus Tracking Section
        layoutLiveBusTracking.setOnClickListener(v -> navigateToLiveBusTracking());
        layoutBusRouteInfo.setOnClickListener(v -> navigateToBusRouteInfo());

        // Ticketing Section
        layoutTicketPurchase.setOnClickListener(v -> navigateToTicketPurchase());
        layoutPaymentGateway.setOnClickListener(v -> navigateToPaymentGateway());

        // Trip Management Section
        layoutTripHistory.setOnClickListener(v -> navigateToTripHistory());

        // Maps & Navigation Section
        layoutInteractiveMap.setOnClickListener(v -> navigateToInteractiveMap());

        // Safety & Emergency Section
        layoutEmergencyPanel.setOnClickListener(v -> navigateToEmergencyPanel());

        // Notifications Section
        layoutNotificationCenter.setOnClickListener(v -> navigateToNotificationCenter());

        // Reviews Section
        layoutRatingInterface.setOnClickListener(v -> navigateToRatingInterface());

        // App Settings Section
        layoutAppAppearance.setOnClickListener(v -> navigateToAppSettings());
        layoutPrivacySecurity.setOnClickListener(v -> navigateToPrivacySettings());

        // Help & Support Section
        layoutHelpAndSupport.setOnClickListener(v -> navigateToHelpSupport()); // FIXED: Updated variable name
        layoutAboutBusMate.setOnClickListener(v -> showAboutBusMate());

        // Logout button click listener
        cardViewLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    /**
     * FIXED: Navigation methods with proper implementations
     */
    private void navigateToProfile() {
        // FIXED: Create ProfileActivity or use placeholder
        Toast.makeText(this, "Profile Management - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement ProfileActivity
        // Intent intent = new Intent(this, ProfileActivity.class);
        // startActivity(intent);
    }

    private void navigateToPasswordRecovery() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void navigateToLiveBusTracking() {
        Toast.makeText(this, "Live Bus Tracking - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to live bus tracking
    }

    private void navigateToBusRouteInfo() {
        Toast.makeText(this, "Bus Route Information - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to bus route info
    }

    private void navigateToTicketPurchase() {
        Toast.makeText(this, "Ticket Purchase - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to ticket purchase
    }

    private void navigateToPaymentGateway() {
        Toast.makeText(this, "Payment Gateway - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to payment gateway
    }

    private void navigateToTripHistory() {
        Toast.makeText(this, "Trip History - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to trip history
    }

    private void navigateToInteractiveMap() {
        Toast.makeText(this, "Interactive Map - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to interactive map
    }

    private void navigateToEmergencyPanel() {
        Toast.makeText(this, "Emergency Panel - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to emergency panel
    }

    private void navigateToNotificationCenter() {
        Toast.makeText(this, "Notification Center - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to notification center
    }

    private void navigateToRatingInterface() {
        Toast.makeText(this, "Rating Interface - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to rating interface
    }

    private void navigateToAppSettings() {
        Toast.makeText(this, "App Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to app settings
    }

    private void navigateToPrivacySettings() {
        Toast.makeText(this, "Privacy & Security - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to privacy settings
    }

    private void navigateToHelpSupport() {
        Toast.makeText(this, "Help & Support - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement navigation to help support
    }

    /**
     * Show membership information dialog
     */
    private void showMembershipInfo() {
        new AlertDialog.Builder(this)
                .setTitle("Membership Status")
                .setMessage("Premium Member\n\nBenefits:\n• Priority booking\n• Exclusive routes\n• 20% discount on tickets\n• Premium customer support")
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Show about BusMate information
     */
    private void showAboutBusMate() {
        new AlertDialog.Builder(this)
                .setTitle("About BusMate")
                .setMessage("BusMate - Smart Bus Travel Assistant\n\nVersion 2.1.0\n\nTransforming Public Transportation Through Innovation\n\nDeveloped for EEI4369 Course Project")
                .setPositiveButton("Terms & Privacy", (dialog, which) -> {
                    Toast.makeText(this, "Terms & Privacy - Coming Soon", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout from BusMate?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Perform logout functionality
     */
    private void performLogout() {
        // Show loading message
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

        // Clear all user session data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate to MainActivity (Get Started screen)
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish current activity
        finish();

        // Show logout success message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    @Override
    public void onBackPressed() {
        // Navigate back to dashboard instead of closing app
        super.onBackPressed();
        Intent intent = new Intent(this, PassengerDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
