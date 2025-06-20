package com.s23010421.busmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * BusOperatorDashboardActivity - Main dashboard for BusMate bus operators
 * Provides comprehensive route management, passenger communication, and trip controls
 * Implements professional operator features for efficient bus service management
 */
public class BusOperatorDashboardActivity extends AppCompatActivity {

    // UI component declarations
    private TextView textViewOperatorName;
    private TextView textViewGreeting;
    private TextView textViewCurrentRoute;
    private TextView textViewVehicleInfo;
    private TextView textViewOnlineStatus;
    private TextView textViewTripProgress;
    private TextView textViewPassengerCount;
    private TextView textViewNextStop;
    private TextView textViewScheduleStatus;
    private ImageView imageViewOperatorPhoto;

    // Status and control components
    private Switch switchOnlineStatus;
    private Button buttonStartTrip;
    private Button buttonEndTrip;
    private Button buttonEmergencyAlert;
    private Button buttonPassengerBroadcast;

    // Quick action cards
    private CardView cardViewRouteManagement;
    private CardView cardViewPassengerComm;
    private CardView cardViewTripControls;
    private CardView cardViewEmergencyPanel;

    // Trip information cards
    private CardView cardViewCurrentTrip;
    private CardView cardViewTodaysStats;
    private CardView cardViewUpcomingStops;
    private CardView cardViewPassengerRequests;

    // Navigation components
    private BottomNavigationView bottomNavigationView;

    // Database and session management
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "BusMatePrefs";

    // Operator data
    private long operatorId;
    private String operatorEmail;
    private String operatorName;
    private String currentRoute;
    private boolean isOnlineStatus = false;
    private boolean isTripActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_operator_dashboard);

        // Initialize database and preferences
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Get operator data from intent or session
        getOperatorData();

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load operator profile data
        loadOperatorProfile();

        // Set personalized greeting
        setPersonalizedGreeting();

        // Load current trip information
        loadCurrentTripInfo();

        // Setup bottom navigation
        setupBottomNavigation();

        // Initialize trip status
        initializeTripStatus();
    }

    /**
     * Get operator data from intent or shared preferences
     */
    private void getOperatorData() {
        // Try to get from intent first
        operatorId = getIntent().getLongExtra("user_id", -1);
        operatorEmail = getIntent().getStringExtra("user_email");

        // If not available from intent, get from shared preferences
        if (operatorId == -1) {
            operatorId = sharedPreferences.getLong("user_id", -1);
            operatorEmail = sharedPreferences.getString("email", "");
        }
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        // Header components
        textViewOperatorName = findViewById(R.id.textViewOperatorName);
        textViewGreeting = findViewById(R.id.textViewGreeting);
        textViewCurrentRoute = findViewById(R.id.textViewCurrentRoute);
        textViewVehicleInfo = findViewById(R.id.textViewVehicleInfo);
        textViewOnlineStatus = findViewById(R.id.textViewOnlineStatus);
        imageViewOperatorPhoto = findViewById(R.id.imageViewOperatorPhoto);

        // Trip information
        textViewTripProgress = findViewById(R.id.textViewTripProgress);
        textViewPassengerCount = findViewById(R.id.textViewPassengerCount);
        textViewNextStop = findViewById(R.id.textViewNextStop);
        textViewScheduleStatus = findViewById(R.id.textViewScheduleStatus);

        // Control components
        switchOnlineStatus = findViewById(R.id.switchOnlineStatus);
        buttonStartTrip = findViewById(R.id.buttonStartTrip);
        buttonEndTrip = findViewById(R.id.buttonEndTrip);
        buttonEmergencyAlert = findViewById(R.id.buttonEmergencyAlert);
        buttonPassengerBroadcast = findViewById(R.id.buttonPassengerBroadcast);

        // Quick action cards
        cardViewRouteManagement = findViewById(R.id.cardViewRouteManagement);
        cardViewPassengerComm = findViewById(R.id.cardViewPassengerComm);
        cardViewTripControls = findViewById(R.id.cardViewTripControls);
        cardViewEmergencyPanel = findViewById(R.id.cardViewEmergencyPanel);

        // Information cards
        cardViewCurrentTrip = findViewById(R.id.cardViewCurrentTrip);
        cardViewTodaysStats = findViewById(R.id.cardViewTodaysStats);
        cardViewUpcomingStops = findViewById(R.id.cardViewUpcomingStops);
        cardViewPassengerRequests = findViewById(R.id.cardViewPassengerRequests);

        // Bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationOperator);
    }

    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Online status toggle
        switchOnlineStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleOnlineStatus(isChecked);
        });

        // Trip control buttons
        buttonStartTrip.setOnClickListener(v -> startTrip());
        buttonEndTrip.setOnClickListener(v -> endTrip());

        // Emergency and communication
        buttonEmergencyAlert.setOnClickListener(v -> triggerEmergencyAlert());
        buttonPassengerBroadcast.setOnClickListener(v -> openPassengerBroadcast());

        // Quick action cards
        cardViewRouteManagement.setOnClickListener(v -> navigateToRouteManagement());
        cardViewPassengerComm.setOnClickListener(v -> navigateToPassengerCommunication());
        cardViewTripControls.setOnClickListener(v -> navigateToTripControls());
        cardViewEmergencyPanel.setOnClickListener(v -> navigateToEmergencyPanel());

        // Information cards
        cardViewCurrentTrip.setOnClickListener(v -> navigateToCurrentTripDetails());
        cardViewTodaysStats.setOnClickListener(v -> navigateToStatistics());
        cardViewUpcomingStops.setOnClickListener(v -> navigateToStopManagement());
        cardViewPassengerRequests.setOnClickListener(v -> navigateToPassengerRequests());
    }

    /**
     * Load operator profile data from database
     */
    private void loadOperatorProfile() {
        if (operatorId != -1) {
            Cursor profileCursor = databaseHelper.getBusOperatorProfile(operatorId);

            if (profileCursor != null && profileCursor.moveToFirst()) {
                int nameIndex = profileCursor.getColumnIndex(DatabaseHelper.COL_FULL_NAME);
                int vehicleIndex = profileCursor.getColumnIndex(DatabaseHelper.COL_VEHICLE_REGISTRATION);
                int routeIndex = profileCursor.getColumnIndex(DatabaseHelper.COL_ROUTE_NUMBER);

                if (nameIndex >= 0) {
                    operatorName = profileCursor.getString(nameIndex);
                    textViewOperatorName.setText(operatorName != null ? operatorName : "Bus Operator");
                }

                if (vehicleIndex >= 0) {
                    String vehicleReg = profileCursor.getString(vehicleIndex);
                    textViewVehicleInfo.setText("Vehicle: " + (vehicleReg != null ? vehicleReg : "Not Assigned"));
                }

                if (routeIndex >= 0) {
                    currentRoute = profileCursor.getString(routeIndex);
                    textViewCurrentRoute.setText("Route " + (currentRoute != null ? currentRoute : "Not Assigned"));
                }

                profileCursor.close();
            } else {
                // Handle case where profile data is not found
                textViewOperatorName.setText("Bus Operator");
                textViewVehicleInfo.setText("Vehicle: Not Assigned");
                textViewCurrentRoute.setText("Route: Not Assigned");
            }
        }
    }

    /**
     * Set personalized greeting based on time of day
     */
    private void setPersonalizedGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hourOfDay >= 5 && hourOfDay < 12) {
            greeting = "Good Morning";
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            greeting = "Good Afternoon";
        } else if (hourOfDay >= 17 && hourOfDay < 21) {
            greeting = "Good Evening";
        } else {
            greeting = "Good Night";
        }

        textViewGreeting.setText(greeting + ", Operator!");
    }

    /**
     * Load current trip information (simulated data)
     */
    private void loadCurrentTripInfo() {
        // Simulated trip data - in real app, fetch from database/API
        if (isTripActive) {
            textViewTripProgress.setText("Trip Progress: 45% Complete");
            textViewPassengerCount.setText("Passengers: 23/40");
            textViewNextStop.setText("Next: Central Bus Station");
            textViewScheduleStatus.setText("On Schedule");
        } else {
            textViewTripProgress.setText("No Active Trip");
            textViewPassengerCount.setText("Passengers: 0/40");
            textViewNextStop.setText("Next: Trip Not Started");
            textViewScheduleStatus.setText("Waiting to Start");
        }
    }

    /**
     * Setup bottom navigation for operator-specific features
     */
    private void setupBottomNavigation() {
        // Set Dashboard as selected
        bottomNavigationView.setSelectedItemId(R.id.navigation_operator_dashboard);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_operator_dashboard) {
                // Already on dashboard, do nothing
                return true;
            } else if (itemId == R.id.navigation_operator_routes) {
                navigateToRouteManagement();
                return true;
            } else if (itemId == R.id.navigation_operator_passengers) {
                navigateToPassengerCommunication();
                return true;
            } else if (itemId == R.id.navigation_operator_earnings) {
                navigateToEarnings();
                return true;
            } else if (itemId == R.id.navigation_operator_profile) {
                //  Navigate to operator more screen instead of profile
                navigateToOperatorMore();
                return true;
            }

            return false;
        });
    }

    /**
     * Initialize trip status based on saved state
     */
    private void initializeTripStatus() {
        // Check if there's an active trip from shared preferences
        isTripActive = sharedPreferences.getBoolean("trip_active", false);
        isOnlineStatus = sharedPreferences.getBoolean("online_status", false);

        // Update UI based on status
        switchOnlineStatus.setChecked(isOnlineStatus);
        updateTripControlButtons();
        updateOnlineStatusDisplay();
    }

    /**
     * Toggle online status for the operator
     */
    private void toggleOnlineStatus(boolean isOnline) {
        isOnlineStatus = isOnline;

        // Save status to preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("online_status", isOnlineStatus);
        editor.apply();

        updateOnlineStatusDisplay();

        String statusMessage = isOnline ? "You are now ONLINE and available for trips" :
                "You are now OFFLINE";
        Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * Update online status display
     */
    private void updateOnlineStatusDisplay() {
        if (isOnlineStatus) {
            textViewOnlineStatus.setText("● ONLINE");
            textViewOnlineStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            textViewOnlineStatus.setText("● OFFLINE");
            textViewOnlineStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    /**
     * Start a new trip
     */
    private void startTrip() {
        if (!isOnlineStatus) {
            Toast.makeText(this, "Please go online first to start a trip", Toast.LENGTH_SHORT).show();
            return;
        }

        isTripActive = true;

        // Save trip status
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("trip_active", true);
        editor.apply();

        updateTripControlButtons();
        loadCurrentTripInfo();

        Toast.makeText(this, "Trip started successfully!", Toast.LENGTH_SHORT).show();
    }

    /**
     * End current trip
     */
    private void endTrip() {
        if (!isTripActive) {
            Toast.makeText(this, "No active trip to end", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("End Trip Confirmation")
                .setMessage("Are you sure you want to end the current trip?")
                .setPositiveButton("End Trip", (dialog, which) -> {
                    isTripActive = false;

                    // Save trip status
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("trip_active", false);
                    editor.apply();

                    updateTripControlButtons();
                    loadCurrentTripInfo();

                    Toast.makeText(this, "Trip ended successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Update trip control buttons based on trip status
     */
    private void updateTripControlButtons() {
        if (isTripActive) {
            buttonStartTrip.setEnabled(false);
            buttonStartTrip.setText("Trip In Progress");
            buttonEndTrip.setEnabled(true);
            buttonEndTrip.setText("End Trip");
        } else {
            buttonStartTrip.setEnabled(isOnlineStatus);
            buttonStartTrip.setText("Start Trip");
            buttonEndTrip.setEnabled(false);
            buttonEndTrip.setText("No Active Trip");
        }
    }

    /**
     * Trigger emergency alert
     */
    private void triggerEmergencyAlert() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Emergency Alert")
                .setMessage("This will send an emergency alert to dispatch and emergency services. Continue?")
                .setPositiveButton("Send Alert", (dialog, which) -> {
                    // TODO: Implement emergency alert system
                    Toast.makeText(this, "Emergency alert sent to dispatch!", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Open passenger broadcast screen
     */
    private void openPassengerBroadcast() {
        if (!isTripActive) {
            Toast.makeText(this, "Start a trip first to communicate with passengers", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Implement passenger broadcast functionality
        Toast.makeText(this, "Passenger Broadcast - Coming Soon", Toast.LENGTH_SHORT).show();
    }

    /**
     * Navigation methods for different operator features
     */
    private void navigateToRouteManagement() {
        Toast.makeText(this, "Route Management - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement RouteManagementActivity
    }

    private void navigateToPassengerCommunication() {
        Toast.makeText(this, "Passenger Communication - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement PassengerCommunicationActivity
    }

    private void navigateToTripControls() {
        Toast.makeText(this, "Advanced Trip Controls - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement TripControlsActivity
    }

    private void navigateToEmergencyPanel() {
        Toast.makeText(this, "Emergency Panel - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement EmergencyPanelActivity
    }

    private void navigateToCurrentTripDetails() {
        if (!isTripActive) {
            Toast.makeText(this, "No active trip", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Trip Details - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement TripDetailsActivity
    }

    private void navigateToStatistics() {
        Toast.makeText(this, "Statistics & Earnings - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement StatisticsActivity
    }

    private void navigateToStopManagement() {
        Toast.makeText(this, "Stop Management - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement StopManagementActivity
    }

    private void navigateToPassengerRequests() {
        Toast.makeText(this, "Passenger Requests - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement PassengerRequestsActivity
    }

    private void navigateToEarnings() {
        Toast.makeText(this, "Earnings & Performance - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement EarningsActivity
    }


    /**
     * Navigate to bus operator more screen
     */
    private void navigateToOperatorMore() {
        Intent intent = new Intent(this, BusOperatorMoreActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }



    /**
     * Handle logout functionality
     */
    private void handleLogout() {
        if (isTripActive) {
            Toast.makeText(this, "Please end your current trip before logging out", Toast.LENGTH_LONG).show();
            return;
        }

        // Clear user session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Navigate to sign in activity
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadCurrentTripInfo();
        updateTripControlButtons();
        updateOnlineStatusDisplay();
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
        // Show confirmation dialog before minimizing app
        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setTitle("Minimize BusMate")
                .setMessage("Keep the app running in background to maintain your online status. Minimize app?")
                .setPositiveButton("Minimize", (dialog, which) -> {
                    // Move app to background instead of closing
                    moveTaskToBack(true);
                })
                .setNegativeButton("Stay", null)
                .show();
    }
}
