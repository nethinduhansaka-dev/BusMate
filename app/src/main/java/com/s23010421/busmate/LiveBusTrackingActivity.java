package com.s23010421.busmate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.view.View;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced LiveBusTrackingActivity with Real-Time Data Integration
 * Supports multiple transit APIs for live bus tracking
 */
public class LiveBusTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "LiveBusTracking";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds
    private static final int BUS_DATA_UPDATE_INTERVAL = 15000; // 15 seconds
    private static final double SEARCH_RADIUS_KM = 2.0; // 2km radius

    // UI Components
    private GoogleMap mMap;
    private TextView textViewBusRoute;
    private TextView textViewBusStatus;
    private TextView textViewETA;
    private TextView textViewNearbyBuses;
    private Button buttonBack;
    private Button buttonRefresh;
    private ProgressBar progressBar;

    // Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng currentUserLocation;

    // Data Management
    private ExecutorService executorService;
    private Handler mainHandler;
    private List<BusInfo> nearbyBuses;
    private List<Marker> busMarkers;
    private Circle searchRadiusCircle;

    // Bus Data
    private String routeNumber;
    private String destination;
    private String busStop;
    private String arrivalTime;

    // Real-time tracking
    private boolean isTrackingActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "Enhanced LiveBusTrackingActivity onCreate started");
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
            showErrorAndFinish("Error loading bus tracking");
        }
    }

    /**
     * Initialize core components
     */
    private void initializeComponents() {
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
        nearbyBuses = new ArrayList<>();
        busMarkers = new ArrayList<>();

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
                    Location location = locationResult.getLastLocation();
                    currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    if (isTrackingActive) {
                        updateUserLocationOnMap();
                        fetchNearbyBuses();
                    }
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
            busStop = getIntent().getStringExtra("bus_stop");
            arrivalTime = getIntent().getStringExtra("arrival_time");

            // Set defaults if data is missing
            if (routeNumber == null) routeNumber = "Live Tracking";
            if (destination == null) destination = "Real-time Bus Data";
            if (busStop == null) busStop = "Your Location";
            if (arrivalTime == null) arrivalTime = "Updating...";

            Log.d(TAG, "Bus data loaded - Route: " + routeNumber + ", Destination: " + destination);

        } catch (Exception e) {
            Log.e(TAG, "Error getting bus data: " + e.getMessage(), e);
            // Set default values
            routeNumber = "Live Tracking";
            destination = "Real-time Bus Data";
            busStop = "Your Location";
            arrivalTime = "Updating...";
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        try {
            textViewBusRoute = findViewById(R.id.textViewBusRoute);
            textViewBusStatus = findViewById(R.id.textViewBusStatus);
            textViewETA = findViewById(R.id.textViewETA);
            textViewNearbyBuses = findViewById(R.id.textViewNearbyBuses);
            buttonBack = findViewById(R.id.buttonBack);
            buttonRefresh = findViewById(R.id.buttonRefresh);
            progressBar = findViewById(R.id.progressBar);

            // Set initial information
            updateUI();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
        }
    }

    /**
     * Update UI with current data
     */
    private void updateUI() {
        runOnUiThread(() -> {
            try {
                if (textViewBusRoute != null) {
                    textViewBusRoute.setText(routeNumber + " - " + destination);
                }
                if (textViewBusStatus != null) {
                    String status = isTrackingActive ? "🚌 Live Tracking Active" : "📍 Getting Location...";
                    textViewBusStatus.setText(status);
                }
                if (textViewETA != null) {
                    textViewETA.setText("ETA: " + arrivalTime);
                }
                if (textViewNearbyBuses != null) {
                    textViewNearbyBuses.setText("Nearby Buses: " + nearbyBuses.size());
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
     * Setup Google Map
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
     * Setup click listeners
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
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage(), e);
        }
    }

    /**
     * Google Maps callback - Enhanced with real-time features
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        try {
            mMap = googleMap;
            Log.d(TAG, "Google Map ready");

            // Configure map
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);

            // Check location permission and start tracking
            checkLocationPermission();

        } catch (Exception e) {
            Log.e(TAG, "Error in onMapReady: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show();
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
     * Enable real-time location tracking
     */
    private void enableLocationTracking() {
        try {
            if (mMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);
                startLocationUpdates();
                isTrackingActive = true;
                updateUI();

                Toast.makeText(this, "Starting live bus tracking...", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error enabling location tracking: " + e.getMessage(), e);
        }
    }

    /**
     * Start continuous location updates
     */
    private void startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

                // Get initial location
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                centerMapOnUser();
                                addSearchRadiusCircle();
                                fetchNearbyBuses();
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
     * Center map on user location
     */
    private void centerMapOnUser() {
        if (mMap != null && currentUserLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15.0f));
        }
    }

    /**
     * Add search radius circle to map
     */
    private void addSearchRadiusCircle() {
        if (mMap != null && currentUserLocation != null) {
            // Remove existing circle
            if (searchRadiusCircle != null) {
                searchRadiusCircle.remove();
            }

            // Add new circle
            searchRadiusCircle = mMap.addCircle(new CircleOptions()
                    .center(currentUserLocation)
                    .radius(SEARCH_RADIUS_KM * 1000) // Convert km to meters
                    .strokeColor(0x330000FF)
                    .fillColor(0x110000FF)
                    .strokeWidth(2));
        }
    }

    /**
     * Update user location marker on map
     */
    private void updateUserLocationOnMap() {
        if (currentUserLocation != null) {
            addSearchRadiusCircle();
        }
    }

    /**
     * Fetch nearby buses using real-time data
     */
    private void fetchNearbyBuses() {
        if (currentUserLocation == null) return;

        showProgress(true);

        executorService.execute(() -> {
            try {
                List<BusInfo> buses = new ArrayList<>();

                // Try multiple data sources for comprehensive coverage
                fetchFromGTFS(buses);
                fetchFromOpenData(buses);
                fetchFromTransitLand(buses);

                mainHandler.post(() -> {
                    nearbyBuses.clear();
                    nearbyBuses.addAll(buses);
                    updateBusMarkersOnMap();
                    updateUI();
                    showProgress(false);

                    if (buses.isEmpty()) {
                        Toast.makeText(this, "No buses found nearby. Expanding search...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Found " + buses.size() + " nearby buses", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error fetching bus data: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    showProgress(false);
                    Toast.makeText(this, "Error fetching bus data", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Fetch from GTFS Real-time feeds
     */
    private void fetchFromGTFS(List<BusInfo> buses) {
        try {
            // Example GTFS-RT URL (replace with your local transit authority's feed)
            String gtfsUrl = "https://api.transitland.org/api/v2/rest/vehicles"
                    + "?lat=" + currentUserLocation.latitude
                    + "&lon=" + currentUserLocation.longitude
                    + "&radius=" + (SEARCH_RADIUS_KM * 1000);

            String jsonResponse = makeHttpRequest(gtfsUrl);
            if (jsonResponse != null) {
                parseGTFSResponse(jsonResponse, buses);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching GTFS data: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch from Open Data APIs
     */
    private void fetchFromOpenData(List<BusInfo> buses) {
        try {
            // For Singapore (since user is in Singapore)
            if (isInSingapore()) {
                fetchSingaporeTransitData(buses);
            }

            // Add more regional APIs as needed

        } catch (Exception e) {
            Log.e(TAG, "Error fetching open data: " + e.getMessage(), e);
        }
    }

    /**
     * Check if user is in Singapore
     */
    private boolean isInSingapore() {
        if (currentUserLocation == null) return false;
        // Singapore bounds approximately
        return currentUserLocation.latitude >= 1.16 && currentUserLocation.latitude <= 1.48 &&
                currentUserLocation.longitude >= 103.6 && currentUserLocation.longitude <= 104.1;
    }

    /**
     * Fetch Singapore transit data
     */
    private void fetchSingaporeTransitData(List<BusInfo> buses) {
        try {
            // Use LTA DataMall API for Singapore bus data
            // Note: You'll need to register for API key at datamall.lta.gov.sg
            String apiKey = "YOUR_LTA_API_KEY"; // Replace with actual API key

            if (!"YOUR_LTA_API_KEY".equals(apiKey)) {
                String url = "http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2";
                // Implementation for Singapore LTA API
            }

        } catch (Exception e) {
            Log.e(TAG, "Error fetching Singapore data: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch from TransitLand API
     */
    private void fetchFromTransitLand(List<BusInfo> buses) {
        try {
            String url = "https://transit.land/api/v2/rest/stops"
                    + "?lat=" + currentUserLocation.latitude
                    + "&lon=" + currentUserLocation.longitude
                    + "&radius=" + (SEARCH_RADIUS_KM * 1000);

            String jsonResponse = makeHttpRequest(url);
            if (jsonResponse != null) {
                parseTransitLandResponse(jsonResponse, buses);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching TransitLand data: " + e.getMessage(), e);
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
     * Parse GTFS response
     */
    private void parseGTFSResponse(String jsonResponse, List<BusInfo> buses) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray vehicles = json.optJSONArray("vehicles");

            if (vehicles != null) {
                for (int i = 0; i < vehicles.length(); i++) {
                    JSONObject vehicle = vehicles.getJSONObject(i);
                    BusInfo bus = parseBusFromVehicle(vehicle);
                    if (bus != null) {
                        buses.add(bus);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing GTFS response: " + e.getMessage(), e);
        }
    }

    /**
     * Parse TransitLand response
     */
    private void parseTransitLandResponse(String jsonResponse, List<BusInfo> buses) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            JSONArray stops = json.optJSONArray("stops");

            if (stops != null) {
                for (int i = 0; i < stops.length(); i++) {
                    JSONObject stop = stops.getJSONObject(i);
                    BusInfo bus = parseBusFromStop(stop);
                    if (bus != null) {
                        buses.add(bus);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing TransitLand response: " + e.getMessage(), e);
        }
    }

    /**
     * Parse bus info from vehicle data
     */
    private BusInfo parseBusFromVehicle(JSONObject vehicle) {
        try {
            JSONObject position = vehicle.optJSONObject("position");
            if (position != null) {
                double lat = position.optDouble("latitude");
                double lon = position.optDouble("longitude");
                String routeId = vehicle.optString("route_id", "Unknown");
                String vehicleId = vehicle.optString("vehicle_id", "");

                return new BusInfo(routeId, vehicleId, lat, lon, "Live", "Real-time");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing vehicle: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Parse bus info from stop data
     */
    private BusInfo parseBusFromStop(JSONObject stop) {
        try {
            JSONObject geometry = stop.optJSONObject("geometry");
            if (geometry != null) {
                JSONArray coordinates = geometry.optJSONArray("coordinates");
                if (coordinates != null && coordinates.length() >= 2) {
                    double lon = coordinates.getDouble(0);
                    double lat = coordinates.getDouble(1);
                    String stopName = stop.optString("stop_name", "Bus Stop");

                    return new BusInfo("Bus Stop", stopName, lat, lon, "Stop", "Active");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing stop: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Update bus markers on map
     */
    private void updateBusMarkersOnMap() {
        if (mMap == null) return;

        // Clear existing bus markers
        for (Marker marker : busMarkers) {
            marker.remove();
        }
        busMarkers.clear();

        // Add new bus markers
        for (BusInfo bus : nearbyBuses) {
            LatLng position = new LatLng(bus.latitude, bus.longitude);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(bus.routeNumber)
                    .snippet(bus.destination + " • " + bus.status);

            // Different colors for different types
            if ("Bus Stop".equals(bus.routeNumber)) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }

            Marker marker = mMap.addMarker(markerOptions);
            busMarkers.add(marker);
        }
    }

    /**
     * Refresh bus data manually
     */
    private void refreshBusData() {
        if (currentUserLocation != null) {
            fetchNearbyBuses();
            Toast.makeText(this, "Refreshing bus data...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show/hide progress indicator
     */
    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Handle permission request results
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
                Toast.makeText(this, "Location permission required for live tracking", Toast.LENGTH_LONG).show();
                finish();
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
        if (isTrackingActive && currentUserLocation != null) {
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
        Log.d(TAG, "Enhanced LiveBusTrackingActivity destroyed");
    }

    /**
     * Bus information data class
     */
    private static class BusInfo {
        String routeNumber;
        String destination;
        double latitude;
        double longitude;
        String status;
        String eta;

        BusInfo(String routeNumber, String destination, double latitude, double longitude, String status, String eta) {
            this.routeNumber = routeNumber;
            this.destination = destination;
            this.latitude = latitude;
            this.longitude = longitude;
            this.status = status;
            this.eta = eta;
        }
    }
}