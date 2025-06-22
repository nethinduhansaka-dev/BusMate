package com.s23010421.busmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * EmergencyActivity for EEI4369 BusMate Project
 * Implements the exact UI design shown in the image with emergency features
 */
public class EmergencyActivity extends AppCompatActivity {

    private static final String TAG = "EmergencyActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private Button buttonBack;
    private Button buttonEmergencyAlert;
    private CardView cardViewQuickContacts;
    private Button buttonPolice;
    private Button buttonMedical;
    private Button buttonFamily;
    private Button buttonBusSupport;
    private TextView textViewCurrentLocation;
    private Switch switchShareLocation;
    private CardView cardViewReportIncident;
    private Button buttonBusIssue;
    private Button buttonSafetyConcern;
    private Button buttonRoadBlock;
    private Button buttonOther;

    // Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private boolean isLocationSharingEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "EmergencyActivity onCreate started");
            setContentView(R.layout.activity_emergency);

            initializeViews();
            initializeLocationServices();
            setupClickListeners();
            requestLocationPermission();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorAndFinish("Error loading emergency screen");
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        buttonBack = findViewById(R.id.buttonBack);
        buttonEmergencyAlert = findViewById(R.id.buttonEmergencyAlert);

        // Quick Contacts
        buttonPolice = findViewById(R.id.buttonPolice);
        buttonMedical = findViewById(R.id.buttonMedical);
        buttonFamily = findViewById(R.id.buttonFamily);
        buttonBusSupport = findViewById(R.id.buttonBusSupport);

        // Location
        textViewCurrentLocation = findViewById(R.id.textViewCurrentLocation);
        switchShareLocation = findViewById(R.id.switchShareLocation);

        // Report Incident
        buttonBusIssue = findViewById(R.id.buttonBusIssue);
        buttonSafetyConcern = findViewById(R.id.buttonSafetyConcern);
        buttonRoadBlock = findViewById(R.id.buttonRoadBlock);
        buttonOther = findViewById(R.id.buttonOther);

        // Set initial location text
        textViewCurrentLocation.setText("📍 Negombo, Western Province\nAccuracy: ±5 meters • Last updated: Now");
    }

    /**
     * Initialize location services
     */
    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        // Back button
        buttonBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Emergency Alert button
        buttonEmergencyAlert.setOnClickListener(v -> showEmergencyAlert());

        // Quick Contacts
        buttonPolice.setOnClickListener(v -> dialEmergencyNumber("119"));
        buttonMedical.setOnClickListener(v -> dialEmergencyNumber("110"));
        buttonFamily.setOnClickListener(v -> dialEmergencyNumber("+94 77 987 6543"));
        buttonBusSupport.setOnClickListener(v -> dialEmergencyNumber("+94 11 234 5678"));

        // Location sharing toggle
        switchShareLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isLocationSharingEnabled = isChecked;
                handleLocationSharing(isChecked);
            }
        });

        // Report Incident buttons
        buttonBusIssue.setOnClickListener(v -> reportIncident("Bus Issue"));
        buttonSafetyConcern.setOnClickListener(v -> reportIncident("Safety Concern"));
        buttonRoadBlock.setOnClickListener(v -> reportIncident("Road Block"));
        buttonOther.setOnClickListener(v -> reportIncident("Other"));
    }

    /**
     * Show emergency alert confirmation
     */
    private void showEmergencyAlert() {
        new AlertDialog.Builder(this)
                .setTitle("🚨 Emergency Alert")
                .setMessage("Are you sure you want to activate the emergency alert?\n\n" +
                        "This will:\n" +
                        "• Send your location to emergency contacts\n" +
                        "• Call emergency services\n" +
                        "• Notify bus company support")
                .setPositiveButton("ACTIVATE EMERGENCY", (dialog, which) -> {
                    activateEmergency();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Activate emergency procedures
     */
    private void activateEmergency() {
        try {
            // Show processing message
            Toast.makeText(this, "🚨 Emergency Alert Activated!\nNotifying emergency contacts...",
                    Toast.LENGTH_LONG).show();

            // Simulate emergency activation
            // In real implementation, this would:
            // 1. Send SMS/notification to emergency contacts
            // 2. Share current location
            // 3. Call emergency services if configured
            // 4. Log emergency event

            // For demo, show confirmation
            new AlertDialog.Builder(this)
                    .setTitle("Emergency Alert Sent")
                    .setMessage("✅ Emergency contacts have been notified\n" +
                            "✅ Your location has been shared\n" +
                            "✅ Emergency services alerted\n\n" +
                            "Help is on the way. Stay calm and follow safety procedures.")
                    .setPositiveButton("OK", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Error activating emergency: " + e.getMessage(), e);
            Toast.makeText(this, "Error activating emergency alert", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Dial emergency number
     */
    private void dialEmergencyNumber(String phoneNumber) {
        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(dialIntent);

            Toast.makeText(this, "Calling " + phoneNumber, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error dialing number: " + e.getMessage(), e);
            Toast.makeText(this, "Unable to make call", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle location sharing toggle
     */
    private void handleLocationSharing(boolean enabled) {
        if (enabled) {
            Toast.makeText(this, "Location sharing enabled for emergency contacts", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Location sharing disabled", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Report incident
     */
    private void reportIncident(String incidentType) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Report " + incidentType)
                    .setMessage("Describe the " + incidentType.toLowerCase() + " you want to report:")
                    .setView(createIncidentReportInput())
                    .setPositiveButton("Submit Report", (dialog, which) -> {
                        submitIncidentReport(incidentType);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "Error reporting incident: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening incident report", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create incident report input view
     */
    private View createIncidentReportInput() {
        android.widget.EditText editText = new android.widget.EditText(this);
        editText.setHint("Describe the incident...");
        editText.setMinLines(3);
        editText.setPadding(16, 16, 16, 16);
        return editText;
    }

    /**
     * Submit incident report
     */
    private void submitIncidentReport(String incidentType) {
        Toast.makeText(this, "✅ " + incidentType + " report submitted successfully", Toast.LENGTH_LONG).show();

        // In real implementation, this would send the report to appropriate authorities
        Log.d(TAG, "Incident report submitted: " + incidentType);
    }

    /**
     * Request location permission
     */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    /**
     * Get current location
     */
    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                currentLocation = location;
                                updateLocationDisplay();
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting location: " + e.getMessage(), e);
        }
    }

    /**
     * Update location display
     */
    private void updateLocationDisplay() {
        if (currentLocation != null && textViewCurrentLocation != null) {
            String locationText = String.format("📍 Negombo, Western Province\n" +
                            "Accuracy: ±5 meters • Last updated: Now\n" +
                            "Coordinates: %.4f, %.4f",
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            textViewCurrentLocation.setText(locationText);
        }
    }

    /**
     * Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
                Toast.makeText(this, "Location access granted for emergency services", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location access required for emergency features", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Show error and finish
     */
    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "EmergencyActivity destroyed");
    }
}
