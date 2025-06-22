package com.s23010421.busmate;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.Geocoder;
import android.location.Address;
import java.util.List;
import java.util.Locale;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FIXED: PassengerDashboardActivity for EEI4369 Project
 * EEI4369 Project: BusMate - Smart Bus Travel Assistant
 */
public class PassengerDashboardActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "PassengerDashboard";
    private static final int BUS_UPDATE_INTERVAL = 30000; // 30 seconds
    private static final double SEARCH_RADIUS_KM = 2.0; // 2km radius

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

    // FIXED: Nearby Buses Section - GET from XML instead of creating
    private LinearLayout layoutNearbyBuses;
    private TextView textViewNearbyBusesTitle;

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

    // Real-time Data Management
    private ExecutorService executorService;
    private Handler mainHandler;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private List<RealtimeBusInfo> nearbyBuses;
    private boolean isLocationUpdatesActive = false;

    // User Data
    private long passengerId;
    private String passengerName;
    private String passengerEmail;
    private Location currentLocation;

    // Geocoding
    private Geocoder geocoder;
    private String currentAddressString = "Getting location...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "Enhanced Dashboard onCreate started");
            setContentView(R.layout.activity_passenger_dashboard);

            // Initialize components with error handling
            if (!initializeViews()) {
                Log.e(TAG, "Failed to initialize views");
                showErrorAndRedirect("Error loading dashboard views");
                return;
            }

            initializeServices();
            initializeRealTimeComponents();

            // Enhanced data loading with validation
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

            // FIXED: Call existing method instead of missing one
            startBusUpdates();

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate: " + e.getMessage(), e);
            showErrorAndRedirect("Dashboard initialization failed");
        }
    }

    /**
     * Initialize real-time components
     */
    private void initializeRealTimeComponents() {
        try {
            executorService = Executors.newFixedThreadPool(3);
            mainHandler = new Handler(Looper.getMainLooper());
            nearbyBuses = new ArrayList<>();

            // Setup location request for real-time updates
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(BUS_UPDATE_INTERVAL)
                    .setFastestInterval(15000);

            // Location callback for continuous updates
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult.getLastLocation() != null) {
                        currentLocation = locationResult.getLastLocation();
                        updateLocationDisplay();
                        fetchRealTimeBusData();
                    }
                }
            };

            Log.d(TAG, "Real-time components initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing real-time components: " + e.getMessage(), e);
        }
    }

    /**
     * FIXED: Enhanced view initialization - Use XML components instead of creating new ones
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

            // FIXED: Get nearby buses section from XML
            layoutNearbyBuses = findViewById(R.id.layoutNearbyBuses);
            textViewNearbyBusesTitle = findViewById(R.id.textViewNearbyBusesTitle);

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
        geocoder = new Geocoder(this, Locale.getDefault());
    }

    /**
     * FIXED: Enhanced passenger data loading - removed COL_PROFILE_PHOTO reference
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
            }

            if (passengerId == -1) {
                Log.e(TAG, "No valid user ID found");
                return false;
            }

            // Load passenger profile from database
            Cursor passengerCursor = null;
            try {
                passengerCursor = databaseHelper.getPassengerProfile(passengerId);

                if (passengerCursor != null && passengerCursor.moveToFirst()) {
                    // Extract available passenger data
                    int nameIndex = passengerCursor.getColumnIndex(DatabaseHelper.COL_FULL_NAME);
                    int phoneIndex = passengerCursor.getColumnIndex(DatabaseHelper.COL_PHONE);

                    // Set passenger name
                    if (nameIndex >= 0) {
                        passengerName = passengerCursor.getString(nameIndex);
                        textViewPassengerName.setText(passengerName != null ? passengerName : "BusMate User");
                    } else {
                        textViewPassengerName.setText("BusMate User");
                    }

                    // FIXED: Removed profile photo loading since COL_PROFILE_PHOTO doesn't exist
                    setDefaultProfilePhoto();

                } else {
                    Log.w(TAG, "No passenger profile found for user ID: " + passengerId);
                    textViewPassengerName.setText("BusMate User");
                    setDefaultProfilePhoto();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading passenger data: " + e.getMessage(), e);
                textViewPassengerName.setText("BusMate User");
                setDefaultProfilePhoto();
            } finally {
                if (passengerCursor != null) {
                    passengerCursor.close();
                }
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Critical error loading passenger data: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Set default profile photo
     */
    private void setDefaultProfilePhoto() {
        if (imageViewPassengerPhoto != null) {
            // Use Android default gallery icon
            imageViewPassengerPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    /**
     * FIXED: Renamed method to avoid missing method error
     */
    private void startBusUpdates() {
        try {
            if (textViewNearbyBusesTitle != null) {
                textViewNearbyBusesTitle.setText("🚌 Live Nearby Buses");
            }

            // Initial fetch
            fetchRealTimeBusData();

            // Schedule periodic updates
            schedulePeriodicUpdates();

            Log.d(TAG, "Bus updates started");

        } catch (Exception e) {
            Log.e(TAG, "Error starting bus updates: " + e.getMessage(), e);
        }
    }

    /**
     * FIXED: Simplified periodic updates without missing busUpdateRunnable
     */
    private void schedulePeriodicUpdates() {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null) {
                    fetchRealTimeBusData();
                }
                // Schedule next update
                schedulePeriodicUpdates();
            }
        }, BUS_UPDATE_INTERVAL);
    }

    /**
     * Fetch real-time bus data from multiple sources
     */
    private void fetchRealTimeBusData() {
        if (currentLocation == null) {
            Log.w(TAG, "Current location not available for bus data fetch");
            return;
        }

        executorService.execute(() -> {
            try {
                List<RealtimeBusInfo> buses = new ArrayList<>();

                // Generate simulated realistic data for demo
                fetchSimulatedData(buses);

                mainHandler.post(() -> {
                    nearbyBuses.clear();
                    nearbyBuses.addAll(buses);
                    updateBusDisplay();

                    Log.d(TAG, "Found " + buses.size() + " nearby buses");
                });

            } catch (Exception e) {
                Log.e(TAG, "Error fetching real-time bus data: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    showErrorBusState();
                });
            }
        });
    }

    /**
     * Fetch simulated realistic data for demo
     */
    private void fetchSimulatedData(List<RealtimeBusInfo> buses) {
        try {
            // Generate realistic simulated bus data
            String[] routes = {"245", "187", "138", "76", "A1", "X3"};
            String[] destinations = {"Colombo Fort", "Airport", "Kandy", "Galle", "Negombo", "Kotte"};
            String[] statuses = {"On Time", "1 min delay", "2 min early", "3 min delay", "On Time"};

            int busCount = Math.min(5, routes.length);
            for (int i = 0; i < busCount; i++) {
                int arrivalTime = 2 + (i * 4) + (int)(Math.random() * 5); // 2-20 minutes
                int capacity = 30 + (int)(Math.random() * 60); // 30-90% capacity
                String status = statuses[(int)(Math.random() * statuses.length)];

                buses.add(new RealtimeBusInfo(
                        routes[i],
                        destinations[i % destinations.length],
                        arrivalTime,
                        capacity + "%",
                        status
                ));
            }

            Log.d(TAG, "Generated " + buses.size() + " simulated buses");

        } catch (Exception e) {
            Log.e(TAG, "Error generating simulated data: " + e.getMessage(), e);
        }
    }

    /**
     * Update bus display
     */
    private void updateBusDisplay() {
        try {
            // Clear existing bus views
            if (layoutNearbyBuses != null) {
                layoutNearbyBuses.removeAllViews();

                if (nearbyBuses.isEmpty()) {
                    showNoBusesFound();
                    return;
                }

                // Add each bus as a simple view
                for (RealtimeBusInfo bus : nearbyBuses) {
                    View busCard = createBusCard(bus);
                    layoutNearbyBuses.addView(busCard);
                }

                // Update bus count in notification
                updateBusArrivalNotification();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating bus display: " + e.getMessage(), e);
        }
    }

    /**
     * Create simple bus card
     */
    private View createBusCard(RealtimeBusInfo bus) {
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(16, 12, 16, 12);
        cardLayout.setBackgroundColor(getResources().getColor(android.R.color.white));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 8, 0, 8);
        cardLayout.setLayoutParams(layoutParams);

        // Bus route and destination
        TextView routeText = new TextView(this);
        routeText.setText("🚌 Route " + bus.routeNumber + " → " + bus.destination);
        routeText.setTextSize(16);
        routeText.setTextColor(getResources().getColor(android.R.color.black));
        routeText.setTypeface(null, android.graphics.Typeface.BOLD);
        cardLayout.addView(routeText);

        // Arrival time and capacity
        TextView detailsText = new TextView(this);
        String arrivalText = bus.arrivalMinutes <= 1 ? "Arriving now" : "Arrives in " + bus.arrivalMinutes + " min";
        detailsText.setText("⏰ " + arrivalText + " • 👥 " + bus.capacity + " full • " + bus.status);
        detailsText.setTextSize(14);
        detailsText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        cardLayout.addView(detailsText);

        // Click listener to track this bus
        cardLayout.setOnClickListener(v -> trackBus(bus));

        return cardLayout;
    }

    /**
     * Show no buses found message
     */
    private void showNoBusesFound() {
        TextView noBusesText = new TextView(this);
        noBusesText.setText("📍 No buses found nearby\nExpanding search radius...");
        noBusesText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        noBusesText.setPadding(16, 24, 16, 24);
        noBusesText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        if (layoutNearbyBuses != null) {
            layoutNearbyBuses.addView(noBusesText);
        }
    }

    /**
     * Show error state for bus data
     */
    private void showErrorBusState() {
        TextView errorText = new TextView(this);
        errorText.setText("⚠️ Error loading bus data\nTap to try again");
        errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        errorText.setPadding(16, 24, 16, 24);
        errorText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        errorText.setOnClickListener(v -> fetchRealTimeBusData());
        if (layoutNearbyBuses != null) {
            layoutNearbyBuses.addView(errorText);
        }
    }

    /**
     * Update bus arrival notification with nearest bus
     */
    private void updateBusArrivalNotification() {
        if (textViewBusArrivalNotification == null || nearbyBuses.isEmpty()) return;

        // Find the nearest bus
        RealtimeBusInfo nearestBus = nearbyBuses.get(0);
        for (RealtimeBusInfo bus : nearbyBuses) {
            if (bus.arrivalMinutes < nearestBus.arrivalMinutes) {
                nearestBus = bus;
            }
        }

        String notification = "🚌 Route " + nearestBus.routeNumber + " arriving in " +
                nearestBus.arrivalMinutes + " minutes";
        textViewBusArrivalNotification.setText(notification);

        // Set urgency color
        if (nearestBus.arrivalMinutes <= 3) {
            textViewBusArrivalNotification.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (nearestBus.arrivalMinutes <= 8) {
            textViewBusArrivalNotification.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            textViewBusArrivalNotification.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    /**
     * Track specific bus
     */
    private void trackBus(RealtimeBusInfo bus) {
        Intent intent = new Intent(this, LiveBusTrackingActivity.class);
        intent.putExtra("route_number", bus.routeNumber);
        intent.putExtra("destination", bus.destination);
        intent.putExtra("arrival_time", bus.arrivalMinutes + " min");
        intent.putExtra("bus_stop", "Current Location");

        if (currentLocation != null) {
            intent.putExtra("user_latitude", currentLocation.getLatitude());
            intent.putExtra("user_longitude", currentLocation.getLongitude());
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Error handling and redirect method
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
     * Redirect to login method
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
        } else if (hourOfDay >= 21 || hourOfDay < 5) {
            greeting = "Good Night";
        } else {
            greeting = "Welcome";
        }

        textViewGreeting.setText(greeting + (passengerName != null ? ", " + passengerName : "!"));
    }

    /**
     * Set up bottom navigation actions
     */
    private void setupBottomNavigation() {
        if (bottomNavigationView == null) return;

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Already on dashboard
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
            } else {
                return false;
            }
        });
    }

    /**
     * Request location permission if not granted
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
     * Handle permission result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required for live bus updates", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Get current location (single update)
     */
    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location;
                            updateLocationDisplay();
                            fetchRealTimeBusData();
                        } else {
                            // Request continuous updates if last location is null
                            startLocationUpdates();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error getting current location: " + e.getMessage(), e);
        }
    }

    /**
     * Start continuous location updates
     */
    private void startLocationUpdates() {
        if (isLocationUpdatesActive) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        isLocationUpdatesActive = true;
    }

    /**
     * Stop location updates
     */
    private void stopLocationUpdates() {
        if (!isLocationUpdatesActive) return;
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isLocationUpdatesActive = false;
    }

    /**
     * Update location display in UI
     */
    private void updateLocationDisplay() {
        if (textViewCurrentLocation != null && currentLocation != null) {
            String loc = String.format("📍 %.4f, %.4f",
                    currentLocation.getLatitude(), currentLocation.getLongitude());
            textViewCurrentLocation.setText(loc);
        }
    }

    /**
     * Load real-time bus updates (initial call)
     */
    private void loadRealTimeBusUpdates() {
        // Initial setup for bus updates
        if (textViewBusArrivalNotification != null) {
            textViewBusArrivalNotification.setText("Loading bus information...");
        }
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

    /**
     * FIXED: Navigate to ticket booking
     */
    private void navigateToTicketBooking() {
        try {
            Intent intent = new Intent(this, TicketBookingActivity.class);
            intent.putExtra("passenger_id", passengerId);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to ticket booking: " + e.getMessage(), e);
            Toast.makeText(this, "Ticket booking temporarily unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * FIXED: Navigate to trip history
     */
    private void navigateToTripHistory() {
        try {
            Intent intent = new Intent(this, TripHistoryActivity.class);
            intent.putExtra("passenger_id", passengerId);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to trip history: " + e.getMessage(), e);
            Toast.makeText(this, "Trip history temporarily unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * FIXED: Navigate to emergency
     */
    private void navigateToEmergency() {
        try {
            Intent intent = new Intent(this, EmergencyActivity.class);
            intent.putExtra("passenger_id", passengerId);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to emergency: " + e.getMessage(), e);
            Toast.makeText(this, "Emergency features temporarily unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void navigateToMore() {
        Intent intent = new Intent(this, MoreActivity.class);
        startActivity(intent);
    }

    /**
     * Data class for real-time bus info
     */
    private static class RealtimeBusInfo {
        String routeNumber;
        String destination;
        int arrivalMinutes;
        String capacity;
        String status;

        RealtimeBusInfo(String routeNumber, String destination, int arrivalMinutes, String capacity, String status) {
            this.routeNumber = routeNumber;
            this.destination = destination;
            this.arrivalMinutes = arrivalMinutes;
            this.capacity = capacity;
            this.status = status;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLocationUpdatesActive) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        Log.d(TAG, "PassengerDashboardActivity destroyed");
    }
}
