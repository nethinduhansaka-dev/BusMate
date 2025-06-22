package com.s23010421.busmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * ProfileActivity for EEI4369 BusMate Project
 * Displays and manages passenger profile information
 */
public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final String PREF_NAME = "BusMatePrefs";

    // UI Components
    private ImageView imageViewProfilePhoto;
    private TextView textViewProfileName;
    private TextView textViewProfileEmail;
    private TextView textViewProfilePhone;
    private TextView textViewProfileAddress;
    private TextView textViewMembershipStatus;
    private TextView textViewTotalTrips;
    private TextView textViewTotalSpent;

    // Profile Cards
    private CardView cardViewPersonalInfo;
    private CardView cardViewTravelStats;
    private CardView cardViewPaymentMethods;
    private CardView cardViewPreferences;
    private CardView cardViewSecurity;

    // Action Buttons
    private Button buttonEditProfile;
    private Button buttonLogout;
    private Button buttonBack;

    // Database and User Data
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private long passengerId;
    private String passengerName;
    private String passengerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "ProfileActivity onCreate started");
            setContentView(R.layout.activity_profile);

            // Initialize components
            initializeViews();
            initializeServices();
            loadUserData();
            setupClickListeners();
            loadProfileData();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorAndFinish("Error loading profile");
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        try {
            // Profile header
            imageViewProfilePhoto = findViewById(R.id.imageViewProfilePhoto);
            textViewProfileName = findViewById(R.id.textViewProfileName);
            textViewProfileEmail = findViewById(R.id.textViewProfileEmail);
            textViewProfilePhone = findViewById(R.id.textViewProfilePhone);
            textViewProfileAddress = findViewById(R.id.textViewProfileAddress);
            textViewMembershipStatus = findViewById(R.id.textViewMembershipStatus);

            // Travel statistics
            textViewTotalTrips = findViewById(R.id.textViewTotalTrips);
            textViewTotalSpent = findViewById(R.id.textViewTotalSpent);

            // Profile cards
            cardViewPersonalInfo = findViewById(R.id.cardViewPersonalInfo);
            cardViewTravelStats = findViewById(R.id.cardViewTravelStats);
            cardViewPaymentMethods = findViewById(R.id.cardViewPaymentMethods);
            cardViewPreferences = findViewById(R.id.cardViewPreferences);
            cardViewSecurity = findViewById(R.id.cardViewSecurity);

            // Action buttons
            buttonEditProfile = findViewById(R.id.buttonEditProfile);
            buttonLogout = findViewById(R.id.buttonLogout);
            buttonBack = findViewById(R.id.buttonBack);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize services
     */
    private void initializeServices() {
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    /**
     * Load user data from intent and SharedPreferences
     */
    private void loadUserData() {
        try {
            // Get user data from intent or SharedPreferences
            passengerId = getIntent().getLongExtra("passenger_id", -1);

            if (passengerId == -1) {
                passengerId = sharedPreferences.getLong("user_id", -1);
            }

            if (passengerId == -1) {
                Log.e(TAG, "No valid passenger ID found");
                showErrorAndFinish("User data not found");
                return;
            }

            Log.d(TAG, "Loading profile for passenger ID: " + passengerId);

        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: " + e.getMessage(), e);
        }
    }

    /**
     * Load profile data from database
     */
    private void loadProfileData() {
        try {
            Cursor passengerCursor = null;

            try {
                passengerCursor = databaseHelper.getPassengerProfile(passengerId);

                if (passengerCursor != null && passengerCursor.moveToFirst()) {
                    // Extract profile data
                    int nameIndex = passengerCursor.getColumnIndex(DatabaseHelper.COL_FULL_NAME);
                    int phoneIndex = passengerCursor.getColumnIndex(DatabaseHelper.COL_PHONE);
                    int addressIndex = passengerCursor.getColumnIndex(DatabaseHelper.COL_ADDRESS);

                    if (nameIndex >= 0) {
                        passengerName = passengerCursor.getString(nameIndex);
                    }

                    String phone = phoneIndex >= 0 ? passengerCursor.getString(phoneIndex) : "Not provided";
                    String address = addressIndex >= 0 ? passengerCursor.getString(addressIndex) : "Not provided";

                    // Get email from SharedPreferences or intent
                    passengerEmail = getIntent().getStringExtra("user_email");
                    if (passengerEmail == null) {
                        passengerEmail = sharedPreferences.getString("email", "user@busmate.com");
                    }

                    // Update UI with profile data
                    updateProfileUI(passengerName, passengerEmail, phone, address);

                } else {
                    Log.w(TAG, "No profile data found");
                    setDefaultProfileData();
                }

            } finally {
                if (passengerCursor != null) {
                    passengerCursor.close();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading profile data: " + e.getMessage(), e);
            setDefaultProfileData();
        }
    }

    /**
     * Update UI with profile data
     */
    private void updateProfileUI(String name, String email, String phone, String address) {
        try {
            if (textViewProfileName != null) {
                textViewProfileName.setText(name != null ? name : "BusMate User");
            }

            if (textViewProfileEmail != null) {
                textViewProfileEmail.setText(email);
            }

            if (textViewProfilePhone != null) {
                textViewProfilePhone.setText("📱 " + phone);
            }

            if (textViewProfileAddress != null) {
                textViewProfileAddress.setText("📍 " + address);
            }

            if (textViewMembershipStatus != null) {
                textViewMembershipStatus.setText("✨ Premium Member");
            }

            // Set travel statistics (simulated data for demo)
            if (textViewTotalTrips != null) {
                textViewTotalTrips.setText("47");
            }

            if (textViewTotalSpent != null) {
                textViewTotalSpent.setText("LKR 2,450");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating profile UI: " + e.getMessage(), e);
        }
    }

    /**
     * Set default profile data
     */
    private void setDefaultProfileData() {
        updateProfileUI("BusMate User", "user@busmate.com", "Not provided", "Not provided");
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

            // Edit profile button
            if (buttonEditProfile != null) {
                buttonEditProfile.setOnClickListener(v -> {
                    // Navigate to edit profile (placeholder)
                    Toast.makeText(this, "Edit Profile feature coming soon", Toast.LENGTH_SHORT).show();
                });
            }

            // Logout button
            if (buttonLogout != null) {
                buttonLogout.setOnClickListener(v -> showLogoutConfirmation());
            }

            // Profile cards click listeners
            if (cardViewPersonalInfo != null) {
                cardViewPersonalInfo.setOnClickListener(v -> {
                    Toast.makeText(this, "Personal Information", Toast.LENGTH_SHORT).show();
                });
            }

            if (cardViewTravelStats != null) {
                cardViewTravelStats.setOnClickListener(v -> {
                    showTravelStatsDialog();
                });
            }

            if (cardViewPaymentMethods != null) {
                cardViewPaymentMethods.setOnClickListener(v -> {
                    Toast.makeText(this, "Payment Methods", Toast.LENGTH_SHORT).show();
                });
            }

            if (cardViewPreferences != null) {
                cardViewPreferences.setOnClickListener(v -> {
                    Toast.makeText(this, "Preferences", Toast.LENGTH_SHORT).show();
                });
            }

            if (cardViewSecurity != null) {
                cardViewSecurity.setOnClickListener(v -> {
                    Toast.makeText(this, "Security Settings", Toast.LENGTH_SHORT).show();
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Show travel statistics dialog
     */
    private void showTravelStatsDialog() {
        try {
            String stats = "📊 Travel Statistics\n\n" +
                    "🚌 Total Trips: 47\n" +
                    "💰 Total Spent: LKR 2,450\n" +
                    "🛣️ Distance Traveled: 1,250 km\n" +
                    "⭐ Average Rating: 4.8/5\n" +
                    "🌱 CO2 Saved: 85 kg";

            new AlertDialog.Builder(this)
                    .setTitle("Travel Statistics")
                    .setMessage(stats)
                    .setPositiveButton("Close", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing travel stats: " + e.getMessage(), e);
        }
    }

    /**
     * Show logout confirmation
     */
    private void showLogoutConfirmation() {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout from BusMate?")
                    .setPositiveButton("Logout", (dialog, which) -> performLogout())
                    .setNegativeButton("Cancel", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing logout confirmation: " + e.getMessage(), e);
        }
    }

    /**
     * Perform logout
     */
    private void performLogout() {
        try {
            // Clear SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Navigate to login
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage(), e);
            Toast.makeText(this, "Error during logout", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show error and finish activity
     */
    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        Log.d(TAG, "ProfileActivity destroyed");
    }
}
