package com.s23010421.busmate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * LiveBusTrackingActivity for EEI4369 BusMate Project
 * Implements Google Maps integration with real-time bus tracking
 * Author: V.P.N. Hansaka (S23010421)
 */
public class LiveBusTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "LiveBusTracking";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds

    // UI Components
    private GoogleMap mMap;
    private TextView textViewBusRoute;
    private TextView textViewMapRoute;
    private TextView textViewETA;
    private TextView textViewCapacity;
    private Button buttonBack;
    private Button buttonRefresh;
    private Button buttonSetReminder;
    private Button buttonContactDriver;

    // Location Services (EEI4369 Lab Session 3 - Google Maps Integration)
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    // Data Management
    private ExecutorService executorService;
    private Handler mainHandler;
    private Marker busMarker;
    private Marker userMarker;

    // Bus Data from Intent
    private String routeNumber;
    private String destination;
    private String arrivalTime;
    private String busStop;

    // Tracking State
    private boolean isTrackingActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "LiveBusTrackingActivity onCreate started");
            setContentView(R.layout.activity_live_bus_tracking);

            // Initialize components
            initializeComponents();
            getBusDataFromIntent();
            initializeViews();
            initializeLocationServices();
            setupGoogleMap();
            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorAndFinish("Error loading live tracking");
        }
    }

    /**
     * Initialize core components
     */
    private void initializeComponents() {
        executorService = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());

        // Setup location request for real-time updates
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(5000);

        // Location callback for continuous updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    currentLocation = locationResult.getLastLocation();
                    updateUserLocationOnMap();
                    updateBusLocationSimulation();
                }
            }
        };
    }

    /**
     * Get bus data from intent extras
     */
    private void getBusDataFromIntent() {
        try {
            routeNumber = getIntent().getStringExtra("route_number");
            destination = getIntent().getStringExtra("destination");
            arrivalTime = getIntent().getStringExtra("arrival_time");
            busStop = getIntent().getStringExtra("bus_stop");

            // Set defaults if data is missing (matching the image)
            if (routeNumber == null) routeNumber = "245";
            if (destination == null) destination = "Colombo Express";
            if (arrivalTime == null) arrivalTime = "5 min";
            if (busStop == null) busStop = "Current Location";

            Log.d(TAG, "Bus data loaded - Route: " + routeNumber + ", Destination: " + destination);

        } catch (Exception e) {
            Log.e(TAG, "Error getting bus data: " + e.getMessage(), e);
            // Set default values matching the image
            routeNumber = "245";
            destination = "Colombo Express";
            arrivalTime = "5 min";
            busStop = "Current Location";
        }
    }

    /**
     * Initialize UI components to match the image design
     */
    private void initializeViews() {
        try {
            textViewBusRoute = findViewById(R.id.textViewBusRoute);
            textViewMapRoute = findViewById(R.id.textViewMapRoute);
            textViewETA = findViewById(R.id.textViewETA);
            textViewCapacity = findViewById(R.id.textViewCapacity);
            buttonBack = findViewById(R.id.buttonBack);
            buttonRefresh = findViewById(R.id.buttonRefresh);
            buttonSetReminder = findViewById(R.id.buttonSetReminder);
            buttonContactDriver = findViewById(R.id.buttonContactDriver);

            // Set initial information exactly as shown in image
            updateUI();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }

    /**
     * Update UI with data to match the image
     */
    private void updateUI() {
        runOnUiThread(() -> {
            try {
                if (textViewBusRoute != null) {
                    textViewBusRoute.setText("Route " + routeNumber + " - " + destination);
                }
                if (textViewMapRoute != null) {
                    textViewMapRoute.setText("Route " + routeNumber + " - " + destination);
                }
                if (textViewETA != null) {
                    textViewETA.setText(arrivalTime);
                }
                if (textViewCapacity != null) {
                    textViewCapacity.setText("78% Full");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating UI: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Initialize location services
     */
    private void initializeLocationServices() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing location services: " + e.getMessage(), e);
        }
    }

    /**
     * Setup Google Map (EEI4369 Lab Session 3 Implementation)
     */
    private void setupGoogleMap() {
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            } else {
                Log.e(TAG, "Map fragment not found");
                showErrorAndFinish("Map not available");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up map: " + e.getMessage(), e);
            showErrorAndFinish("Error loading map");
        }
    }

    /**
     * Setup click listeners to match the image functionality
     */
    private void setupClickListeners() {
        try {
            if (buttonBack != null) {
                buttonBack.setOnClickListener(v -> {
                    stopLocationUpdates();
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
            }

            if (buttonRefresh != null) {
                buttonRefresh.setOnClickListener(v -> {
                    refreshBusData();
                    Toast.makeText(this, "Refreshing bus location...", Toast.LENGTH_SHORT).show();
                });
            }

            if (buttonSetReminder != null) {
                buttonSetReminder.setOnClickListener(v -> {
                    setArrivalReminder();
                });
            }

            if (buttonContactDriver != null) {
                buttonContactDriver.setOnClickListener(v -> {
                    contactDriver();
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Google Maps callback - EEI4369 Lab Session 3 Integration
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        try {
            mMap = googleMap;
            Log.d(TAG, "Google Map ready");

            // Configure map UI
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);

            // Set default location (Negombo area for demo)
            LatLng negomboArea = new LatLng(7.2906, 79.9000);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(negomboArea, 13.0f));

            // Add initial bus marker
            addBusMarker(negomboArea);

            // Check location permission and start tracking
            checkLocationPermission();

        } catch (Exception e) {
            Log.e(TAG, "Error in onMapReady: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Add bus marker to map
     */
    private void addBusMarker(LatLng position) {
        if (mMap != null) {
            // Remove existing bus marker
            if (busMarker != null) {
                busMarker.remove();
            }

            // Add new bus marker
            busMarker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Bus " + routeNumber)
                    .snippet(destination + " • Capacity: 78% Full")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    /**
     * Check and request location permission
     */
    private void checkLocationPermission() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                enableLocationTracking();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking location permission: " + e.getMessage(), e);
        }
    }

    /**
     * Enable location tracking
     */
    private void enableLocationTracking() {
        try {
            if (mMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);
                startLocationUpdates();
                isTrackingActive = true;

                Toast.makeText(this, "Live tracking activated", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error enabling location tracking: " + e.getMessage(), e);
        }
    }

    /**
     * Start location updates
     */
    private void startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                        Looper.getMainLooper());

                // Get initial location
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                currentLocation = location;
                                updateUserLocationOnMap();
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting location updates: " + e.getMessage(), e);
        }
    }

    /**
     * Stop location updates
     */
    private void stopLocationUpdates() {
        try {
            if (fusedLocationClient != null && locationCallback != null) {
                fusedLocationClient.removeLocationUpdates(locationCallback);
                isTrackingActive = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error stopping location updates: " + e.getMessage(), e);
        }
    }

    /**
     * Update user location on map
     */
    private void updateUserLocationOnMap() {
        if (currentLocation != null && mMap != null) {
            LatLng userLocation = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());

            // Remove existing user marker
            if (userMarker != null) {
                userMarker.remove();
            }

            // Add user location marker
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    /**
     * Update bus location (simulation for demo)
     */
    private void updateBusLocationSimulation() {
        if (mMap != null) {
            // Simulate bus movement around Negombo area
            double lat = 7.2906 + (Math.random() - 0.5) * 0.01;
            double lng = 79.9000 + (Math.random() - 0.5) * 0.01;

            LatLng newBusLocation = new LatLng(lat, lng);
            addBusMarker(newBusLocation);
        }
    }

    /**
     * Refresh bus data
     */
    private void refreshBusData() {
        if (isTrackingActive) {
            updateBusLocationSimulation();
            updateUI();
        } else {
            Toast.makeText(this, "Location tracking not active", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set arrival reminder
     */
    private void setArrivalReminder() {
        try {
            Toast.makeText(this, "Reminder set for bus arrival in " + arrivalTime,
                    Toast.LENGTH_LONG).show();

            // In real implementation, this would set an actual reminder/notification
            Log.d(TAG, "Arrival reminder set for Route " + routeNumber);

        } catch (Exception e) {
            Log.e(TAG, "Error setting reminder: " + e.getMessage(), e);
        }
    }

    /**
     * Contact driver functionality
     */
    private void contactDriver() {
        try {
            Intent intent = new Intent(this, ContactDriverActivity.class);
            intent.putExtra("route_number", routeNumber);
            intent.putExtra("driver_name", "Bus Driver");
            intent.putExtra("bus_id", "CM-" + routeNumber);

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        } catch (Exception e) {
            Log.e(TAG, "Error contacting driver: " + e.getMessage(), e);
            Toast.makeText(this, "Driver contact feature temporarily unavailable",
                    Toast.LENGTH_SHORT).show();
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
                enableLocationTracking();
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission required for live tracking",
                        Toast.LENGTH_LONG).show();
            }
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
    protected void onResume() {
        super.onResume();
        if (isTrackingActive && currentLocation != null) {
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

        Log.d(TAG, "LiveBusTrackingActivity destroyed");
    }
}
