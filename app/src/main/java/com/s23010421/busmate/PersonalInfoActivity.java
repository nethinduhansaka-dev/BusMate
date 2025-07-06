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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * PersonalInfoActivity for EEI4369 BusMate Project
 * Displays comprehensive personal information from user registration
 */
public class PersonalInfoActivity extends AppCompatActivity {

    private static final String TAG = "PersonalInfoActivity";
    private static final String PREF_NAME = "BusMatePrefs";

    // UI Components - Header Section
    private ImageView imageViewProfilePhoto;
    private TextView textViewFullName;
    private TextView textViewEmail;
    private TextView textViewMembershipStatus;
    private Button buttonBack;
    private Button buttonEditProfile;

    // Personal Information Section
    private TextView textViewPhone;
    private TextView textViewDateOfBirth;
    private TextView textViewGender;
    private TextView textViewAddress;
    private TextView textViewNationality;
    private TextView textViewIdNumber;
    private TextView textViewBloodType;

    // Contact Information Section
    private TextView textViewSecondaryContact;
    private TextView textViewPreferredDestinations;
    private TextView textViewLanguagePreference;

    // Emergency Contact Section
    private TextView textViewEmergencyContactName;
    private TextView textViewEmergencyContactPhone;
    private TextView textViewEmergencyRelationship;
    private TextView textViewMedicalInfo;

    // Travel Preferences Section
    private TextView textViewSeatPreferences;
    private TextView textViewTravelTimePreferences;
    private TextView textViewNotificationSettings;

    // Information Cards
    private CardView cardViewPersonalDetails;
    private CardView cardViewContactInfo;
    private CardView cardViewEmergencyContact;
    private CardView cardViewTravelPreferences;

    // Database and User Data
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "PersonalInfoActivity onCreate started");
            setContentView(R.layout.activity_personal_info);

            // Initialize components
            initializeViews();
            initializeServices();
            loadUserData();
            setupClickListeners();
            loadPersonalInformation();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorAndFinish("Error loading personal information");
        }
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        try {
            // Header Components
            imageViewProfilePhoto = findViewById(R.id.imageViewProfilePhoto);
            textViewFullName = findViewById(R.id.textViewFullName);
            textViewEmail = findViewById(R.id.textViewEmail);
            textViewMembershipStatus = findViewById(R.id.textViewMembershipStatus);
            buttonBack = findViewById(R.id.buttonBack);
            buttonEditProfile = findViewById(R.id.buttonEditProfile);

            // Personal Information
            textViewPhone = findViewById(R.id.textViewPhone);
            textViewDateOfBirth = findViewById(R.id.textViewDateOfBirth);
            textViewGender = findViewById(R.id.textViewGender);
            textViewAddress = findViewById(R.id.textViewAddress);
            textViewNationality = findViewById(R.id.textViewNationality);
            textViewIdNumber = findViewById(R.id.textViewIdNumber);
            textViewBloodType = findViewById(R.id.textViewBloodType);

            // Contact Information
            textViewSecondaryContact = findViewById(R.id.textViewSecondaryContact);
            textViewPreferredDestinations = findViewById(R.id.textViewPreferredDestinations);
            textViewLanguagePreference = findViewById(R.id.textViewLanguagePreference);

            // Emergency Contact
            textViewEmergencyContactName = findViewById(R.id.textViewEmergencyContactName);
            textViewEmergencyContactPhone = findViewById(R.id.textViewEmergencyContactPhone);
            textViewEmergencyRelationship = findViewById(R.id.textViewEmergencyRelationship);
            textViewMedicalInfo = findViewById(R.id.textViewMedicalInfo);

            // Travel Preferences
            textViewSeatPreferences = findViewById(R.id.textViewSeatPreferences);
            textViewTravelTimePreferences = findViewById(R.id.textViewTravelTimePreferences);
            textViewNotificationSettings = findViewById(R.id.textViewNotificationSettings);

            // Information Cards
            cardViewPersonalDetails = findViewById(R.id.cardViewPersonalDetails);
            cardViewContactInfo = findViewById(R.id.cardViewContactInfo);
            cardViewEmergencyContact = findViewById(R.id.cardViewEmergencyContact);
            cardViewTravelPreferences = findViewById(R.id.cardViewTravelPreferences);

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
            userId = getIntent().getLongExtra("user_id", -1);

            if (userId == -1) {
                userId = sharedPreferences.getLong("user_id", -1);
            }

            if (userId == -1) {
                Log.e(TAG, "No valid user ID found");
                showErrorAndFinish("User data not found");
                return;
            }

            Log.d(TAG, "Loading personal info for user ID: " + userId);

        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: " + e.getMessage(), e);
        }
    }

    /**
     * Load personal information from database
     */
    private void loadPersonalInformation() {
        try {
            Cursor passengerCursor = null;

            try {
                passengerCursor = databaseHelper.getPassengerProfile(userId);

                if (passengerCursor != null && passengerCursor.moveToFirst()) {
                    // Extract and display all personal data
                    displayPersonalData(passengerCursor);
                } else {
                    Log.w(TAG, "No personal data found");
                    setDefaultPersonalData();
                }

            } finally {
                if (passengerCursor != null) {
                    passengerCursor.close();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading personal information: " + e.getMessage(), e);
            setDefaultPersonalData();
        }
    }

    /**
     * Display personal data from database cursor
     */
    private void displayPersonalData(Cursor cursor) {
        try {
            // Header Information
            String fullName = getColumnValue(cursor, DatabaseHelper.COL_FULL_NAME, "BusMate User");
            String email = sharedPreferences.getString("email", "user@busmate.com");

            textViewFullName.setText(fullName);
            textViewEmail.setText(email);
            textViewMembershipStatus.setText("✨ Verified Premium Member");

            // Personal Details
            String phone = getColumnValue(cursor, DatabaseHelper.COL_PHONE, "Not provided");
            String dateOfBirth = getColumnValue(cursor, DatabaseHelper.COL_DATE_OF_BIRTH, "Not provided");
            String gender = getColumnValue(cursor, DatabaseHelper.COL_GENDER, "Not specified");
            String address = getColumnValue(cursor, DatabaseHelper.COL_ADDRESS, "Not provided");
            String bloodType = getColumnValue(cursor, DatabaseHelper.COL_BLOOD_TYPE, "Not specified");

            textViewPhone.setText("📱 " + phone);
            textViewDateOfBirth.setText("🎂 " + dateOfBirth);
            textViewGender.setText("👤 " + gender);
            textViewAddress.setText("🏠 " + address);
            textViewBloodType.setText("🩸 " + bloodType);

            // Demo data for fields not in current database schema
            textViewNationality.setText("🌍 Sri Lankan");
            textViewIdNumber.setText("🆔 ********1234");

            // Contact Information
            textViewSecondaryContact.setText("📞 +94 77 987 6543");
            textViewPreferredDestinations.setText("📍 Colombo, Kandy, Galle");
            textViewLanguagePreference.setText("🌐 English, Sinhala");

            // Emergency Contact
            String emergencyName = getColumnValue(cursor, DatabaseHelper.COL_EMERGENCY_CONTACT, "Not provided");
            String emergencyPhone = getColumnValue(cursor, DatabaseHelper.COL_EMERGENCY_PHONE, "Not provided");

            textViewEmergencyContactName.setText("👥 " + emergencyName);
            textViewEmergencyContactPhone.setText("🚨 " + emergencyPhone);
            textViewEmergencyRelationship.setText("💼 Family Member");
            textViewMedicalInfo.setText("🏥 No known allergies");

            // Travel Preferences
            textViewSeatPreferences.setText("💺 Window, Front Section");
            textViewTravelTimePreferences.setText("⏰ Morning (6AM-12PM)");
            textViewNotificationSettings.setText("🔔 SMS, Email, Push Notifications");

        } catch (Exception e) {
            Log.e(TAG, "Error displaying personal data: " + e.getMessage(), e);
            setDefaultPersonalData();
        }
    }

    /**
     * Helper method to safely get column value
     */
    private String getColumnValue(Cursor cursor, String columnName, String defaultValue) {
        try {
            int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex >= 0) {
                String value = cursor.getString(columnIndex);
                return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting column value for " + columnName + ": " + e.getMessage());
        }
        return defaultValue;
    }

    /**
     * Set default personal data when database data is not available
     */
    private void setDefaultPersonalData() {
        try {
            // Header
            textViewFullName.setText("BusMate User");
            textViewEmail.setText("user@busmate.com");
            textViewMembershipStatus.setText("✨ Premium Member");

            // Personal Details
            textViewPhone.setText("📱 Not provided");
            textViewDateOfBirth.setText("🎂 Not provided");
            textViewGender.setText("👤 Not specified");
            textViewAddress.setText("🏠 Not provided");
            textViewNationality.setText("🌍 Not specified");
            textViewIdNumber.setText("🆔 Not provided");
            textViewBloodType.setText("🩸 Not specified");

            // Contact Information
            textViewSecondaryContact.setText("📞 Not provided");
            textViewPreferredDestinations.setText("📍 Not set");
            textViewLanguagePreference.setText("🌐 English");

            // Emergency Contact
            textViewEmergencyContactName.setText("👥 Not provided");
            textViewEmergencyContactPhone.setText("🚨 Not provided");
            textViewEmergencyRelationship.setText("💼 Not specified");
            textViewMedicalInfo.setText("🏥 Not provided");

            // Travel Preferences
            textViewSeatPreferences.setText("💺 No preference set");
            textViewTravelTimePreferences.setText("⏰ All times");
            textViewNotificationSettings.setText("🔔 Default settings");

        } catch (Exception e) {
            Log.e(TAG, "Error setting default data: " + e.getMessage(), e);
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

            // Edit profile button
            if (buttonEditProfile != null) {
                buttonEditProfile.setOnClickListener(v -> {
                    Toast.makeText(this, "Edit Profile feature coming soon", Toast.LENGTH_SHORT).show();
                });
            }

            // Information cards click listeners (for future expansion)
            setupCardClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Setup card click listeners
     */
    private void setupCardClickListeners() {
        if (cardViewPersonalDetails != null) {
            cardViewPersonalDetails.setOnClickListener(v -> {
                Toast.makeText(this, "Personal Details", Toast.LENGTH_SHORT).show();
            });
        }

        if (cardViewContactInfo != null) {
            cardViewContactInfo.setOnClickListener(v -> {
                Toast.makeText(this, "Contact Information", Toast.LENGTH_SHORT).show();
            });
        }

        if (cardViewEmergencyContact != null) {
            cardViewEmergencyContact.setOnClickListener(v -> {
                Toast.makeText(this, "Emergency Contact", Toast.LENGTH_SHORT).show();
            });
        }

        if (cardViewTravelPreferences != null) {
            cardViewTravelPreferences.setOnClickListener(v -> {
                Toast.makeText(this, "Travel Preferences", Toast.LENGTH_SHORT).show();
            });
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
        Log.d(TAG, "PersonalInfoActivity destroyed");
    }
}
