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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 * COMPLETE UPDATED: PassengerDashboardActivity for EEI4369 BusMate Project
 * V.P.N. Hansaka (S23010421)
 * Features implemented for EEI4369:
 * - SQLite Database Integration (Lab Session 5)
 * - Google Maps Integration (Lab Session 3) - Separate Map and Live Tracking
 * - Real-time Bus Tracking with GPS
 * - Accelerometer Integration (Lab Session 4)
 * - Multimedia Integration (Lab Session 2)
 * - Professional UI/UX Design with proper navigation
 * - Complete error handling and null-safe operations
 */
public class PassengerDashboardActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "PassengerDashboard";
    private static final int BUS_UPDATE_INTERVAL = 30000; // 30 seconds
    private static final String PREF_NAME = "BusMatePrefs";

    // OpenWeatherMap API Configuration
    private static final String WEATHER_API_KEY = "ec1f4893aaa6a68f1317c36bbc78ec2f";
    private static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final int WEATHER_UPDATE_INTERVAL = 300000; // 5 minutes

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

    // Nearby Buses Section
    private LinearLayout layoutNearbyBuses;
    private TextView textViewNearbyBusesTitle;
    private CardView cardViewRoute245;
    private CardView cardViewRoute187;

    // Real-Time Notifications Section
    private CardView cardViewLiveBusUpdates;
    private TextView textViewBusArrivalNotification;
    private Button buttonTrackBus;

    // Bottom Navigation
    private BottomNavigationView bottomNavigationView;

    // Database and Location Services (EEI4369 Lab Session 5 - SQLite Integration)
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences sharedPreferences;

    // EEI4369 Lab Session 4 - Sensor Integration (Optional UI Elements)
    private TextView textViewMotionStatus;
    private TextView textViewAccelerometerData;
    private AccelerometerManager accelerometerManager;
    private boolean isAccelerometerActive = false;

    // Real-time Data Management
    private ExecutorService executorService;
    private Handler mainHandler;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private List<RealtimeBusInfo> nearbyBuses;
    private boolean isLocationUpdatesActive = false;
    private Timer busUpdateTimer;
    private Timer weatherUpdateTimer;

    // User Data
    private long passengerId;
    private String passengerName;
    private String passengerEmail;
    private Location currentLocation;

    // Emergency State for Accelerometer
    private boolean isEmergencyModeActive = false;
    private int emergencyShakeCount = 0;

    // Cache for geocoding results to avoid repeated API calls
    private final java.util.Map<String, String> locationCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, Long> cacheTimestamps = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 10 * 60 * 1000; // 10 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "=== EEI4369 Enhanced Dashboard onCreate started ===");
            setContentView(R.layout.activity_passenger_dashboard);

            // Initialize components with error handling
            if (!initializeViews()) {
                Log.e(TAG, "Failed to initialize views");
                showErrorAndRedirect("Error loading dashboard views");
                return;
            }

            initializeServices();
            initializeRealTimeComponents();

            // EEI4369 Lab Session 4 - Safe accelerometer initialization
            initializeAccelerometerSafely();

            // Enhanced data loading with validation (SQLite Integration)
            if (!loadPassengerData()) {
                Log.e(TAG, "Failed to load passenger data");
                showErrorAndRedirect("Unable to load user data");
                return;
            }

            setupClickListeners();
            setupBottomNavigation();
            setupNearbyBusesSection();
            requestLocationPermission();
            setPersonalizedGreeting();
            loadRealTimeBusUpdates();

            // Start real-time bus tracking
            startBusUpdates();

            // Start weather updates
            startWeatherUpdates();

        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate: " + e.getMessage(), e);
            showErrorAndRedirect("Dashboard initialization failed");
        }
    }

    /**
     * Enhanced view initialization with safe error handling
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

            // Add null checks for critical views
            if (cardViewFindBus == null || cardViewBuyTicket == null) {
                Log.e(TAG, "Critical CardViews not found in layout");
                return false;
            }

            // Nearby buses section
            layoutNearbyBuses = findViewById(R.id.layoutNearbyBuses);
            textViewNearbyBusesTitle = findViewById(R.id.textViewNearbyBusesTitle);

            // Real-time notifications
            cardViewLiveBusUpdates = findViewById(R.id.cardViewLiveBusUpdates);
            textViewBusArrivalNotification = findViewById(R.id.textViewBusArrivalNotification);
            buttonTrackBus = findViewById(R.id.buttonTrackBus);

            // Bottom navigation
            bottomNavigationView = findViewById(R.id.bottomNavigationPassenger);

            // Optional motion status views (safe initialization)
            try {
                textViewMotionStatus = findViewById(R.id.textViewMotionStatus);
                textViewAccelerometerData = findViewById(R.id.textViewAccelerometerData);
                Log.d(TAG, "✅ Motion status views found");
            } catch (Exception e) {
                Log.d(TAG, "⚠️ Motion status views not found - will use alternative displays");
                textViewMotionStatus = null;
                textViewAccelerometerData = null;
            }

            Log.d(TAG, "All views initialized successfully");
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
        try {
            databaseHelper = new DatabaseHelper(this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            Log.d(TAG, "Services initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing services: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize real-time components for EEI4369 demonstration
     */
    private void initializeRealTimeComponents() {
        try {
            executorService = Executors.newFixedThreadPool(3);
            mainHandler = new Handler(Looper.getMainLooper());
            nearbyBuses = new ArrayList<>();

            // Setup location request for real-time updates (EEI4369 Lab Session 3)
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
                        fetchCurrentWeather(); // Fetch weather when location updates
                    }
                }
            };

            Log.d(TAG, "Real-time components initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing real-time components: " + e.getMessage(), e);
        }
    }

    /**
     * Safe accelerometer initialization - EEI4369 Lab Session 4
     */
    private void initializeAccelerometerSafely() {
        try {
            accelerometerManager = new AccelerometerManager(this);

            if (accelerometerManager.isAccelerometerAvailable()) {
                accelerometerManager.setAccelerometerListener(new AccelerometerManager.AccelerometerListener() {

                    @Override
                    public void onShakeDetected() {
                        handleEmergencyShakeSafely();
                    }

                    @Override
                    public void onWalkingDetected(boolean walking) {
                        handleWalkingDetectionSafely(walking);
                    }

                    @Override
                    public void onBusMovementDetected(boolean moving) {
                        handleBusMovementSafely(moving);
                    }

                    @Override
                    public void onAccelerometerChanged(float x, float y, float z) {
                        updateMotionIndicatorsSafely(x, y, z);
                    }
                });

                // Start accelerometer monitoring
                boolean started = accelerometerManager.startListening();
                isAccelerometerActive = started;

                Log.d(TAG, "✅ EEI4369 Accelerometer initialized safely: " + started);

                if (started) {
                    Toast.makeText(this, "🔄 Motion detection active", Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.w(TAG, "⚠️ Accelerometer not available");
                Toast.makeText(this, "Motion detection not available", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error initializing accelerometer (non-critical): " + e.getMessage(), e);
            accelerometerManager = null;
        }
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
                        String displayName = passengerName != null ? passengerName : "Sarah";
                        textViewPassengerName.setText(displayName);
                    } else {
                        textViewPassengerName.setText("Sarah");
                    }
                } else {
                    Log.w(TAG, "No passenger profile found for user ID: " + passengerId);
                    textViewPassengerName.setText("Sarah");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading passenger data: " + e.getMessage(), e);
                textViewPassengerName.setText("Sarah");
            } finally {
                if (passengerCursor != null) {
                    passengerCursor.close();
                }
            }

            // Set initial location placeholder until real location is obtained
            if (textViewCurrentLocation != null) {
                textViewCurrentLocation.setText("Getting your location...");
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
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        try {
            // Quick Actions with null checks
            if (cardViewFindBus != null) {
                cardViewFindBus.setOnClickListener(v -> {
                    // Navigate to Live Bus Tracking (Find Bus functionality)
                    navigateToLiveBusTracking();
                });
            }

            if (cardViewBuyTicket != null) {
                cardViewBuyTicket.setOnClickListener(v -> {
                    navigateToTicketBooking();
                });
            }

            if (cardViewEmergency != null) {
                cardViewEmergency.setOnClickListener(v -> {
                    navigateToEmergency();
                });
            }

            if (cardViewTripHistory != null) {
                cardViewTripHistory.setOnClickListener(v -> {
                    navigateToTripHistory();
                });
            }

            // Live Bus Updates with null checks
            if (cardViewLiveBusUpdates != null) {
                cardViewLiveBusUpdates.setOnClickListener(v -> {
                    navigateToLiveBusTracking();
                });
            }

            if (buttonTrackBus != null) {
                buttonTrackBus.setOnClickListener(v -> {
                    navigateToLiveBusTracking();
                });
            }

            // Profile photo click
            if (imageViewPassengerPhoto != null) {
                imageViewPassengerPhoto.setOnClickListener(v -> {
                    navigateToProfile();
                });
            }

            Log.d(TAG, "Click listeners setup completed");

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Setup nearby buses section with clickable cards
     */
    private void setupNearbyBusesSection() {
        try {
            if (layoutNearbyBuses != null) {
                // Create dynamic bus cards or use existing ones
                setupBusCard245();
                setupBusCard187();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up nearby buses section: " + e.getMessage(), e);
        }
    }

    /**
     * Setup Route 245 bus card
     */
    private void setupBusCard245() {
        try {
            // Try to find existing card or create new one
            cardViewRoute245 = findViewById(R.id.cardViewRoute245);
            if (cardViewRoute245 == null) {
                // If card doesn't exist in layout, look for it in layoutNearbyBuses
                if (layoutNearbyBuses != null && layoutNearbyBuses.getChildCount() > 0) {
                    View firstChild = layoutNearbyBuses.getChildAt(0);
                    if (firstChild instanceof CardView) {
                        cardViewRoute245 = (CardView) firstChild;
                    }
                }
            }

            if (cardViewRoute245 != null) {
                cardViewRoute245.setOnClickListener(v -> {
                    trackSpecificBus("245", "Colombo Express", "5 min", "78% Full");
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up bus card 245: " + e.getMessage(), e);
        }
    }

    /**
     * Setup Route 187 bus card
     */
    private void setupBusCard187() {
        try {
            // Try to find existing card or create new one
            cardViewRoute187 = findViewById(R.id.cardViewRoute187);
            if (cardViewRoute187 == null) {
                // If card doesn't exist in layout, look for it in layoutNearbyBuses
                if (layoutNearbyBuses != null && layoutNearbyBuses.getChildCount() > 1) {
                    View secondChild = layoutNearbyBuses.getChildAt(1);
                    if (secondChild instanceof CardView) {
                        cardViewRoute187 = (CardView) secondChild;
                    }
                }
            }

            if (cardViewRoute187 != null) {
                cardViewRoute187.setOnClickListener(v -> {
                    trackSpecificBus("187", "Airport Link", "12 min", "45% Full");
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up bus card 187: " + e.getMessage(), e);
        }
    }

    /**
     * Track specific bus from nearby buses section - Navigate to Live Bus Tracking
     */
    private void trackSpecificBus(String routeNumber, String destination, String arrivalTime, String capacity) {
        try {
            Intent intent = new Intent(this, LiveBusTrackingActivity.class);
            intent.putExtra("route_number", routeNumber);
            intent.putExtra("destination", destination);
            intent.putExtra("arrival_time", arrivalTime);
            intent.putExtra("capacity", capacity);
            intent.putExtra("bus_stop", "Current Location");

            if (currentLocation != null) {
                intent.putExtra("user_latitude", currentLocation.getLatitude());
                intent.putExtra("user_longitude", currentLocation.getLongitude());
            }

            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            Toast.makeText(this, "Tracking Route " + routeNumber + " - " + destination,
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error tracking specific bus: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening live tracking", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set personalized greeting based on time of day
     */
    private void setPersonalizedGreeting() {
        if (textViewGreeting == null)
            return;

        try {
            Calendar calendar = Calendar.getInstance();
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

            String greeting;
            if (hourOfDay >= 5 && hourOfDay < 12) {
                greeting = "Good Morning,";
            } else if (hourOfDay >= 12 && hourOfDay < 17) {
                greeting = "Good Afternoon,";
            } else if (hourOfDay >= 17 && hourOfDay < 21) {
                greeting = "Good Evening,";
            } else {
                greeting = "Good Night,";
            }

            textViewGreeting.setText(greeting);
        } catch (Exception e) {
            Log.e(TAG, "Error setting greeting: " + e.getMessage(), e);
            textViewGreeting.setText("Hello,");
        }
    }

    /**
     * UPDATED: Set up bottom navigation actions with separate Map and Live Tracking
     */
    /**
     * Setup bottom navigation without profile tab
     */
    private void setupBottomNavigation() {
        if (bottomNavigationView == null)
            return;

        try {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    return true; // Already on home
                } else if (itemId == R.id.navigation_map) {
                    Intent mapIntent = new Intent(PassengerDashboardActivity.this, MapActivity.class);
                    startActivity(mapIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (itemId == R.id.navigation_trips) {
                    Intent tripIntent = new Intent(PassengerDashboardActivity.this, TripHistoryActivity.class);
                    tripIntent.putExtra("user_id", passengerId);
                    startActivity(tripIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    return true;
                } else if (itemId == R.id.navigation_tickets) {
                    navigateToTicketBooking();
                    return true;
                } else if (itemId == R.id.navigation_more) {
                    navigateToMore();
                    return true;
                }
                return false;
            });

        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation: " + e.getMessage(), e);
        }
    }

    /**
     * Start real-time bus updates
     */
    private void startBusUpdates() {
        try {
            if (textViewNearbyBusesTitle != null) {
                textViewNearbyBusesTitle.setText("Nearby Buses");
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
     * Schedule periodic updates
     */
    private void schedulePeriodicUpdates() {
        try {
            if (busUpdateTimer != null) {
                busUpdateTimer.cancel();
            }

            busUpdateTimer = new Timer();
            busUpdateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (currentLocation != null) {
                        fetchRealTimeBusData();
                    }
                }
            }, 0, BUS_UPDATE_INTERVAL);

        } catch (Exception e) {
            Log.e(TAG, "Error scheduling periodic updates: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch real-time bus data
     */
    private void fetchRealTimeBusData() {
        if (currentLocation == null) {
            Log.w(TAG, "Current location not available for bus data fetch");
            return;
        }

        executorService.execute(() -> {
            try {
                List<RealtimeBusInfo> buses = new ArrayList<>();
                fetchSimulatedData(buses);

                mainHandler.post(() -> {
                    nearbyBuses.clear();
                    nearbyBuses.addAll(buses);
                    updateBusArrivalNotification();

                    Log.d(TAG, "Found " + buses.size() + " nearby buses");
                });

            } catch (Exception e) {
                Log.e(TAG, "Error fetching real-time bus data: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Fetch simulated data for demonstration
     */
    private void fetchSimulatedData(List<RealtimeBusInfo> buses) {
        try {
            String[] routes = { "245", "187" };
            String[] destinations = { "Colombo Express", "Airport Link" };
            int[] arrivalTimes = { 5, 12 };
            String[] capacities = { "78%", "45%" };

            for (int i = 0; i < routes.length; i++) {
                buses.add(new RealtimeBusInfo(
                        routes[i],
                        destinations[i],
                        arrivalTimes[i],
                        capacities[i],
                        "On Time"));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error generating simulated data: " + e.getMessage(), e);
        }
    }

    /**
     * Update bus arrival notification
     */
    private void updateBusArrivalNotification() {
        if (textViewBusArrivalNotification == null || nearbyBuses.isEmpty()) {
            if (textViewBusArrivalNotification != null) {
                textViewBusArrivalNotification.setText("Bus #245 arriving in 5 minutes");
            }
            return;
        }

        try {
            RealtimeBusInfo nearestBus = nearbyBuses.get(0);
            for (RealtimeBusInfo bus : nearbyBuses) {
                if (bus.arrivalMinutes < nearestBus.arrivalMinutes) {
                    nearestBus = bus;
                }
            }

            String notification = "🚌 Route " + nearestBus.routeNumber + " arriving in " +
                    nearestBus.arrivalMinutes + " minutes";
            textViewBusArrivalNotification.setText(notification);

        } catch (Exception e) {
            Log.e(TAG, "Error updating bus arrival notification: " + e.getMessage(), e);
        }
    }

    /**
     * Load real-time bus updates
     */
    private void loadRealTimeBusUpdates() {
        // Handled by startBusUpdates()
    }

    // ===========================================
    // ACCELEROMETER EVENT HANDLERS (EEI4369 Lab Session 4)
    // ===========================================

    /**
     * Handle emergency shake with safe UI updates
     */
    private void handleEmergencyShakeSafely() {
        runOnUiThread(() -> {
            try {
                emergencyShakeCount++;

                Log.d(TAG, "🚨 EMERGENCY SHAKE DETECTED! Count: " + emergencyShakeCount);

                if (emergencyShakeCount == 1) {
                    Toast.makeText(this, "🚨 Emergency shake detected! Shake 2 more times to activate",
                            Toast.LENGTH_SHORT).show();
                    updateMotionStatusSafely("🚨 Shake detected! Shake 2 more times for emergency");

                } else if (emergencyShakeCount == 2) {
                    Toast.makeText(this, "🚨 Shake one more time to activate emergency!",
                            Toast.LENGTH_SHORT).show();
                    updateMotionStatusSafely("🚨 ONE MORE SHAKE for emergency activation!");

                } else if (emergencyShakeCount >= 3) {
                    activateEmergencyFromShake();
                    emergencyShakeCount = 0;
                }

                // Reset shake counter after 5 seconds
                mainHandler.removeCallbacksAndMessages(null);
                mainHandler.postDelayed(() -> {
                    emergencyShakeCount = 0;
                    updateMotionStatusSafely("");
                }, 5000);

            } catch (Exception e) {
                Log.e(TAG, "Error handling emergency shake: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Safe motion status updates
     */
    private void updateMotionStatusSafely(String message) {
        if (textViewMotionStatus != null) {
            textViewMotionStatus.setText(message);
            textViewMotionStatus.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
        } else {
            // Use current location text as alternative
            if (textViewCurrentLocation != null && !message.isEmpty()) {
                textViewCurrentLocation.setText(message);
            }
        }
    }

    /**
     * Activate emergency from shake gesture
     */
    private void activateEmergencyFromShake() {
        try {
            Log.d(TAG, "🚨 EMERGENCY ACTIVATED BY SHAKE GESTURE!");
            isEmergencyModeActive = true;

            Toast.makeText(this, "🚨 EMERGENCY ACTIVATED BY SHAKE!\nNotifying emergency contacts...",
                    Toast.LENGTH_LONG).show();

            Intent emergencyIntent = new Intent(this, EmergencyActivity.class);
            emergencyIntent.putExtra("emergency_trigger", "shake_gesture");
            emergencyIntent.putExtra("auto_activate", true);

            if (currentLocation != null) {
                emergencyIntent.putExtra("user_latitude", currentLocation.getLatitude());
                emergencyIntent.putExtra("user_longitude", currentLocation.getLongitude());
            }

            startActivity(emergencyIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            Log.e(TAG, "Error activating emergency from shake: " + e.getMessage(), e);
            Toast.makeText(this, "Error activating emergency", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle walking detection safely
     */
    private void handleWalkingDetectionSafely(boolean walking) {
        runOnUiThread(() -> {
            try {
                if (walking) {
                    Log.d(TAG, "👤 Walking detected - enhancing location tracking");
                    if (textViewCurrentLocation != null) {
                        if (currentLocation != null) {
                            textViewCurrentLocation.setText("👤 Walking detected - Updating location...");
                            // Update with actual location after a short delay
                            mainHandler.postDelayed(() -> updateLocationDisplay(), 1000);
                        } else {
                            textViewCurrentLocation.setText("👤 Walking detected - Getting location...");
                        }
                    }
                    updateMotionStatusSafely("👤 Walking to bus stop - Enhanced tracking");
                    startLocationUpdates();

                    if (textViewBusArrivalNotification != null) {
                        textViewBusArrivalNotification.setText("🚶‍♂️ Walking detected - Live updates enhanced");
                    }

                } else {
                    Log.d(TAG, "🛑 Walking stopped");
                    // Show current location when stopped walking
                    updateLocationDisplay();
                    updateMotionStatusSafely("🛑 Stationary - Standard GPS tracking");

                    if (textViewBusArrivalNotification != null) {
                        textViewBusArrivalNotification.setText("Bus #245 arriving in 5 minutes");
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "Error handling walking detection: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Handle bus movement safely
     */
    private void handleBusMovementSafely(boolean moving) {
        runOnUiThread(() -> {
            try {
                if (moving) {
                    Log.d(TAG, "🚌 Bus movement detected");
                    if (textViewBusArrivalNotification != null) {
                        textViewBusArrivalNotification.setText("🚌 Bus in motion - Live tracking enhanced");
                    }
                    updateMotionStatusSafely("🚌 Vehicle motion detected - Enhanced tracking");
                    fetchRealTimeBusData();

                } else {
                    Log.d(TAG, "🚏 Bus stopped");
                    if (textViewBusArrivalNotification != null) {
                        textViewBusArrivalNotification.setText("🚏 Bus stopped - Check if at your stop");
                    }
                    updateMotionStatusSafely("🚏 Bus stopped - May be at bus stop");
                }

            } catch (Exception e) {
                Log.e(TAG, "Error handling bus movement: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Update motion indicators safely
     */
    private void updateMotionIndicatorsSafely(float x, float y, float z) {
        float motionIntensity = (float) Math.sqrt(x * x + y * y + z * z) - 9.8f;

        if (textViewAccelerometerData != null && Math.abs(motionIntensity) > 0.2f) {
            runOnUiThread(() -> {
                String dataText = String.format("📱 Motion: X=%.2f Y=%.2f Z=%.2f", x, y, z);
                textViewAccelerometerData.setText(dataText);
                textViewAccelerometerData.setVisibility(View.VISIBLE);
            });
        }

        if (isEmergencyModeActive && Math.abs(motionIntensity) > 1.0f) {
            runOnUiThread(() -> {
                updateMotionStatusSafely("🚨 Emergency mode - High motion detected");
            });
        }
    }

    // ===========================================
    // LOCATION SERVICES (EEI4369 Lab Session 3)
    // ===========================================

    /**
     * Request location permission
     */
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
                // Start location updates immediately for better accuracy
                startLocationUpdates();
                Toast.makeText(this, "✅ Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Location permission required for live bus updates", Toast.LENGTH_LONG).show();
                // Set fallback location display
                if (textViewCurrentLocation != null) {
                    textViewCurrentLocation.setText(" Location permission required");
                }
                if (textViewCurrentWeather != null) {
                    textViewCurrentWeather.setText("🌤️ Location needed for weather");
                }
            }
        }
    }

    /**
     * Get current location
     */
    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location;
                            updateLocationDisplay();
                            fetchRealTimeBusData();
                            fetchCurrentWeather(); // Fetch weather with initial location
                        } else {
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
        if (isLocationUpdatesActive)
            return;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            isLocationUpdatesActive = true;
        } catch (Exception e) {
            Log.e(TAG, "Error starting location updates: " + e.getMessage(), e);
        }
    }

    /**
     * Stop location updates
     */
    private void stopLocationUpdates() {
        if (!isLocationUpdatesActive)
            return;
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isLocationUpdatesActive = false;
        } catch (Exception e) {
            Log.e(TAG, "Error stopping location updates: " + e.getMessage(), e);
        }
    }

    /**
     * Update location display with enhanced error handling and fallback strategies
     */
    private void updateLocationDisplay() {
        if (textViewCurrentLocation != null && currentLocation != null) {
            try {
                // Show progressive loading messages
                showLocationLoadingProgress();

                // Get address using reverse geocoding in background
                executorService.execute(() -> {
                    String address = getCachedAddressFromLocation(currentLocation);

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        if (textViewCurrentLocation != null) {
                            if (address != null && !address.isEmpty()) {
                                textViewCurrentLocation.setText(address);
                                Log.d(TAG, "✅ Location displayed: " + address);
                            } else {
                                // If reverse geocoding fails, show coordinates as fallback
                                String coords = String.format(java.util.Locale.US, "📍 Near %.4f°, %.4f°",
                                        currentLocation.getLatitude(), currentLocation.getLongitude());
                                textViewCurrentLocation.setText(coords);
                                Log.w(TAG, "⚠️ Using coordinates fallback: " + coords);

                                // Try again after a delay with all strategies
                                mainHandler.postDelayed(() -> {
                                    Log.d(TAG, "🔄 Retrying reverse geocoding with all strategies...");
                                    executorService.execute(() -> {
                                        String retryAddress = getCachedAddressFromLocation(currentLocation);
                                        if (retryAddress != null && !retryAddress.isEmpty()) {
                                            runOnUiThread(() -> {
                                                if (textViewCurrentLocation != null) {
                                                    textViewCurrentLocation.setText(retryAddress);
                                                    Log.d(TAG, "✅ Retry successful: " + retryAddress);
                                                }
                                            });
                                        }
                                    });
                                }, 5000); // Retry after 5 seconds
                            }
                        }
                    });
                });

            } catch (Exception e) {
                Log.e(TAG, "Error updating location display: " + e.getMessage(), e);
                textViewCurrentLocation.setText(" Location unavailable");
            }
        } else if (textViewCurrentLocation != null) {
            textViewCurrentLocation.setText(" Getting your location...");
        }
    }

    /**
     * Get cached address or fetch new one if not cached or expired
     */
    private String getCachedAddressFromLocation(Location location) {
        if (location == null)
            return null;

        // Create cache key from rounded coordinates (to avoid cache misses for tiny
        // location changes)
        String cacheKey = String.format(java.util.Locale.US, "%.4f,%.4f",
                location.getLatitude(), location.getLongitude());

        // Check if we have a valid cached result
        if (locationCache.containsKey(cacheKey)) {
            Long timestamp = cacheTimestamps.get(cacheKey);
            if (timestamp != null && (System.currentTimeMillis() - timestamp) < CACHE_DURATION) {
                String cachedAddress = locationCache.get(cacheKey);
                Log.d(TAG, "🗂️ Using cached address: " + cachedAddress);
                return cachedAddress;
            } else {
                // Cache expired, remove it
                locationCache.remove(cacheKey);
                cacheTimestamps.remove(cacheKey);
            }
        }

        // Fetch new address
        String address = getAddressFromLocation(location);

        // Cache the result if valid
        if (address != null && !address.isEmpty() && !isCoordinateString(address)) {
            locationCache.put(cacheKey, address);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            Log.d(TAG, "💾 Cached address: " + address + " for key: " + cacheKey);
        }

        return address;
    }

    /**
     * Get readable address from location using multiple strategies
     */
    private String getAddressFromLocation(Location location) {
        // Strategy 1: Use Android Geocoder (fastest)
        String address = tryAndroidGeocoder(location);
        if (address != null && !address.isEmpty()) {
            return address;
        }

        // Strategy 2: Use OpenStreetMap Nominatim (backup)
        address = tryNominatimGeocoder(location);
        if (address != null && !address.isEmpty()) {
            return address;
        }

        // Strategy 3: Use a simplified area name based on coordinates
        return getAreaNameFromCoordinates(location);
    }

    /**
     * Try Android's built-in Geocoder
     */
    private String tryAndroidGeocoder(Location location) {
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(this, java.util.Locale.getDefault());

            if (!geocoder.isPresent()) {
                Log.w(TAG, "Android Geocoder not available");
                return null;
            }

            java.util.List<android.location.Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    5); // Get more results for better accuracy

            if (addresses != null && !addresses.isEmpty()) {
                // Try each address until we find a good one
                for (android.location.Address address : addresses) {
                    String result = parseAddressComponents(address);
                    if (result != null && !result.isEmpty() && !isCoordinateString(result)) {
                        Log.d(TAG, "✅ Android Geocoder success: " + result);
                        return result;
                    }
                }
            }

            Log.w(TAG, "⚠️ Android Geocoder: No useful address components found");

        } catch (Exception e) {
            Log.e(TAG, "❌ Android Geocoder error: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Parse address components with improved logic
     */
    private String parseAddressComponents(android.location.Address address) {
        // Log all available address components for debugging
        Log.d(TAG, "Address components:");
        Log.d(TAG, "- Locality: " + address.getLocality());
        Log.d(TAG, "- SubLocality: " + address.getSubLocality());
        Log.d(TAG, "- AdminArea: " + address.getAdminArea());
        Log.d(TAG, "- SubAdminArea: " + address.getSubAdminArea());
        Log.d(TAG, "- CountryName: " + address.getCountryName());
        Log.d(TAG, "- FeatureName: " + address.getFeatureName());
        Log.d(TAG, "- Thoroughfare: " + address.getThoroughfare());

        // Try different combinations to get the best result
        String[] strategies = {
                // Strategy 1: Locality + SubAdminArea
                buildAddressString(address.getLocality(), address.getSubAdminArea()),
                // Strategy 2: SubLocality + Locality
                buildAddressString(address.getSubLocality(), address.getLocality()),
                // Strategy 3: SubLocality + SubAdminArea
                buildAddressString(address.getSubLocality(), address.getSubAdminArea()),
                // Strategy 4: Just Locality
                address.getLocality(),
                // Strategy 5: Just SubLocality
                address.getSubLocality(),
                // Strategy 6: Just SubAdminArea
                address.getSubAdminArea(),
                // Strategy 7: Just AdminArea
                address.getAdminArea(),
                // Strategy 8: FeatureName
                address.getFeatureName(),
                // Strategy 9: Thoroughfare
                address.getThoroughfare()
        };

        for (String strategy : strategies) {
            if (strategy != null && !strategy.trim().isEmpty() &&
                    !isCoordinateString(strategy) && !isNumericOnly(strategy)) {
                return strategy.trim();
            }
        }

        // Final fallback - parse address line
        if (address.getAddressLine(0) != null && !address.getAddressLine(0).isEmpty()) {
            return parseAddressLine(address.getAddressLine(0));
        }

        return null;
    }

    /**
     * Build address string from two components
     */
    private String buildAddressString(String primary, String secondary) {
        if (primary == null || primary.trim().isEmpty()) {
            return secondary;
        }
        if (secondary == null || secondary.trim().isEmpty()) {
            return primary;
        }
        if (primary.equals(secondary)) {
            return primary;
        }
        return primary + ", " + secondary;
    }

    /**
     * Parse address line to extract meaningful location name
     */
    private String parseAddressLine(String addressLine) {
        try {
            String[] parts = addressLine.split(",");
            for (String part : parts) {
                part = part.trim();
                if (!part.isEmpty() && !isNumericOnly(part) &&
                        !isCoordinateString(part) && part.length() > 2) {
                    // Skip postal codes and numbers
                    if (!part.matches("\\d{5,}") && !part.matches("^\\d+.*")) {
                        return part;
                    }
                }
            }
            // If no good part found, return the original
            return addressLine;
        } catch (Exception e) {
            return addressLine;
        }
    }

    /**
     * Try OpenStreetMap Nominatim for reverse geocoding (backup method)
     */
    private String tryNominatimGeocoder(Location location) {
        // Check network connectivity first
        if (!isNetworkAvailable()) {
            Log.w(TAG, "⚠️ No network connectivity for Nominatim geocoder");
            return null;
        }

        try {
            String url = String.format(java.util.Locale.US,
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&zoom=10&addressdetails=1",
                    location.getLatitude(), location.getLongitude());

            java.net.URL nominatimUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) nominatimUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "BusMate Android App");

            if (connection.getResponseCode() == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String result = parseNominatimResponse(response.toString());
                if (result != null) {
                    Log.d(TAG, "✅ Nominatim geocoder success: " + result);
                }
                return result;
            } else {
                Log.w(TAG, "⚠️ Nominatim API error - Response code: " + connection.getResponseCode());
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Nominatim geocoder error: " + e.getMessage());
        }

        return null;
    }

    /**
     * Parse Nominatim JSON response
     */
    private String parseNominatimResponse(String jsonResponse) {
        try {
            // Simple JSON parsing without external libraries
            if (jsonResponse.contains("\"city\"")) {
                String city = extractJsonValue(jsonResponse, "city");
                String state = extractJsonValue(jsonResponse, "state");
                if (city != null && !city.isEmpty()) {
                    if (state != null && !state.isEmpty() && !city.equals(state)) {
                        return city + ", " + state;
                    }
                    return city;
                }
            }

            if (jsonResponse.contains("\"town\"")) {
                String town = extractJsonValue(jsonResponse, "town");
                if (town != null && !town.isEmpty()) {
                    return town;
                }
            }

            if (jsonResponse.contains("\"village\"")) {
                String village = extractJsonValue(jsonResponse, "village");
                if (village != null && !village.isEmpty()) {
                    return village;
                }
            }

            if (jsonResponse.contains("\"suburb\"")) {
                String suburb = extractJsonValue(jsonResponse, "suburb");
                if (suburb != null && !suburb.isEmpty()) {
                    return suburb;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing Nominatim response: " + e.getMessage());
        }

        return null;
    }

    /**
     * Extract value from JSON string (simple parser)
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting JSON value: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get approximate area name based on coordinates (final fallback)
     */
    private String getAreaNameFromCoordinates(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        // Sri Lanka coordinate ranges and major cities/areas
        if (lat >= 5.9 && lat <= 9.9 && lon >= 79.5 && lon <= 81.9) {
            // Northern areas
            if (lat >= 8.5) {
                return "Northern Region, Sri Lanka";
            }
            // Central areas
            else if (lat >= 7.0 && lat <= 8.0 && lon >= 80.5 && lon <= 81.5) {
                return "Central Region, Sri Lanka";
            }
            // Western areas (including Colombo)
            else if (lon <= 80.0) {
                return "Western Region, Sri Lanka";
            }
            // Eastern areas
            else if (lon >= 81.0) {
                return "Eastern Region, Sri Lanka";
            }
            // Southern areas
            else if (lat <= 6.5) {
                return "Southern Region, Sri Lanka";
            }
            // Default for Sri Lanka
            return "Sri Lanka";
        }

        // For locations outside Sri Lanka, use general region names
        return String.format(java.util.Locale.US, "Location %.4f°, %.4f°", lat, lon);
    }

    /**
     * Check if a string contains coordinate-like patterns
     */
    private boolean isCoordinateString(String str) {
        if (str == null)
            return false;
        return str.matches(".*\\d+\\.\\d+.*") && (str.contains("°") || str.contains(","));
    }

    /**
     * Check if a string is numeric only
     */
    private boolean isNumericOnly(String str) {
        if (str == null || str.trim().isEmpty())
            return false;
        return str.trim().matches("^\\d+$");
    }

    // ===========================================
    // WEATHER API METHODS (OpenWeatherMap Integration)
    // ===========================================

    /**
     * Fetch current weather for the user's location
     */
    private void fetchCurrentWeather() {
        if (currentLocation == null) {
            Log.w(TAG, "Current location not available for weather fetch");
            if (textViewCurrentWeather != null) {
                textViewCurrentWeather.setText("🌤️ Location needed for weather");
            }
            return;
        }

        executorService.execute(() -> {
            try {
                String weatherData = getWeatherFromAPI(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude());

                if (weatherData != null) {
                    String weatherDisplay = parseWeatherData(weatherData);

                    runOnUiThread(() -> {
                        if (textViewCurrentWeather != null && weatherDisplay != null) {
                            textViewCurrentWeather.setText(weatherDisplay);
                        } else if (textViewCurrentWeather != null) {
                            textViewCurrentWeather.setText("🌤️ Weather unavailable");
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        if (textViewCurrentWeather != null) {
                            textViewCurrentWeather.setText("🌤️ Weather error");
                        }
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    if (textViewCurrentWeather != null) {
                        textViewCurrentWeather.setText("🌤️ Weather error");
                    }
                });
            }
        });
    }

    /**
     * Get weather data from OpenWeatherMap API
     */
    private String getWeatherFromAPI(double latitude, double longitude) {
        try {
            String urlString = WEATHER_BASE_URL +
                    "?lat=" + latitude +
                    "&lon=" + longitude +
                    "&appid=" + WEATHER_API_KEY +
                    "&units=metric"; // Use Celsius

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds timeout
            connection.setReadTimeout(10000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Log.d(TAG, "Weather API response: " + response.toString());
                return response.toString();

            } else {
                Log.e(TAG, "Weather API error - Response code: " + responseCode);
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error calling weather API: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parse weather data from API response
     */
    private String parseWeatherData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            // Get temperature
            JSONObject main = jsonObject.getJSONObject("main");
            double temp = main.getDouble("temp");
            int temperature = (int) Math.round(temp);

            // Get weather description
            JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
            String description = weather.getString("main");
            String weatherIcon = getWeatherIcon(description);

            // Get humidity and feels like
            int humidity = main.getInt("humidity");
            double feelsLike = main.getDouble("feels_like");

            // Build weather display string
            String weatherDisplay = weatherIcon + " " + temperature + "°C";

            Log.d(TAG, "Parsed weather: " + weatherDisplay + " (" + description + ")");
            return weatherDisplay;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing weather data: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get appropriate weather icon based on weather condition
     */
    private String getWeatherIcon(String weatherCondition) {
        switch (weatherCondition.toLowerCase()) {
            case "clear":
                return "☀️";
            case "clouds":
                return "☁️";
            case "rain":
                return "🌧️";
            case "drizzle":
                return "🌦️";
            case "thunderstorm":
                return "⛈️";
            case "snow":
                return "❄️";
            case "mist":
            case "fog":
                return "🌫️";
            case "haze":
                return "🌫️";
            default:
                return "🌤️";
        }
    }

    /**
     * Start periodic weather updates
     */
    private void startWeatherUpdates() {
        try {
            if (weatherUpdateTimer != null) {
                weatherUpdateTimer.cancel();
            }

            weatherUpdateTimer = new Timer();
            weatherUpdateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (currentLocation != null) {
                        fetchCurrentWeather();
                    }
                }
            }, 0, WEATHER_UPDATE_INTERVAL); // Update every 5 minutes

            Log.d(TAG, "Weather updates started");

        } catch (Exception e) {
            Log.e(TAG, "Error starting weather updates: " + e.getMessage(), e);
        }
    }

    // ===========================================
    // NAVIGATION METHODS (UPDATED)
    // ===========================================

    /**
     * Navigate to Live Bus Tracking (Find Bus functionality)
     */
    private void navigateToLiveBusTracking() {
        try {
            Intent intent = new Intent(this, LiveBusTrackingActivity.class);

            // Default values for Find Bus functionality
            intent.putExtra("route_number", "245");
            intent.putExtra("destination", "Colombo Express");
            intent.putExtra("arrival_time", "5 min");
            intent.putExtra("capacity", "78% Full");
            intent.putExtra("bus_stop", "Current Location");

            if (currentLocation != null) {
                intent.putExtra("user_latitude", currentLocation.getLatitude());
                intent.putExtra("user_longitude", currentLocation.getLongitude());
            }

            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to live tracking: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening live tracking", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * NEW: Navigate to general Map screen (Map button in nav bar)
     */
    private void navigateToMapScreen() {
        try {
            Intent intent = new Intent(this, MapActivity.class);

            if (currentLocation != null) {
                intent.putExtra("user_latitude", currentLocation.getLatitude());
                intent.putExtra("user_longitude", currentLocation.getLongitude());
            }

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            Log.e(TAG, "Error navigating to map: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening map", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate to Ticket Booking
     */
    private void navigateToTicketBooking() {
        try {
            Intent intent = new Intent(this, TicketBookingActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to ticket booking: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening ticket booking", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate to Emergency
     */
    private void navigateToEmergency() {
        try {
            Intent intent = new Intent(this, EmergencyActivity.class);

            if (currentLocation != null) {
                intent.putExtra("user_latitude", currentLocation.getLatitude());
                intent.putExtra("user_longitude", currentLocation.getLongitude());
            }

            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to emergency: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening emergency", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate to Trip History
     */
    private void navigateToTripHistory() {
        try {
            Intent intent = new Intent(this, TripHistoryActivity.class);
            intent.putExtra("user_id", passengerId);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to trip history: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening trip history", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate to Profile
     */
    private void navigateToProfile() {
        try {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("user_id", passengerId);
            intent.putExtra("user_email", passengerEmail);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to profile: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening profile", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigate to More
     */
    private void navigateToMore() {
        try {
            Intent intent = new Intent(this, MoreActivity.class);
            intent.putExtra("user_id", passengerId);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to more: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening more", Toast.LENGTH_SHORT).show();
        }
    }

    // ===========================================
    // ERROR HANDLING AND UTILITY METHODS
    // ===========================================

    /**
     * Error handling and redirect method
     */
    private void showErrorAndRedirect(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(PassengerDashboardActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }

    // ===========================================
    // DATA CLASSES
    // ===========================================

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

    // ===========================================
    // ACTIVITY LIFECYCLE METHODS
    // ===========================================

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (isLocationUpdatesActive) {
                startLocationUpdates();
            }

            // Resume accelerometer safely
            if (accelerometerManager != null && !isAccelerometerActive) {
                boolean started = accelerometerManager.startListening();
                isAccelerometerActive = started;
                Log.d(TAG, "Accelerometer resumed: " + started);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            stopLocationUpdates();

            // Pause accelerometer to save battery (unless in emergency)
            if (!isEmergencyModeActive && accelerometerManager != null && isAccelerometerActive) {
                accelerometerManager.stopListening();
                isAccelerometerActive = false;
                Log.d(TAG, "Accelerometer paused");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onPause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            stopLocationUpdates();

            // Clean up accelerometer
            if (accelerometerManager != null) {
                accelerometerManager.stopListening();
                accelerometerManager = null;
            }

            // Clean up timers and threads
            if (busUpdateTimer != null) {
                busUpdateTimer.cancel();
                busUpdateTimer = null;
            }

            if (weatherUpdateTimer != null) {
                weatherUpdateTimer.cancel();
                weatherUpdateTimer = null;
            }

            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }

            if (databaseHelper != null) {
                databaseHelper.close();
            }

            Log.d(TAG, "EEI4369 Enhanced PassengerDashboardActivity destroyed successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: " + e.getMessage(), e);
        }
    }

    /**
     * Show progressive loading messages to give user feedback during geocoding
     */
    private void showLocationLoadingProgress() {
        if (textViewCurrentLocation == null)
            return;

        final String[] loadingMessages = {
                "📍 Finding your location...",
                "📍 Getting address details...",
                "📍 Resolving location name...",
                "📍 Almost there..."
        };

        final android.os.Handler progressHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        final java.util.concurrent.atomic.AtomicInteger messageIndex = new java.util.concurrent.atomic.AtomicInteger(0);

        // Update loading message every 1.5 seconds
        Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (textViewCurrentLocation != null &&
                        textViewCurrentLocation.getText().toString().contains("📍") &&
                        !textViewCurrentLocation.getText().toString().contains(",") &&
                        !textViewCurrentLocation.getText().toString().contains("Near")) {

                    int index = messageIndex.getAndIncrement();
                    if (index < loadingMessages.length) {
                        textViewCurrentLocation.setText(loadingMessages[index]);
                        progressHandler.postDelayed(this, 1500);
                    }
                }
            }
        };

        progressHandler.post(progressRunnable);
    }

    /**
     * Test all geocoding strategies for debugging (can be called from UI for
     * testing)
     */
    private void testAllGeocodingStrategies() {
        if (currentLocation == null) {
            Log.w(TAG, "Cannot test geocoding: No current location available");
            return;
        }

        Log.d(TAG, "🧪 Testing all geocoding strategies for location: " +
                currentLocation.getLatitude() + ", " + currentLocation.getLongitude());

        executorService.execute(() -> {
            // Test Strategy 1: Android Geocoder
            Log.d(TAG, "📱 Testing Android Geocoder...");
            String androidResult = tryAndroidGeocoder(currentLocation);
            Log.d(TAG, "📱 Android Geocoder result: " + (androidResult != null ? androidResult : "FAILED"));

            // Test Strategy 2: Nominatim
            Log.d(TAG, "🌍 Testing Nominatim Geocoder...");
            String nominatimResult = tryNominatimGeocoder(currentLocation);
            Log.d(TAG, "🌍 Nominatim result: " + (nominatimResult != null ? nominatimResult : "FAILED"));

            // Test Strategy 3: Area Name from Coordinates
            Log.d(TAG, "📍 Testing coordinate-based area name...");
            String areaResult = getAreaNameFromCoordinates(currentLocation);
            Log.d(TAG, "📍 Area name result: " + areaResult);

            // Test combined strategy
            Log.d(TAG, "🔄 Testing combined strategy...");
            String combinedResult = getAddressFromLocation(currentLocation);
            Log.d(TAG, "🔄 Combined strategy result: " + (combinedResult != null ? combinedResult : "FAILED"));

            // Show results in UI
            runOnUiThread(() -> {
                if (textViewCurrentLocation != null) {
                    String bestResult = combinedResult != null ? combinedResult
                            : (androidResult != null ? androidResult
                            : (nominatimResult != null ? nominatimResult : areaResult));
                    textViewCurrentLocation.setText("📍 " + bestResult);
                    Log.d(TAG, "✅ Final result displayed: " + bestResult);
                }
            });
        });
    }

    /**
     * Check if device has internet connectivity for external geocoding APIs
     */
    private boolean isNetworkAvailable() {
        try {
            android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(
                    android.content.Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    android.net.Network network = connectivityManager.getActiveNetwork();
                    android.net.NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                    return capabilities != null &&
                            capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                } else {
                    android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    return networkInfo != null && networkInfo.isConnected();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network connectivity: " + e.getMessage());
        }
        return false;
    }
}
