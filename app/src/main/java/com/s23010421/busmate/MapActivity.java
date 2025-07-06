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
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern MapActivity with full-screen map, floating controls, and bottom sheet
 * Designed like Uber/Apple Maps interface
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private GoogleMap mMap;
    private SearchView searchView;
    private FloatingActionButton fabMyLocation;
    private FloatingActionButton fabBusStops;
    private FloatingActionButton fabRoutes;

    // Bottom Sheet Components
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheetLayout;

    // Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    // Map Data
    private List<Marker> busStopMarkers;
    private List<Marker> busMarkers;
    private List<Polyline> routePolylines;
    private boolean showBusStops = true;
    private boolean showRoutes = true;

    // Selected Data
    private Marker selectedBusStopMarker;
    private BusStopInfo selectedBusStopInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initializeComponents();
        setupGoogleMap();
        setupLocationServices();
        setupBottomSheet();
        setupFloatingButtons();
        setupSearchView();
        checkLocationPermission();
    }

    /**
     * Initialize all components and data structures
     */
    private void initializeComponents() {
        // Initialize UI components
        searchView = findViewById(R.id.searchView);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        fabBusStops = findViewById(R.id.fabBusStops);
        fabRoutes = findViewById(R.id.fabRoutes);
        bottomSheetLayout = findViewById(R.id.bottomSheetLayout);

        // Initialize data structures
        busStopMarkers = new ArrayList<>();
        busMarkers = new ArrayList<>();
        routePolylines = new ArrayList<>();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    /**
     * Setup Google Map fragment
     */
    private void setupGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Setup location services
     */
    private void setupLocationServices() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(30000)
                .setFastestInterval(15000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    currentLocation = locationResult.getLastLocation();
                }
            }
        };
    }

    /**
     * Setup bottom sheet behavior
     */
    private void setupBottomSheet() {
        if (bottomSheetLayout != null) {
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    // Handle bottom sheet state changes
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // Handle bottom sheet slide animations
                }
            });
        }
    }

    /**
     * Setup floating action buttons
     */
    private void setupFloatingButtons() {
        // My Location FAB
        fabMyLocation.setOnClickListener(v -> centerMapOnUserLocation());

        // Bus Stops Toggle FAB
        fabBusStops.setOnClickListener(v -> toggleBusStopsVisibility());

        // Routes Toggle FAB
        fabRoutes.setOnClickListener(v -> toggleRoutesVisibility());
    }

    /**
     * Setup search view functionality
     */
    private void setupSearchView() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchLocation(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Show search suggestions if needed
                    return false;
                }
            });
        }
    }

    /**
     * Google Maps callback
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Google Map ready");

        // Configure map
        configureMap();

        // Add map data
        addBusStops();
        addBusRoutes();
        addLiveBuses();

        // Setup map listeners
        setupMapListeners();

        // Set initial camera position (Negombo, Sri Lanka)
        LatLng negombo = new LatLng(7.2906, 79.9000);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(negombo, 13.0f));
    }

    /**
     * Configure map settings
     */
    private void configureMap() {
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(false); // We have custom controls
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false); // Using custom FAB
        }
    }

    /**
     * Setup map click listeners
     */
    private void setupMapListeners() {
        if (mMap != null) {
            // Marker click listener
            mMap.setOnMarkerClickListener(marker -> {
                String tag = (String) marker.getTag();
                if ("bus_stop".equals(tag)) {
                    showBusStopBottomSheet(marker);
                    return true;
                } else if ("bus".equals(tag)) {
                    showBusBottomSheet(marker);
                    return true;
                }
                return false;
            });

            // Map click listener to hide bottom sheet
            mMap.setOnMapClickListener(latLng -> {
                hideBottomSheet();
            });
        }
    }

    /**
     * Add bus stops to the map
     */
    private void addBusStops() {
        if (mMap == null) return;

        // Sample bus stops in Negombo area
        BusStopInfo[] busStops = {
                new BusStopInfo("1", "Negombo Bus Station", "Main Street, Negombo",
                        7.2906, 79.9000, new String[]{"245", "187"}),
                new BusStopInfo("2", "Hospital Junction", "Negombo-Colombo Rd",
                        7.2800, 79.9100, new String[]{"245"}),
                new BusStopInfo("3", "Market Square", "Dalukanda Junction",
                        7.2700, 79.9200, new String[]{"187"}),
                new BusStopInfo("4", "Railway Station", "Station Road",
                        7.2600, 79.9300, new String[]{"245", "187"}),
                new BusStopInfo("5", "St. Mary's Church", "Church Street",
                        7.2500, 79.9400, new String[]{"245"})
        };

        for (BusStopInfo busStop : busStops) {
            LatLng position = new LatLng(busStop.latitude, busStop.longitude);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(busStop.name)
                    .snippet(busStop.address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            if (marker != null) {
                marker.setTag("bus_stop");
                busStopMarkers.add(marker);
            }
        }
    }

    /**
     * Add bus routes to the map
     */
    private void addBusRoutes() {
        if (mMap == null) return;

        // Route 245 (Blue)
        List<LatLng> route245 = new ArrayList<>();
        route245.add(new LatLng(7.2906, 79.9000));
        route245.add(new LatLng(7.2800, 79.9100));
        route245.add(new LatLng(7.2600, 79.9300));
        route245.add(new LatLng(7.2500, 79.9400));

        Polyline polyline245 = mMap.addPolyline(new PolylineOptions()
                .addAll(route245)
                .width(8f)
                .color(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
                .geodesic(true));
        routePolylines.add(polyline245);

        // Route 187 (Orange)
        List<LatLng> route187 = new ArrayList<>();
        route187.add(new LatLng(7.2906, 79.9000));
        route187.add(new LatLng(7.2700, 79.9200));
        route187.add(new LatLng(7.2600, 79.9300));

        Polyline polyline187 = mMap.addPolyline(new PolylineOptions()
                .addAll(route187)
                .width(8f)
                .color(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                .geodesic(true));
        routePolylines.add(polyline187);
    }

    /**
     * Add live buses to the map
     */
    private void addLiveBuses() {
        if (mMap == null) return;

        // Bus 245
        LatLng bus245Position = new LatLng(7.2850, 79.9050);
        Marker bus245 = mMap.addMarker(new MarkerOptions()
                .position(bus245Position)
                .title("Bus 245")
                .snippet("To Colombo • 5 min")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        if (bus245 != null) {
            bus245.setTag("bus");
            busMarkers.add(bus245);
        }

        // Bus 187
        LatLng bus187Position = new LatLng(7.2750, 79.9150);
        Marker bus187 = mMap.addMarker(new MarkerOptions()
                .position(bus187Position)
                .title("Bus 187")
                .snippet("To Airport • 12 min")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        if (bus187 != null) {
            bus187.setTag("bus");
            busMarkers.add(bus187);
        }
    }

    /**
     * Show bus stop information in bottom sheet
     */
    private void showBusStopBottomSheet(Marker marker) {
        selectedBusStopMarker = marker;

        // Create bus stop info (in real app, get from database)
        selectedBusStopInfo = new BusStopInfo(
                "1",
                marker.getTitle(),
                marker.getSnippet(),
                marker.getPosition().latitude,
                marker.getPosition().longitude,
                new String[]{"245", "187"}
        );

        // Update bottom sheet content
        updateBottomSheetContent();

        // Show bottom sheet
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    /**
     * Show bus information in bottom sheet
     */
    private void showBusBottomSheet(Marker marker) {
        // Handle bus marker clicks
        String title = marker.getTitle();
        String snippet = marker.getSnippet();

        Toast.makeText(this, title + " - " + snippet, Toast.LENGTH_SHORT).show();

        // You can expand this to show detailed bus info
    }

    /**
     * Update bottom sheet content with selected bus stop info
     */
    private void updateBottomSheetContent() {
        if (selectedBusStopInfo == null) return;

        // Update TextViews in bottom sheet with bus stop information
        // This would reference TextViews in the bottom sheet layout
        // Implementation depends on your bottom sheet layout structure
    }

    /**
     * Hide bottom sheet
     */
    private void hideBottomSheet() {
        if (bottomSheetBehavior != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
        selectedBusStopMarker = null;
        selectedBusStopInfo = null;
    }

    /**
     * Center map on user's current location
     */
    private void centerMapOnUserLocation() {
        if (mMap != null && currentLocation != null) {
            LatLng userLocation = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16.0f));
            Toast.makeText(this, "Centered on your location", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
        }
    }

    /**
     * Toggle bus stops visibility
     */
    private void toggleBusStopsVisibility() {
        showBusStops = !showBusStops;

        for (Marker marker : busStopMarkers) {
            marker.setVisible(showBusStops);
        }

        String message = showBusStops ? "Bus stops shown" : "Bus stops hidden";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Update FAB icon/color to reflect state
        int colorId = showBusStops ? android.R.color.holo_green_dark : android.R.color.darker_gray;
        fabBusStops.setBackgroundTintList(ContextCompat.getColorStateList(this, colorId));
    }

    /**
     * Toggle routes visibility
     */
    private void toggleRoutesVisibility() {
        showRoutes = !showRoutes;

        for (Polyline polyline : routePolylines) {
            polyline.setVisible(showRoutes);
        }

        String message = showRoutes ? "Routes shown" : "Routes hidden";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Update FAB icon/color to reflect state
        int colorId = showRoutes ? android.R.color.holo_blue_dark : android.R.color.darker_gray;
        fabRoutes.setBackgroundTintList(ContextCompat.getColorStateList(this, colorId));
    }

    /**
     * Search for location
     */
    private void searchLocation(String query) {
        // Simple location search implementation
        if (query.toLowerCase().contains("negombo")) {
            LatLng negombo = new LatLng(7.2906, 79.9000);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(negombo, 15.0f));
        } else if (query.toLowerCase().contains("colombo")) {
            LatLng colombo = new LatLng(6.9271, 79.8612);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(colombo, 13.0f));
        } else {
            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
        }

        // Clear search view focus
        searchView.clearFocus();
    }

    /**
     * Check location permission
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableLocationServices();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Enable location services
     */
    private void enableLocationServices() {
        try {
            if (mMap != null && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                startLocationUpdates();
                getCurrentLocation();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e.getMessage());
        }
    }

    /**
     * Start receiving location updates
     */
    private void startLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                );
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception starting location updates: " + e.getMessage());
        }
    }

    /**
     * Get current location once
     */
    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                currentLocation = location;
                            }
                        });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception getting location: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationServices();
            } else {
                Toast.makeText(this, "Location permission required for full functionality",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentLocation != null) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Data class for bus stop information
     */
    private static class BusStopInfo {
        String id;
        String name;
        String address;
        double latitude;
        double longitude;
        String[] routes;

        BusStopInfo(String id, String name, String address, double lat, double lng, String[] routes) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.latitude = lat;
            this.longitude = lng;
            this.routes = routes;
        }
    }
}