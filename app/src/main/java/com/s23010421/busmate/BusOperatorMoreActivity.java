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
 * BusOperatorMoreActivity - Comprehensive settings and account management for bus operators
 * Provides access to all operator features, settings, and logout functionality
 * Follows BusMate design standards for professional bus operator interface
 */
public class BusOperatorMoreActivity extends AppCompatActivity {

    // UI component declarations
    private TextView textViewOperatorName;
    private TextView textViewOperatorEmail;
    private TextView textViewOperatorStatus;
    private TextView textViewVehicleInfo;
    private TextView textViewRouteInfo;
    private ImageView imageViewOperatorPhoto;

    // Menu sections
    private LinearLayout layoutOperatorProfile;
    private LinearLayout layoutVehicleManagement;
    private LinearLayout layoutRouteManagement;
    private LinearLayout layoutScheduleManagement;
    private LinearLayout layoutPassengerCommunication;
    private LinearLayout layoutEarningsPerformance;
    private LinearLayout layoutTripManagement;
    private LinearLayout layoutSafetyEmergency;
    private LinearLayout layoutNotificationSettings;
    private LinearLayout layoutAppSettings;
    private LinearLayout layoutHelpSupport;
    private LinearLayout layoutReportsAnalytics;

    // Logout button
    private CardView cardViewLogout;

    // Database and session management
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "BusMatePrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_TYPE = "user_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_operator_more);

        // Initialize database and preferences
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize UI components
        initializeViews();

        // Load operator profile data
        loadOperatorProfileData();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        // Header components
        textViewOperatorName = findViewById(R.id.textViewOperatorName);
        textViewOperatorEmail = findViewById(R.id.textViewOperatorEmail);
        textViewOperatorStatus = findViewById(R.id.textViewOperatorStatus);
        textViewVehicleInfo = findViewById(R.id.textViewVehicleInfo);
        textViewRouteInfo = findViewById(R.id.textViewRouteInfo);
        imageViewOperatorPhoto = findViewById(R.id.imageViewOperatorPhoto);

        // Menu sections
        layoutOperatorProfile = findViewById(R.id.layoutOperatorProfile);
        layoutVehicleManagement = findViewById(R.id.layoutVehicleManagement);
        layoutRouteManagement = findViewById(R.id.layoutRouteManagement);
        layoutScheduleManagement = findViewById(R.id.layoutScheduleManagement);
        layoutPassengerCommunication = findViewById(R.id.layoutPassengerCommunication);
        layoutEarningsPerformance = findViewById(R.id.layoutEarningsPerformance);
        layoutTripManagement = findViewById(R.id.layoutTripManagement);
        layoutSafetyEmergency = findViewById(R.id.layoutSafetyEmergency);
        layoutNotificationSettings = findViewById(R.id.layoutNotificationSettings);
        layoutAppSettings = findViewById(R.id.layoutAppSettings);
        layoutHelpSupport = findViewById(R.id.layoutHelpSupport);
        layoutReportsAnalytics = findViewById(R.id.layoutReportsAnalytics);

        // Logout button
        cardViewLogout = findViewById(R.id.cardViewLogout);
    }

    /**
     * Load operator profile data from database and preferences
     */
    private void loadOperatorProfileData() {
        long operatorId = sharedPreferences.getLong(KEY_USER_ID, -1);
        String operatorEmail = sharedPreferences.getString(KEY_EMAIL, "");

        if (operatorId != -1) {
            Cursor profileCursor = databaseHelper.getBusOperatorProfile(operatorId);

            if (profileCursor != null && profileCursor.moveToFirst()) {
                int nameIndex = profileCursor.getColumnIndex(DatabaseHelper.COL_FULL_NAME);
                int vehicleIndex = profileCursor.getColumnIndex(DatabaseHelper.COL_VEHICLE_REGISTRATION);
                int routeIndex = profileCursor.getColumnIndex(DatabaseHelper.COL_ROUTE_NUMBER);
                int licenseIndex = profileCursor.getColumnIndex(DatabaseHelper.COL_LICENSE_NUMBER);

                if (nameIndex >= 0) {
                    String operatorName = profileCursor.getString(nameIndex);
                    textViewOperatorName.setText(operatorName != null ? operatorName : "Bus Operator");
                }

                if (vehicleIndex >= 0) {
                    String vehicleReg = profileCursor.getString(vehicleIndex);
                    textViewVehicleInfo.setText("Vehicle: " + (vehicleReg != null ? vehicleReg : "Not Assigned"));
                }

                if (routeIndex >= 0) {
                    String routeNumber = profileCursor.getString(routeIndex);
                    textViewRouteInfo.setText("Route " + (routeNumber != null ? routeNumber : "Not Assigned"));
                }

                if (licenseIndex >= 0) {
                    String licenseNumber = profileCursor.getString(licenseIndex);
                    // You can use license info for verification status
                    textViewOperatorStatus.setText("Verified Operator");
                }

                profileCursor.close();
            } else {
                // Handle case where profile data is not found
                textViewOperatorName.setText("Bus Operator");
                textViewVehicleInfo.setText("Vehicle: Not Assigned");
                textViewRouteInfo.setText("Route: Not Assigned");
                textViewOperatorStatus.setText("Profile Incomplete");
            }
        }

        // Set email
        textViewOperatorEmail.setText(operatorEmail.isEmpty() ? "operator@busmate.com" : operatorEmail);
    }

    /**
     * Set up click listeners for all menu items
     */
    private void setupClickListeners() {
        // Operator Profile Management
        layoutOperatorProfile.setOnClickListener(v -> navigateToOperatorProfile());

        // Vehicle Management
        layoutVehicleManagement.setOnClickListener(v -> navigateToVehicleManagement());

        // Route Management
        layoutRouteManagement.setOnClickListener(v -> navigateToRouteManagement());

        // Schedule Management
        layoutScheduleManagement.setOnClickListener(v -> navigateToScheduleManagement());

        // Passenger Communication
        layoutPassengerCommunication.setOnClickListener(v -> navigateToPassengerCommunication());

        // Earnings & Performance
        layoutEarningsPerformance.setOnClickListener(v -> navigateToEarningsPerformance());

        // Trip Management
        layoutTripManagement.setOnClickListener(v -> navigateToTripManagement());

        // Safety & Emergency
        layoutSafetyEmergency.setOnClickListener(v -> navigateToSafetyEmergency());

        // Notification Settings
        layoutNotificationSettings.setOnClickListener(v -> navigateToNotificationSettings());

        // App Settings
        layoutAppSettings.setOnClickListener(v -> navigateToAppSettings());

        // Help & Support
        layoutHelpSupport.setOnClickListener(v -> navigateToHelpSupport());

        // Reports & Analytics
        layoutReportsAnalytics.setOnClickListener(v -> navigateToReportsAnalytics());

        // Logout button click listener
        cardViewLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    /**
     * Navigation methods for different operator sections
     */
    private void navigateToOperatorProfile() {
        Toast.makeText(this, "Operator Profile Management - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement OperatorProfileActivity
    }

    private void navigateToVehicleManagement() {
        Toast.makeText(this, "Vehicle Management - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement VehicleManagementActivity
    }

    private void navigateToRouteManagement() {
        Toast.makeText(this, "Route Management - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement RouteManagementActivity
    }

    private void navigateToScheduleManagement() {
        Toast.makeText(this, "Schedule Management - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement ScheduleManagementActivity
    }

    private void navigateToPassengerCommunication() {
        Toast.makeText(this, "Passenger Communication - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement PassengerCommunicationActivity
    }

    private void navigateToEarningsPerformance() {
        Toast.makeText(this, "Earnings & Performance - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement EarningsPerformanceActivity
    }

    private void navigateToTripManagement() {
        Toast.makeText(this, "Trip Management - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement TripManagementActivity
    }

    private void navigateToSafetyEmergency() {
        Toast.makeText(this, "Safety & Emergency - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement SafetyEmergencyActivity
    }

    private void navigateToNotificationSettings() {
        Toast.makeText(this, "Notification Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement NotificationSettingsActivity
    }

    private void navigateToAppSettings() {
        Toast.makeText(this, "App Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement AppSettingsActivity
    }

    private void navigateToHelpSupport() {
        Toast.makeText(this, "Help & Support - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement HelpSupportActivity
    }

    private void navigateToReportsAnalytics() {
        Toast.makeText(this, "Reports & Analytics - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement ReportsAnalyticsActivity
    }



    /**
     * Show logout confirmation dialog with operator-specific messaging
     */
    private void showLogoutConfirmation() {
        // Check if operator has active trip
        boolean hasActiveTrip = sharedPreferences.getBoolean("trip_active", false);

        String message = "Are you sure you want to logout from BusMate Operator?";
        if (hasActiveTrip) {
            message = "You have an active trip running. Logging out will end your current trip and set you offline. Continue?";
        }

        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage(message)
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Perform operator logout functionality with trip handling
     */
    private void performLogout() {
        // Show loading message
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

        // Clear all operator session data
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
        Toast.makeText(this, "Logged out successfully. Your status is now offline.", Toast.LENGTH_LONG).show();
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
        // Navigate back to operator dashboard instead of closing app
        Intent intent = new Intent(this, BusOperatorDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
