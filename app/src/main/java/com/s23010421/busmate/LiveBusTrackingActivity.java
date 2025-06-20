package com.s23010421.busmate;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * LiveBusTrackingActivity - Real-time bus tracking with Google Maps integration
 * Provides live bus locations, route visualization, and passenger communication
 * Implements comprehensive bus tracking features for BusMate passengers
 */
public class LiveBusTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private GoogleMap mMap;
    private TextView textViewSelectedRoute;
    private TextView textViewBusNumber;
    private TextView textViewDestination;
    private TextView textViewCurrentCapacity;
    private TextView textViewEstimatedArrival;
    private TextView textViewNextStop;
    private TextView textViewDistance;
    private CardView cardViewBusInfo;
    private Button buttonNotifyArrival;
    private Button buttonShareLocation;
    private Button buttonReportIssue;
    private Button buttonContactDriver;
    private Switch switchNotifications;
    private LinearLayout layoutBusSelection;

    // Location and Map
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private Marker selectedBusMarker;
    private Marker userLocationMarker;

    // Bus tracking data
    private String selectedRouteNumber = "245";
    private String selectedBusId = "WP-CM-5847";
    private boolean isNotificationEnabled = false;

    // Database and preferences
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "BusMatePrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_bus_tracking);

        // Initialize components
        initializeViews();
        initializeLocationServices();
        setupClickListeners();
        loadTrackingPreferences();

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Load initial bus data
        loadBusTrackingData();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        textViewSelectedRoute = findViewById(R.id.textViewSelectedRoute);
        textViewBusNumber = findViewById(R.id.textViewBusNumber);
        textViewDestination = findViewById(R.id.textViewDestination);
        textViewCurrentCapacity = findViewById(R.id.textViewCurrentCapacity);
        textViewEstimatedArrival = findViewById(R.id.textViewEstimatedArrival);
        textViewNextStop = findViewById(R.id.textViewNextStop);
        textViewDistance = findViewById(R.id.textViewDistance);
        cardViewBusInfo = findViewById(R.id.cardViewBusInfo);
        buttonNotifyArrival = findViewById(R.id.buttonNotifyArrival);
        buttonShareLocation = findViewById(R.id.buttonShareLocation);
        buttonReportIssue = findViewById(R.id.buttonReportIssue);
        buttonContactDriver = findViewById(R.id.buttonContactDriver);
        switchNotifications = findViewById(R.id.switchNotifications);
        layoutBusSelection = findViewById(R.id.layoutBusSelection);

        // Initialize database
        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    /**
     * Initialize location services
     */
    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();
    }

    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Notify arrival toggle
        buttonNotifyArrival.setOnClickListener(v -> toggleArrivalNotification());

        // Share location
        buttonShareLocation.setOnClickListener(v -> shareCurrentLocation());

        // Report issue
        buttonReportIssue.setOnClickListener(v -> reportBusIssue());

        // Contact driver
        buttonContactDriver.setOnClickListener(v -> contactBusDriver());

        // Notification switch
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isNotificationEnabled = isChecked;
            saveTrackingPreferences();
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        // Bus info card click for details
        cardViewBusInfo.setOnClickListener(v -> showBusDetails());
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
     * Get current user location
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
                            updateMapWithUserLocation();
                        }
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Enable location layer if permission granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Set map click listener for bus selection
        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() != null && marker.getTag().toString().startsWith("bus_")) {
                selectBus(marker);
                return true;
            }
            return false;
        });

        // Load initial map data
        loadBusesOnMap();
        centerMapOnRoute();
    }

    /**
     * Load real-time bus data and display on map
     */
    private void loadBusesOnMap() {
        // Simulated bus locations for Route 245
        LatLng[] busLocations = {
                new LatLng(6.9271, 79.8612), // Bus 1 - Near Negombo
                new LatLng(6.9541, 79.8648), // Bus 2 - Ja-Ela
                new LatLng(7.0873, 79.8956), // Bus 3 - Kelaniya
                new LatLng(7.2906, 79.9085)  // Bus 4 - Near Colombo
        };

        String[] busNumbers = {"WP-CM-5847", "WP-BM-2341", "WP-KB-8765", "WP-CD-1234"};
        int[] capacities = {78, 45, 92, 56};

        for (int i = 0; i < busLocations.length; i++) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(busLocations[i])
                    .title("Bus " + busNumbers[i])
                    .snippet("Route 245 • " + capacities[i] + "% Full")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker));

            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag("bus_" + busNumbers[i]);

                // Select first bus by default
                if (i == 0) {
                    selectedBusMarker = marker;
                    selectBus(marker);
                }
            }
        }

        // Add route path
        addRoutePathToMap();
    }

    /**
     * Add route path visualization to map
     */
    private void addRoutePathToMap() {
        // Route 245 path coordinates (Negombo to Colombo)
        LatLng[] routePath = {
                new LatLng(6.9271, 79.8612), // Negombo Bus Station
                new LatLng(6.9541, 79.8648), // Ja-Ela
                new LatLng(7.0123, 79.8789), // Wattala
                new LatLng(7.0873, 79.8956), // Kelaniya
                new LatLng(7.1564, 79.9123), // Peliyagoda
                new LatLng(7.2456, 79.9234), // Dematagoda
                new LatLng(7.2906, 79.9085)  // Colombo Fort
        };

        PolylineOptions polylineOptions = new PolylineOptions()
                .color(ContextCompat.getColor(this, R.color.route_path_color))
                .width(8f);

        for (LatLng point : routePath) {
            polylineOptions.add(point);
        }

        mMap.addPolyline(polylineOptions);

        // Add bus stops
        addBusStopsToMap(routePath);
    }

    /**
     * Add bus stops to map
     */
    private void addBusStopsToMap(LatLng[] stops) {
        String[] stopNames = {
                "Negombo Bus Station",
                "Ja-Ela Junction",
                "Wattala Bus Stand",
                "Kelaniya Temple",
                "Peliyagoda",
                "Dematagoda Railway",
                "Colombo Fort"
        };

        for (int i = 0; i < stops.length; i++) {
            MarkerOptions stopMarker = new MarkerOptions()
                    .position(stops[i])
                    .title(stopNames[i])
                    .snippet("Route 245 Stop")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_marker));

            mMap.addMarker(stopMarker);
        }
    }

    /**
     * Select a bus and update UI
     */
    private void selectBus(Marker marker) {
        selectedBusMarker = marker;
        String busTag = marker.getTag().toString();
        String busNumber = busTag.replace("bus_", "");

        updateBusInformationPanel(busNumber);

        // Animate camera to selected bus
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                marker.getPosition(), 15f));
    }

    /**
     * Update bus information panel
     */
    private void updateBusInformationPanel(String busNumber) {
        selectedBusId = busNumber;

        // Update UI with selected bus data
        textViewSelectedRoute.setText("Route 245");
        textViewBusNumber.setText("Bus " + busNumber);
        textViewDestination.setText("Colombo Fort");

        // Simulated real-time data
        switch (busNumber) {
            case "WP-CM-5847":
                textViewCurrentCapacity.setText("78% Full");
                textViewEstimatedArrival.setText("5 minutes");
                textViewNextStop.setText("Ja-Ela Junction");
                textViewDistance.setText("2.3 km away");
                break;
            case "WP-BM-2341":
                textViewCurrentCapacity.setText("45% Full");
                textViewEstimatedArrival.setText("12 minutes");
                textViewNextStop.setText("Wattala Bus Stand");
                textViewDistance.setText("5.8 km away");
                break;
            case "WP-KB-8765":
                textViewCurrentCapacity.setText("92% Full");
                textViewEstimatedArrival.setText("18 minutes");
                textViewNextStop.setText("Peliyagoda");
                textViewDistance.setText("8.2 km away");
                break;
            case "WP-CD-1234":
                textViewCurrentCapacity.setText("56% Full");
                textViewEstimatedArrival.setText("25 minutes");
                textViewNextStop.setText("Colombo Fort");
                textViewDistance.setText("12.1 km away");
                break;
        }
    }

    /**
     * Center map on route
     */
    private void centerMapOnRoute() {
        // Center on Kelaniya (middle of route)
        LatLng routeCenter = new LatLng(7.0873, 79.8956);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routeCenter, 12f));
    }

    /**
     * Update map with user location
     */
    private void updateMapWithUserLocation() {
        if (currentLocation != null && mMap != null) {
            LatLng userLatLng = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());

            if (userLocationMarker != null) {
                userLocationMarker.remove();
            }

            userLocationMarker = mMap.addMarker(new MarkerOptions()
                    .position(userLatLng)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_location)));
        }
    }

    /**
     * Toggle arrival notification
     */
    private void toggleArrivalNotification() {
        isNotificationEnabled = !isNotificationEnabled;

        String message = isNotificationEnabled ?
                "You'll be notified when " + selectedBusId + " approaches" :
                "Arrival notifications disabled for " + selectedBusId;

        buttonNotifyArrival.setText(isNotificationEnabled ?
                "Cancel Notification" : "Notify When Arriving");

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        saveTrackingPreferences();
    }

    /**
     * Share current location with others
     */
    private void shareCurrentLocation() {
        if (currentLocation != null) {
            String shareText = "I'm tracking Bus " + selectedBusId + " on Route " + selectedRouteNumber +
                    " via BusMate. My location: https://maps.google.com/?q=" +
                    currentLocation.getLatitude() + "," + currentLocation.getLongitude();

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "BusMate - Live Bus Tracking");

            startActivity(Intent.createChooser(shareIntent, "Share location via"));
        } else {
            Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Report bus issue
     */
    private void reportBusIssue() {
        // TODO: Implement issue reporting
        Toast.makeText(this, "Issue reporting for Bus " + selectedBusId + " - Coming Soon",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Contact bus driver
     */
    private void contactBusDriver() {
        Intent intent = new Intent(this, DriverCommunicationActivity.class);
        intent.putExtra("bus_number", selectedBusId);
        intent.putExtra("route_number", selectedRouteNumber);
        startActivity(intent);
    }

    /**
     * Show detailed bus information
     */
    private void showBusDetails() {
        Intent intent = new Intent(this, BusDetailsActivity.class);
        intent.putExtra("bus_number", selectedBusId);
        intent.putExtra("route_number", selectedRouteNumber);
        startActivity(intent);
    }

    /**
     * Load tracking preferences
     */
    private void loadTrackingPreferences() {
        isNotificationEnabled = sharedPreferences.getBoolean("arrival_notifications", false);
        switchNotifications.setChecked(isNotificationEnabled);
    }

    /**
     * Save tracking preferences
     */
    private void saveTrackingPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("arrival_notifications", isNotificationEnabled);
        editor.apply();
    }

    /**
     * Load bus tracking data
     */
    private void loadBusTrackingData() {
        // Get route from intent or use default
        selectedRouteNumber = getIntent().getStringExtra("route_number");
        if (selectedRouteNumber == null) {
            selectedRouteNumber = "245";
        }

        textViewSelectedRoute.setText("Route " + selectedRouteNumber);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
                if (mMap != null) {
                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(this, "Location permission required for live tracking",
                        Toast.LENGTH_LONG).show();
            }
        }
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
        Intent intent = new Intent(this, PassengerDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
