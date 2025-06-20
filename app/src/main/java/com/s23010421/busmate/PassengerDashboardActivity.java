package com.s23010421.busmate;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

/**
 * SIMPLIFIED: PassengerDashboardActivity without NearbyBusAdapter
 * EEI4369 Project: BusMate - Smart Bus Travel Assistant
 */
public class PassengerDashboardActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "PassengerDashboard";

    // UI Components - Header Section
    private TextView textViewPassengerName;
    private TextView textViewGreeting;
    private TextView textViewCurrentLocation;
    private TextView textViewCurrentWeather;
    private ImageView imageViewPassengerPhoto;

    // Quick Actions Section
    private CardView cardViewFindBus;
    private CardView cardViewBuyTicket;
    private CardView cardViewEmergency;
    private CardView cardViewTripHistory;

    // SIMPLIFIED: Nearby Buses Section (without RecyclerView)
    private LinearLayout layoutNearbyBuses;
    private TextView textViewNearbyBusesTitle;
    private TextView textViewNearbyBus1;
    private TextView textViewNearbyBus2;
    private TextView textViewNearbyBus3;

    // Real-Time Notifications Section
    private CardView cardViewLiveBusUpdates;
    private TextView textViewBusArrivalNotification;
    private Button buttonTrackBus;

    // Bottom Navigation
    private BottomNavigationView bottomNavigationView;

    // Database and Location Services
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "BusMatePrefs";

    // User Data
    private long passengerId;
    private String passengerName;
    private String passengerEmail;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "Dashboard onCreate started");
            setContentView(R.layout.activity_passenger_dashboard);

            // Initialize components with error handling
            if (!initializeViews()) {
                Log.e(TAG, "Failed to initialize views");
                showErrorAndRedirect("Error loading dashboard views");
                return;
            }

            initializeServices();

            // FIXED: Enhanced data loading with validation
            if (!loadPassengerData()) {
                Log.e(TAG, "Failed to load passenger data");
                showErrorAndRedirect("Unable to load user data");
                return;
            }

            setupClickListeners();
            setupBottomNavigation();
            requestLocationPermission();
            setPersonalizedGreeting();
            loadRealTimeBusUpdates();

            // SIMPLIFIED: Load nearby buses without adapter
            loadNearbyBusesSimple();

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate: " + e.getMessage(), e);
            showErrorAndRedirect("Dashboard initialization failed");
        }
    }

    /**
     * SIMPLIFIED: Initialize views without RecyclerView
     */
    private boolean initializeViews() {
        try {
            // Header components
            textViewPassengerName = findViewById(R.id.textViewPassengerName);
            textViewGreeting = findViewById(R.id.textViewGreeting);
            textViewCurrentLocation = findViewById(R.id.textViewCurrentLocation);
            textViewCurrentWeather = findViewById(R.id.textViewCurrentWeather);
            imageViewPassengerPhoto = findViewById(R.id.imageViewPassengerPhoto);

            // Validate critical views exist
            if (textViewPassengerName == null) {
                Log.e(TAG, "textViewPassengerName not found in layout");
                return false;
            }

            // Quick actions
            cardViewFindBus = findViewById(R.id.cardViewFindBus);
            cardViewBuyTicket = findViewById(R.id.cardViewBuyTicket);
            cardViewEmergency = findViewById(R.id.cardViewEmergency);
            cardViewTripHistory = findViewById(R.id.cardViewTripHistory);

            // SIMPLIFIED: Nearby buses without RecyclerView
            layoutNearbyBuses = findViewById(R.id.layoutNearbyBuses);
            textViewNearbyBusesTitle = findViewById(R.id.textViewNearbyBusesTitle);

            // Simple TextViews for nearby buses (optional - create these in layout)
            textViewNearbyBus1 = findViewById(R.id.textViewNearbyBus1);
            textViewNearbyBus2 = findViewById(R.id.textViewNearbyBus2);
            textViewNearbyBus3 = findViewById(R.id.textViewNearbyBus3);

            // Real-time notifications
            cardViewLiveBusUpdates = findViewById(R.id.cardViewLiveBusUpdates);
            textViewBusArrivalNotification = findViewById(R.id.textViewBusArrivalNotification);
            buttonTrackBus = findViewById(R.id.buttonTrackBus);

            // Bottom navigation
            bottomNavigationView = findViewById(R.id.bottomNavigationPassenger);

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Initialize database and location services
     */
    private void initializeServices() {
        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    /**
     * FIXED: Enhanced passenger data loading with comprehensive error handling
     */
    private boolean loadPassengerData() {
        try {
            // Get passenger data from intent or shared preferences
            passengerId = getIntent().getLongExtra("user_id", -1);
            passengerEmail = getIntent().getStringExtra("user_email");

            Log.d(TAG, "Loading passenger data - ID: " + passengerId + ", Email: " + passengerEmail);

            if (passengerId == -1) {
                passengerId = sharedPreferences.getLong("user_id", -1);
                passengerEmail = sharedPreferences.getString("email", "");
                Log.d(TAG, "Loaded from SharedPreferences - ID: " + passengerId + ", Email: " + passengerEmail);
            }

            // FIXED: Add validation before database query
            if (passengerId == -1) {
                Log.e(TAG, "No valid user ID found");
                return false;
            }

            // Load passenger profile from database with error handling
            Cursor passengerCursor = null;
            try {
                passengerCursor = databaseHelper.getPassengerProfile(passengerId);

                if (passengerCursor != null && passengerCursor.moveToFirst()) {
                    int nameIndex = passengerCursor.getColumnIndex(DatabaseHelper.COL_FULL_NAME);

                    if (nameIndex >= 0) {
                        passengerName = passengerCursor.getString(nameIndex);
                        textViewPassengerName.setText(passengerName != null ? passengerName : "BusMate User");
                    } else {
                        textViewPassengerName.setText("BusMate User");
                    }
                } else {
                    Log.w(TAG, "No passenger profile found for user ID: " + passengerId);
                    textViewPassengerName.setText("BusMate User");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading passenger data: " + e.getMessage(), e);
                textViewPassengerName.setText("BusMate User");
            } finally {
                if (passengerCursor != null) {
                    passengerCursor.close();
                }
            }

            // Set current location placeholder
            if (textViewCurrentLocation != null) {
                textViewCurrentLocation.setText("Getting location...");
            }
            if (textViewCurrentWeather != null) {
                textViewCurrentWeather.setText("Loading weather...");
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Critical error loading passenger data: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * SIMPLIFIED: Load nearby buses without adapter - just display text
     */
    private void loadNearbyBusesSimple() {
        try {
            // Set title
            if (textViewNearbyBusesTitle != null) {
                textViewNearbyBusesTitle.setText("Nearby Buses");
            }

            // SIMPLIFIED: Display nearby buses as simple text (if TextViews exist)
            if (textViewNearbyBus1 != null) {
                textViewNearbyBus1.setText("🚌 Route 245 - Colombo Express • Arrives in 3 min • 78% Full");
                textViewNearbyBus1.setOnClickListener(v -> navigateToLiveBusTracking());
            }

            if (textViewNearbyBus2 != null) {
                textViewNearbyBus2.setText("🚌 Route 187 - Airport Express • Arrives in 7 min • 45% Full");
                textViewNearbyBus2.setOnClickListener(v -> navigateToLiveBusTracking());
            }

            if (textViewNearbyBus3 != null) {
                textViewNearbyBus3.setText("🚌 Route 138 - Kandy Express • Arrives in 12 min • 92% Full");
                textViewNearbyBus3.setOnClickListener(v -> navigateToLiveBusTracking());
            }

            Log.d(TAG, "Simple nearby buses loaded successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error loading simple nearby buses: " + e.getMessage(), e);
        }
    }

    /**
     * FIXED: Error handling and redirect method
     */
    private void showErrorAndRedirect(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Redirect to login after a short delay
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                redirectToLogin();
            }
        }, 2000);
    }

    /**
     * FIXED: Redirect to login method
     */
    private void redirectToLogin() {
        Intent intent = new Intent(PassengerDashboardActivity.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Quick Actions - Add null checks
        if (cardViewFindBus != null) {
            cardViewFindBus.setOnClickListener(v -> navigateToLiveBusTracking());
        }
        if (cardViewBuyTicket != null) {
            cardViewBuyTicket.setOnClickListener(v -> navigateToTicketBooking());
        }
        if (cardViewEmergency != null) {
            cardViewEmergency.setOnClickListener(v -> navigateToEmergency());
        }
        if (cardViewTripHistory != null) {
            cardViewTripHistory.setOnClickListener(v -> navigateToTripHistory());
        }

        // Live Bus Updates - Add null checks
        if (cardViewLiveBusUpdates != null) {
            cardViewLiveBusUpdates.setOnClickListener(v -> navigateToLiveBusTracking());
        }
        if (buttonTrackBus != null) {
            buttonTrackBus.setOnClickListener(v -> navigateToLiveBusTracking());
        }

        // Passenger photo click for profile - Add null check
        if (imageViewPassengerPhoto != null) {
            imageViewPassengerPhoto.setOnClickListener(v -> navigateToProfile());
        }
    }

    /**
     * Set personalized greeting based on time of day
     */
    private void setPersonalizedGreeting() {
        if (textViewGreeting == null) return;

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

        textViewGreeting.setText(greeting + "!");
    }

    /**
     * Load real-time bus updates and notifications
     */
    private void loadRealTimeBusUpdates() {
        if (textViewBusArrivalNotification == null) return;

        // Simulate real-time bus arrival notification
        textViewBusArrivalNotification.setText("Bus #245 arriving in 3 minutes at Central Station");

        // Update notification color based on urgency
        if (textViewBusArrivalNotification.getText().toString().contains("3 minutes")) {
            textViewBusArrivalNotification.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    /**
     * Request location permission for real-time tracking
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
     * Get current user location for nearby bus search
     */
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            updateLocationDisplay();
                        }
                    }
                });
    }

    /**
     * Update location display with current position
     */
    private void updateLocationDisplay() {
        if (currentLocation != null && textViewCurrentLocation != null && textViewCurrentWeather != null) {
            // In a real app, you would use reverse geocoding
            textViewCurrentLocation.setText("Near Central Bus Station, Colombo");
            textViewCurrentWeather.setText("29°C • Partly Cloudy");
        }
    }

    /**
     * Setup bottom navigation for passenger features
     */
    private void setupBottomNavigation() {
        if (bottomNavigationView == null) return;

        // Set Home as selected
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                // Already on home, do nothing
                return true;
            } else if (itemId == R.id.navigation_map) {
                navigateToLiveBusTracking();
                return true;
            } else if (itemId == R.id.navigation_tickets) {
                navigateToTicketBooking();
                return true;
            } else if (itemId == R.id.navigation_trips) {
                navigateToTripHistory();
                return true;
            } else if (itemId == R.id.navigation_more) {
                navigateToMore();
                return true;
            }

            return false;
        });
    }

    // Navigation methods for BusMate features
    private void navigateToLiveBusTracking() {
        Intent intent = new Intent(this, LiveBusTrackingActivity.class);
        if (currentLocation != null) {
            intent.putExtra("user_latitude", currentLocation.getLatitude());
            intent.putExtra("user_longitude", currentLocation.getLongitude());
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToTicketBooking() {
        Intent intent = new Intent(this, TicketBookingActivity.class);
        intent.putExtra("passenger_id", passengerId);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToEmergency() {
        Intent intent = new Intent(this, EmergencyActivity.class);
        if (currentLocation != null) {
            intent.putExtra("emergency_latitude", currentLocation.getLatitude());
            intent.putExtra("emergency_longitude", currentLocation.getLongitude());
        }
        intent.putExtra("passenger_name", passengerName);
        intent.putExtra("passenger_phone", passengerEmail);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void navigateToTripHistory() {
        Intent intent = new Intent(this, TripHistoryActivity.class);
        intent.putExtra("passenger_id", passengerId);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, PassengerProfileActivity.class);
        intent.putExtra("passenger_id", passengerId);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToMore() {
        Intent intent = new Intent(this, MoreActivity.class);
        intent.putExtra("user_type", "passenger");
        intent.putExtra("user_id", passengerId);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
                Toast.makeText(this, "Location access granted. Loading nearby buses...",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission required for nearby bus tracking",
                        Toast.LENGTH_LONG).show();
                // Load default buses without location
                if (textViewCurrentLocation != null) {
                    textViewCurrentLocation.setText("Location access denied");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh real-time data when activity resumes
        loadRealTimeBusUpdates();
        loadNearbyBusesSimple();
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
        // Show exit confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Exit BusMate")
                .setMessage("Are you sure you want to exit BusMate?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    finishAffinity(); // Close all activities
                })
                .setNegativeButton("Stay", null)
                .show();
    }
}
