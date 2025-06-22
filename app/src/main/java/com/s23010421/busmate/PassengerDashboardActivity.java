package com.s23010421.busmate;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced PassengerDashboardActivity with Real-Time Bus Updates
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

    // Enhanced Nearby Buses Section with Real-Time Updates
    private LinearLayout layoutNearbyBuses;
    private TextView textViewNearbyBusesTitle;
    private LinearLayout layoutBusList;
    private ProgressBar progressBarBuses;
    private Button buttonRefreshBuses;
    private TextView textViewLastUpdate;

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

            // Start real-time bus tracking
            startRealTimeBusUpdates();

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
     * Enhanced view initialization with real-time bus components
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

            // Enhanced nearby buses section
            layoutNearbyBuses = findViewById(R.id.layoutNearbyBuses);
            textViewNearbyBusesTitle = findViewById(R.id.textViewNearbyBusesTitle);

            // Create dynamic bus list container
            layoutBusList = new LinearLayout(this);
            layoutBusList.setOrientation(LinearLayout.VERTICAL);

            // Add progress bar for loading state
            progressBarBuses = new ProgressBar(this);
            progressBarBuses.setVisibility(View.GONE);

            // Add refresh button
            buttonRefreshBuses = new Button(this);
            buttonRefreshBuses.setText("🔄 Refresh Buses");
            buttonRefreshBuses.setOnClickListener(v -> refreshBusData());

            // Add last update timestamp
            textViewLastUpdate = new TextView(this);
            textViewLastUpdate.setTextSize(12);
            textViewLastUpdate.setTextColor(getResources().getColor(android.R.color.darker_gray));

            // Add components to nearby buses layout
            if (layoutNearbyBuses != null) {
                layoutNearbyBuses.addView(progressBarBuses);
                layoutNearbyBuses.addView(layoutBusList);
                layoutNearbyBuses.addView(buttonRefreshBuses);
                layoutNearbyBuses.addView(textViewLastUpdate);
            }

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
     * Enhanced passenger data loading with comprehensive error handling
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

            // Add validation before database query
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
     * Start real-time bus updates
     */
    private void startRealTimeBusUpdates() {
        try {
            if (textViewNearbyBusesTitle != null) {
                textViewNearbyBusesTitle.setText("🚌 Live Nearby Buses");
            }

            // Show loading state
            showBusLoadingState(true);

            // Initial fetch
            fetchRealTimeBusData();

            // Schedule periodic updates
            mainHandler.postDelayed(busUpdateRunnable, BUS_UPDATE_INTERVAL);

            Log.d(TAG, "Real-time bus updates started");

        } catch (Exception e) {
            Log.e(TAG, "Error starting real-time bus updates: " + e.getMessage(), e);
        }
    }

    /**
     * Runnable for periodic bus updates
     */
    private final Runnable busUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentLocation != null) {
                fetchRealTimeBusData();
            }
            // Schedule next update
            mainHandler.postDelayed(this, BUS_UPDATE_INTERVAL);
        }
    };

    /**
     * Fetch real-time bus data from multiple sources
     */
    private void fetchRealTimeBusData() {
        if (currentLocation == null) {
            Log.w(TAG, "Current location not available for bus data fetch");
            return;
        }

        showBusLoadingState(true);

        executorService.execute(() -> {
            try {
                List<RealtimeBusInfo> buses = new ArrayList<>();

                // Fetch from multiple data sources
                fetchFromTransitAPI(buses);
                fetchFromLocalTransit(buses);
                fetchSimulatedData(buses); // Fallback with simulated realistic data

                mainHandler.post(() -> {
                    nearbyBuses.clear();
                    nearbyBuses.addAll(buses);
                    updateBusDisplay();
                    showBusLoadingState(false);
                    updateLastUpdateTime();

                    Log.d(TAG, "Found " + buses.size() + " nearby buses");
                });

            } catch (Exception e) {
                Log.e(TAG, "Error fetching real-time bus data: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    showBusLoadingState(false);
                    showErrorBusState();
                });
            }
        });
    }

    /**
     * Fetch from Transit API (example implementation)
     */
    private void fetchFromTransitAPI(List<RealtimeBusInfo> buses) {
        try {
            // Example: Singapore LTA API or other transit APIs
            if (isInSingapore()) {
                fetchSingaporeTransitData(buses);
            } else {
                // Use general transit APIs
                fetchGeneralTransitData(buses);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching from transit API: " + e.getMessage(), e);
        }
    }

    /**
     * Check if user is in Singapore
     */
    private boolean isInSingapore() {
        if (currentLocation == null) return false;
        double lat = currentLocation.getLatitude();
        double lon = currentLocation.getLongitude();
        return lat >= 1.16 && lat <= 1.48 && lon >= 103.6 && lon <= 104.1;
    }

    /**
     * Fetch Singapore transit data
     */
    private void fetchSingaporeTransitData(List<RealtimeBusInfo> buses) {
        try {
            // Simulate Singapore bus data (replace with actual LTA API)
            buses.add(new RealtimeBusInfo("175", "Bukit Batok Int", 3, "85%", "On Time"));
            buses.add(new RealtimeBusInfo("963", "Marina Bay", 7, "65%", "2 min delay"));
            buses.add(new RealtimeBusInfo("14", "Bedok Int", 12, "45%", "On Time"));
        } catch (Exception e) {
            Log.e(TAG, "Error fetching Singapore data: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch general transit data
     */
    private void fetchGeneralTransitData(List<RealtimeBusInfo> buses) {
        try {
            String url = "https://api.transitland.org/api/v2/rest/stops"
                    + "?lat=" + currentLocation.getLatitude()
                    + "&lon=" + currentLocation.getLongitude()
                    + "&radius=" + (SEARCH_RADIUS_KM * 1000);

            String response = makeHttpRequest(url);
            if (response != null) {
                parseTransitResponse(response, buses);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching general transit data: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch from local transit authority
     */
    private void fetchFromLocalTransit(List<RealtimeBusInfo> buses) {
        // Implement local transit authority API calls
        // This would be specific to your region's public transport API
    }

    /**
     * Fetch simulated realistic data as fallback
     */
    private void fetchSimulatedData(List<RealtimeBusInfo> buses) {
        try {
            // Generate realistic simulated bus data
            String[] routes = {"245", "187", "138", "76", "A1", "X3"};
            String[] destinations = {"City Center", "Airport", "University", "Shopping Mall", "Train Station", "Hospital"};
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
     * Make HTTP request
     */
    private String makeHttpRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            connection.disconnect();

            return response.toString();

        } catch (Exception e) {
            Log.e(TAG, "HTTP request error: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parse transit API response
     */
    private void parseTransitResponse(String jsonResponse, List<RealtimeBusInfo> buses) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray stops = json.optJSONArray("stops");

            if (stops != null) {
                for (int i = 0; i < Math.min(stops.length(), 5); i++) {
                    JSONObject stop = stops.getJSONObject(i);
                    String stopName = stop.optString("stop_name", "Bus Stop " + (i + 1));

                    // Simulate bus data for this stop
                    buses.add(new RealtimeBusInfo(
                            "Route " + (100 + i),
                            stopName,
                            5 + (i * 3),
                            (40 + (i * 10)) + "%",
                            "Live"
                    ));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing transit response: " + e.getMessage(), e);
        }
    }

    /**
     * Update bus display with live dot indicator
     */
    private void updateBusDisplay() {
        try {
            // Clear existing bus views
            layoutBusList.removeAllViews();

            if (nearbyBuses.isEmpty()) {
                showNoBusesFound();
                showLiveUpdateIndicator(false);
                return;
            }

            // Add each bus as a card with light theme styling
            for (RealtimeBusInfo bus : nearbyBuses) {
                View busCard = createBusCardLightTheme(bus);
                layoutBusList.addView(busCard);
            }

            // Show live update dot to indicate fresh data
            showLiveUpdateIndicator(true);

            // Hide dot after 3 seconds
            mainHandler.postDelayed(() -> showLiveUpdateIndicator(false), 3000);

            // Update bus count in notification
            updateBusArrivalNotification();

        } catch (Exception e) {
            Log.e(TAG, "Error updating bus display: " + e.getMessage(), e);
        }
    }

    /**
     * Create bus card with light theme (black and white)
     */
    private View createBusCardLightTheme(RealtimeBusInfo bus) {
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(16, 12, 16, 12);

        // Light theme styling - white background with black border
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

        // Light theme color coding - subtle borders instead of background colors
        if (bus.arrivalMinutes <= 3) {
            // Green border for urgent buses
            cardLayout.setBackground(createColorBorder("#4CAF50"));
        } else if (bus.arrivalMinutes <= 8) {
            // Orange border for moderately urgent buses
            cardLayout.setBackground(createColorBorder("#FF9800"));
        } else {
            // Gray border for normal buses
            cardLayout.setBackground(createColorBorder("#E0E0E0"));
        }

        return cardLayout;
    }

    /**
     * Create colored border for light theme
     */
    private android.graphics.drawable.Drawable createColorBorder(String color) {
        try {
            android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
            drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            drawable.setColor(getResources().getColor(android.R.color.white));
            drawable.setStroke(3, android.graphics.Color.parseColor(color));
            drawable.setCornerRadius(8);
            return drawable;
        } catch (Exception e) {
            Log.e(TAG, "Error creating colored border: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Show/hide live update dot without changing background.
     * This method controls the visibility and animation of the live update indicator dot.
     * Call with 'true' to show and animate the dot, or 'false' to hide it.
     */
    private void showLiveUpdateIndicator(boolean show) {
        try {
            View liveUpdateDot = findViewById(R.id.liveUpdateDot);
            if (liveUpdateDot != null) {
                liveUpdateDot.setVisibility(show ? View.VISIBLE : View.GONE);

                // Always bring the dot to front for visibility
                if (show) {
                    liveUpdateDot.bringToFront();
                    animateLiveUpdateDot(liveUpdateDot);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing live update indicator: " + e.getMessage(), e);
        }
    }

    /**
     * Animate the live update dot
     */
    private void animateLiveUpdateDot(View dot) {
        try {
            // Simple pulse animation
            dot.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(500)
                .withEndAction(() -> {
                    dot.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(500)
                        .start();
                })
                .start();
        } catch (Exception e) {
            Log.e(TAG, "Error animating dot: " + e.getMessage(), e);
        }
    }

    /**
     * Enhanced bus loading state with dot indicator
     */
    private void showBusLoadingState(boolean show) {
        try {
            if (progressBarBuses != null) {
                progressBarBuses.setVisibility(show ? View.VISIBLE : View.GONE);
            }

            // Show live update dot during loading
            showLiveUpdateIndicator(show);

        } catch (Exception e) {
            Log.e(TAG, "Error showing bus loading state: " + e.getMessage(), e);
        }
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
        layoutBusList.addView(noBusesText);
    }

    /**
     * Show error state for bus data
     */
    private void showErrorBusState() {
        TextView errorText = new TextView(this);
        errorText.setText("⚠️ Error loading bus data\nTap refresh to try again");
        errorText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        errorText.setPadding(16, 24, 16, 24);
        errorText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        layoutBusList.addView(errorText);
    }

    /**
     * Update last update time
     */
    private void updateLastUpdateTime() {
        if (textViewLastUpdate != null) {
            Calendar now = Calendar.getInstance();
            String timeString = String.format("Last updated: %02d:%02d",
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE));
            textViewLastUpdate.setText(timeString);
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
     * Refresh bus data manually
     */
    private void refreshBusData() {
        try {
            if (currentLocation != null) {
                // Show live update dot immediately
                showLiveUpdateIndicator(true);

                fetchRealTimeBusData();
                Toast.makeText(this, "🔄 Refreshing bus data...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "📍 Getting location first...", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing bus data: " + e.getMessage(), e);
            showLiveUpdateIndicator(false);
        }
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
            String loc = String.format("Lat: %.4f, Lon: %.4f",
                    currentLocation.getLatitude(), currentLocation.getLongitude());
            textViewCurrentLocation.setText(loc);
        }
    }

    /**
     * Load real-time bus updates (initial call)
     */
    private void loadRealTimeBusUpdates() {
        // This can be used for any additional setup if needed
        // For now, handled by startRealTimeBusUpdates()
    }

    /**
     * Navigation helpers
     */
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
        startActivity(intent);
    }

    private void navigateToEmergency() {
        Intent intent = new Intent(this, EmergencyActivity.class);
        startActivity(intent);
    }

    private void navigateToTripHistory() {
        Intent intent = new Intent(this, TripHistoryActivity.class);
        startActivity(intent);
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
        if (mainHandler != null) {
            mainHandler.removeCallbacks(busUpdateRunnable);
        }
        Log.d(TAG, "PassengerDashboardActivity destroyed");
    }
}
