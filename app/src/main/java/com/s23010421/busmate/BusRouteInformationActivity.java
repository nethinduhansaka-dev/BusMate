package com.s23010421.busmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * BusRouteInformationActivity - Comprehensive route information display
 * Shows complete route maps, stop details, schedules, and capacity monitoring
 */
public class BusRouteInformationActivity extends AppCompatActivity implements OnMapReadyCallback {

    // UI Components
    private TextView textViewRouteNumber;
    private TextView textViewRouteDescription;
    private TextView textViewServiceFrequency;
    private TextView textViewOperatingHours;
    private TextView textViewTotalDistance;
    private TextView textViewEstimatedTime;
    private Button buttonStartTracking;
    private Button buttonSetReminder;
    private Button buttonFavoriteRoute;
    private CardView cardViewRouteOverview;
    private CardView cardViewCapacityMonitor;
    private RecyclerView recyclerViewStops;
    private LinearLayout layoutScheduleDisplay;

    // Map
    private GoogleMap mMap;

    // Route data
    private String routeNumber;
    private List<BusStop> busStops;
    private BusStopAdapter stopAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route_information);

        // Get route number from intent
        routeNumber = getIntent().getStringExtra("route_number");
        if (routeNumber == null) {
            routeNumber = "245";
        }

        initializeViews();
        setupClickListeners();
        loadRouteData();
        setupRecyclerView();

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragmentRoute);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initializeViews() {
        textViewRouteNumber = findViewById(R.id.textViewRouteNumber);
        textViewRouteDescription = findViewById(R.id.textViewRouteDescription);
        textViewServiceFrequency = findViewById(R.id.textViewServiceFrequency);
        textViewOperatingHours = findViewById(R.id.textViewOperatingHours);
        textViewTotalDistance = findViewById(R.id.textViewTotalDistance);
        textViewEstimatedTime = findViewById(R.id.textViewEstimatedTime);
        buttonStartTracking = findViewById(R.id.buttonStartTracking);
        buttonSetReminder = findViewById(R.id.buttonSetReminder);
        buttonFavoriteRoute = findViewById(R.id.buttonFavoriteRoute);
        cardViewRouteOverview = findViewById(R.id.cardViewRouteOverview);
        cardViewCapacityMonitor = findViewById(R.id.cardViewCapacityMonitor);
        recyclerViewStops = findViewById(R.id.recyclerViewStops);
        layoutScheduleDisplay = findViewById(R.id.layoutScheduleDisplay);
    }

    private void setupClickListeners() {
        buttonStartTracking.setOnClickListener(v -> startLiveTracking());
        buttonSetReminder.setOnClickListener(v -> setRouteReminder());
        buttonFavoriteRoute.setOnClickListener(v -> toggleFavoriteRoute());
        cardViewCapacityMonitor.setOnClickListener(v -> showCapacityDetails());
    }

    private void loadRouteData() {
        // Load route information based on route number
        textViewRouteNumber.setText("Route " + routeNumber);

        switch (routeNumber) {
            case "245":
                textViewRouteDescription.setText("Negombo ↔ Colombo Fort Express");
                textViewServiceFrequency.setText("Every 15 minutes");
                textViewOperatingHours.setText("5:00 AM - 11:00 PM");
                textViewTotalDistance.setText("42.5 km");
                textViewEstimatedTime.setText("1h 45min");
                loadRoute245Stops();
                break;
            case "187":
                textViewRouteDescription.setText("Airport ↔ Colombo City Link");
                textViewServiceFrequency.setText("Every 20 minutes");
                textViewOperatingHours.setText("4:00 AM - 12:00 AM");
                textViewTotalDistance.setText("35.2 km");
                textViewEstimatedTime.setText("1h 15min");
                loadRoute187Stops();
                break;
            default:
                loadDefaultRouteData();
                break;
        }

        loadScheduleData();
    }

    private void loadRoute245Stops() {
        busStops = new ArrayList<>();
        busStops.add(new BusStop("1", "Negombo Bus Station", "Main terminal",
                "6:00 AM", true, new LatLng(6.9271, 79.8612)));
        busStops.add(new BusStop("2", "Ja-Ela Junction", "Near railway station",
                "6:15 AM", true, new LatLng(6.9541, 79.8648)));
        busStops.add(new BusStop("3", "Wattala Bus Stand", "Commercial area",
                "6:32 AM", false, new LatLng(7.0123, 79.8789)));
        busStops.add(new BusStop("4", "Kelaniya Temple", "Historical site",
                "6:45 AM", true, new LatLng(7.0873, 79.8956)));
        busStops.add(new BusStop("5", "Peliyagoda", "Industrial zone",
                "7:02 AM", false, new LatLng(7.1564, 79.9123)));
        busStops.add(new BusStop("6", "Dematagoda Railway", "Railway connection",
                "7:18 AM", true, new LatLng(7.2456, 79.9234)));
        busStops.add(new BusStop("7", "Colombo Fort", "Final destination",
                "7:35 AM", true, new LatLng(7.2906, 79.9085)));
    }

    private void loadRoute187Stops() {
        busStops = new ArrayList<>();
        busStops.add(new BusStop("1", "Airport Terminal", "International terminal",
                "5:00 AM", true, new LatLng(7.1808, 79.8842)));
        busStops.add(new BusStop("2", "Katunayake Town", "Town center",
                "5:15 AM", false, new LatLng(7.1691, 79.8890)));
        busStops.add(new BusStop("3", "Seeduwa Junction", "Main road junction",
                "5:28 AM", true, new LatLng(7.1234, 79.8956)));
        busStops.add(new BusStop("4", "Negombo Road", "Highway access",
                "5:45 AM", false, new LatLng(7.0875, 79.9012)));
        busStops.add(new BusStop("5", "Colombo City", "Business district",
                "6:15 AM", true, new LatLng(7.2906, 79.9085)));
    }

    private void loadDefaultRouteData() {
        textViewRouteDescription.setText("Route Information");
        textViewServiceFrequency.setText("Check schedule");
        textViewOperatingHours.setText("Operating hours vary");
        textViewTotalDistance.setText("Distance varies");
        textViewEstimatedTime.setText("Time varies");
        busStops = new ArrayList<>();
    }

    private void setupRecyclerView() {
        stopAdapter = new BusStopAdapter(busStops, this::onStopSelected);
        recyclerViewStops.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStops.setAdapter(stopAdapter);
    }

    private void loadScheduleData() {
        layoutScheduleDisplay.removeAllViews();

        // Sample schedule data
        String[] timeSlots = {"6:00 AM", "6:15 AM", "6:30 AM", "6:45 AM", "7:00 AM", "7:15 AM"};
        String[] delays = {"On time", "2 min late", "On time", "5 min late", "On time", "1 min late"};

        for (int i = 0; i < timeSlots.length; i++) {
            View scheduleItem = getLayoutInflater().inflate(R.layout.item_schedule, null);

            TextView timeText = scheduleItem.findViewById(R.id.textViewScheduleTime);
            TextView statusText = scheduleItem.findViewById(R.id.textViewScheduleStatus);

            timeText.setText(timeSlots[i]);
            statusText.setText(delays[i]);

            // Set status color
            if (delays[i].contains("late")) {
                statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }

            layoutScheduleDisplay.addView(scheduleItem);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Add route path and stops
        addRouteToMap();

        // Focus on route
        focusOnRoute();
    }

    private void addRouteToMap() {
        if (busStops.isEmpty()) return;

        // Add route path
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(getResources().getColor(R.color.route_path_color))
                .width(8f);

        // Add stops to map and polyline
        for (BusStop stop : busStops) {
            // Add stop marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(stop.getLocation())
                    .title(stop.getName())
                    .snippet(stop.getDescription())
                    .icon(BitmapDescriptorFactory.fromResource(
                            stop.isAccessible() ? R.drawable.ic_bus_stop_accessible :
                                    R.drawable.ic_bus_stop_marker));

            mMap.addMarker(markerOptions);

            // Add to polyline
            polylineOptions.add(stop.getLocation());
        }

        // Add polyline to map
        mMap.addPolyline(polylineOptions);
    }

    private void focusOnRoute() {
        if (!busStops.isEmpty()) {
            // Focus on middle of route
            int middleIndex = busStops.size() / 2;
            LatLng centerPoint = busStops.get(middleIndex).getLocation();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 11f));
        }
    }

    private void onStopSelected(BusStop stop) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stop.getLocation(), 15f));
        }

        Toast.makeText(this, "Selected: " + stop.getName(), Toast.LENGTH_SHORT).show();
    }

    private void startLiveTracking() {
        Intent intent = new Intent(this, LiveBusTrackingActivity.class);
        intent.putExtra("route_number", routeNumber);
        startActivity(intent);
    }

    private void setRouteReminder() {
        Toast.makeText(this, "Route reminder set for " + routeNumber, Toast.LENGTH_SHORT).show();
        // TODO: Implement reminder functionality
    }

    private void toggleFavoriteRoute() {
        Toast.makeText(this, "Route " + routeNumber + " added to favorites", Toast.LENGTH_SHORT).show();
        // TODO: Implement favorite route functionality
    }

    private void showCapacityDetails() {
        Intent intent = new Intent(this, CapacityMonitorActivity.class);
        intent.putExtra("route_number", routeNumber);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    // Inner class for bus stop data
    public static class BusStop {
        private String id;
        private String name;
        private String description;
        private String nextArrival;
        private boolean accessible;
        private LatLng location;

        public BusStop(String id, String name, String description, String nextArrival,
                       boolean accessible, LatLng location) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.nextArrival = nextArrival;
            this.accessible = accessible;
            this.location = location;
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getNextArrival() { return nextArrival; }
        public boolean isAccessible() { return accessible; }
        public LatLng getLocation() { return location; }
    }
}
