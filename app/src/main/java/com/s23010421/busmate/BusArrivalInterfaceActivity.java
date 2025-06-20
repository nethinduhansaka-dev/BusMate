package com.s23010421.busmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BusArrivalInterfaceActivity - Real-time arrival countdown and notifications
 * Provides countdown timers, arrival notifications, and accessibility features
 */
public class BusArrivalInterfaceActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    // UI Components
    private TextView textViewStopName;
    private TextView textViewStopCode;
    private LinearLayout layoutArrivingBuses;
    private RecyclerView recyclerViewAllRoutes;
    private Button buttonRefreshArrivals;
    private Button buttonContactDriver;
    private Button buttonReportIssue;
    private Switch switchAudioAnnouncements;
    private Switch switchHighContrast;
    private CardView cardViewAccessibility;

    // Accessibility
    private TextToSpeech textToSpeech;
    private boolean audioEnabled = false;
    private boolean highContrastMode = false;

    // Arrival data
    private List<BusArrival> arrivingBuses;
    private List<CountDownTimer> countdownTimers;
    private BusArrivalAdapter arrivalAdapter;

    // Stop information
    private String stopName;
    private String stopCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_arrival_interface);

        // Get stop information from intent
        stopName = getIntent().getStringExtra("stop_name");
        stopCode = getIntent().getStringExtra("stop_code");

        if (stopName == null) stopName = "Central Bus Station";
        if (stopCode == null) stopCode = "CBS001";

        initializeViews();
        initializeTextToSpeech();
        setupClickListeners();
        loadArrivalData();
        setupRecyclerView();
        startArrivalUpdates();
    }

    private void initializeViews() {
        textViewStopName = findViewById(R.id.textViewStopName);
        textViewStopCode = findViewById(R.id.textViewStopCode);
        layoutArrivingBuses = findViewById(R.id.layoutArrivingBuses);
        recyclerViewAllRoutes = findViewById(R.id.recyclerViewAllRoutes);
        buttonRefreshArrivals = findViewById(R.id.buttonRefreshArrivals);
        buttonContactDriver = findViewById(R.id.buttonContactDriver);
        buttonReportIssue = findViewById(R.id.buttonReportIssue);
        switchAudioAnnouncements = findViewById(R.id.switchAudioAnnouncements);
        switchHighContrast = findViewById(R.id.switchHighContrast);
        cardViewAccessibility = findViewById(R.id.cardViewAccessibility);

        // Set stop information
        textViewStopName.setText(stopName);
        textViewStopCode.setText("Stop Code: " + stopCode);

        // Initialize lists
        arrivingBuses = new ArrayList<>();
        countdownTimers = new ArrayList<>();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, this);
    }

    private void setupClickListeners() {
        buttonRefreshArrivals.setOnClickListener(v -> refreshArrivalData());

        buttonContactDriver.setOnClickListener(v -> contactSelectedDriver());

        buttonReportIssue.setOnClickListener(v -> reportStopIssue());

        switchAudioAnnouncements.setOnCheckedChangeListener((buttonView, isChecked) -> {
            audioEnabled = isChecked;
            if (isChecked) {
                announceNextArrival();
            }
        });

        switchHighContrast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            highContrastMode = isChecked;
            applyAccessibilitySettings();
        });
    }

    private void loadArrivalData() {
        // Simulated arrival data
        arrivingBuses.clear();

        arrivingBuses.add(new BusArrival("245", "Colombo Fort", "WP-CM-5847",
                300, 78, "On Schedule"));
        arrivingBuses.add(new BusArrival("187", "Airport", "WP-BM-2341",
                720, 45, "2 min delay"));
        arrivingBuses.add(new BusArrival("245", "Negombo", "WP-KB-8765",
                900, 92, "On Schedule"));
        arrivingBuses.add(new BusArrival("138", "Kandy", "WP-CD-1234",
                1380, 56, "5 min delay"));

        updateArrivalDisplay();
    }

    private void updateArrivalDisplay() {
        layoutArrivingBuses.removeAllViews();

        for (int i = 0; i < Math.min(3, arrivingBuses.size()); i++) {
            BusArrival arrival = arrivingBuses.get(i);
            View arrivalView = createArrivalView(arrival, i);
            layoutArrivingBuses.addView(arrivalView);
        }
    }

    private View createArrivalView(BusArrival arrival, int position) {
        View view = getLayoutInflater().inflate(R.layout.item_bus_arrival, null);

        TextView routeNumber = view.findViewById(R.id.textViewRouteNumber);
        TextView destination = view.findViewById(R.id.textViewDestination);
        TextView busNumber = view.findViewById(R.id.textViewBusNumber);
        TextView countdown = view.findViewById(R.id.textViewCountdown);
        TextView capacity = view.findViewById(R.id.textViewCapacity);
        TextView status = view.findViewById(R.id.textViewStatus);
        ImageView statusIcon = view.findViewById(R.id.imageViewStatusIcon);
        Button trackButton = view.findViewById(R.id.buttonTrackBus);

        routeNumber.setText("Route " + arrival.getRouteNumber());
        destination.setText(arrival.getDestination());
        busNumber.setText(arrival.getBusNumber());
        capacity.setText(arrival.getCapacity() + "% Full");
        status.setText(arrival.getStatus());

        // Set capacity color
        if (arrival.getCapacity() > 80) {
            capacity.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else if (arrival.getCapacity() > 60) {
            capacity.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            capacity.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // Set status icon
        if (arrival.getStatus().contains("delay")) {
            statusIcon.setImageResource(R.drawable.ic_warning);
        } else {
            statusIcon.setImageResource(R.drawable.ic_check_circle);
        }

        // Track button
        trackButton.setOnClickListener(v -> trackBus(arrival));

        // Start countdown timer
        startCountdownTimer(countdown, arrival.getArrivalTimeSeconds(), position);

        return view;
    }

    private void startCountdownTimer(TextView countdownView, int totalSeconds, int position) {
        CountDownTimer timer = new CountDownTimer(totalSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;

                String timeText;
                if (minutes > 0) {
                    timeText = String.format(Locale.getDefault(), "%d:%02d", minutes, remainingSeconds);
                } else {
                    timeText = String.format(Locale.getDefault(), "%d sec", remainingSeconds);
                }

                countdownView.setText(timeText);

                // Announce approaching buses
                if (audioEnabled && seconds == 60 && position == 0) {
                    announceApproachingBus(arrivingBuses.get(position));
                }

                // Change color for imminent arrival
                if (seconds <= 60) {
                    countdownView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (seconds <= 300) {
                    countdownView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                }
            }

            @Override
            public void onFinish() {
                countdownView.setText("Arriving");
                countdownView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                if (audioEnabled && position == 0) {
                    announceArrival(arrivingBuses.get(position));
                }
            }
        };

        timer.start();
        countdownTimers.add(timer);
    }

    private void setupRecyclerView() {
        // Create adapter for all routes
        List<BusArrival> allArrivals = new ArrayList<>(arrivingBuses);

        // Add more routes for comprehensive view
        allArrivals.add(new BusArrival("177", "Gampaha", "WP-EF-9876", 1800, 34, "On Schedule"));
        allArrivals.add(new BusArrival("240", "Chilaw", "WP-GH-5432", 2100, 67, "On Schedule"));

        arrivalAdapter = new BusArrivalAdapter(allArrivals, this::onArrivalSelected);
        recyclerViewAllRoutes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAllRoutes.setAdapter(arrivalAdapter);
    }

    private void startArrivalUpdates() {
        // Refresh arrivals every 30 seconds
        new CountDownTimer(Long.MAX_VALUE, 30000) {
            @Override
            public void onTick(long millisUntilFinished) {
                refreshArrivalData();
            }

            @Override
            public void onFinish() {
                // Won't finish
            }
        }.start();
    }

    private void refreshArrivalData() {
        // Simulate data refresh with slight variations
        for (BusArrival arrival : arrivingBuses) {
            // Decrease arrival time
            int newTime = Math.max(0, arrival.getArrivalTimeSeconds() - 30);
            arrival.setArrivalTimeSeconds(newTime);

            // Update capacity slightly
            int capacityChange = (int) (Math.random() * 10) - 5;
            int newCapacity = Math.max(0, Math.min(100, arrival.getCapacity() + capacityChange));
            arrival.setCapacity(newCapacity);
        }

        updateArrivalDisplay();

        if (arrivalAdapter != null) {
            arrivalAdapter.notifyDataSetChanged();
        }

        Toast.makeText(this, "Arrivals updated", Toast.LENGTH_SHORT).show();
    }

    private void trackBus(BusArrival arrival) {
        Intent intent = new Intent(this, LiveBusTrackingActivity.class);
        intent.putExtra("route_number", arrival.getRouteNumber());
        intent.putExtra("bus_number", arrival.getBusNumber());
        startActivity(intent);
    }

    private void onArrivalSelected(BusArrival arrival) {
        trackBus(arrival);
    }

    private void contactSelectedDriver() {
        if (!arrivingBuses.isEmpty()) {
            BusArrival firstArrival = arrivingBuses.get(0);
            Intent intent = new Intent(this, DriverCommunicationActivity.class);
            intent.putExtra("bus_number", firstArrival.getBusNumber());
            intent.putExtra("route_number", firstArrival.getRouteNumber());
            startActivity(intent);
        }
    }

    private void reportStopIssue() {
        Toast.makeText(this, "Report issue for " + stopName + " - Coming Soon", Toast.LENGTH_SHORT).show();
        // TODO: Implement issue reporting
    }

    private void applyAccessibilitySettings() {
        if (highContrastMode) {
            // Apply high contrast theme
            findViewById(R.id.mainLayout).setBackgroundColor(getResources().getColor(android.R.color.black));
            // Update text colors for high contrast
        } else {
            // Apply normal theme
            findViewById(R.id.mainLayout).setBackgroundColor(getResources().getColor(android.R.color.white));
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Text-to-speech language not supported", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void announceNextArrival() {
        if (textToSpeech != null && !arrivingBuses.isEmpty()) {
            BusArrival nextBus = arrivingBuses.get(0);
            String announcement = String.format("Next bus: Route %s to %s arriving in %d minutes",
                    nextBus.getRouteNumber(), nextBus.getDestination(),
                    nextBus.getArrivalTimeSeconds() / 60);

            textToSpeech.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void announceApproachingBus(BusArrival arrival) {
        if (textToSpeech != null) {
            String announcement = String.format("Bus %s on Route %s is approaching in 1 minute",
                    arrival.getBusNumber(), arrival.getRouteNumber());

            textToSpeech.speak(announcement, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    private void announceArrival(BusArrival arrival) {
        if (textToSpeech != null) {
            String announcement = String.format("Bus %s to %s is now arriving",
                    arrival.getBusNumber(), arrival.getDestination());

            textToSpeech.speak(announcement, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up countdown timers
        for (CountDownTimer timer : countdownTimers) {
            timer.cancel();
        }

        // Clean up text-to-speech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    // Inner class for bus arrival data
    public static class BusArrival {
        private String routeNumber;
        private String destination;
        private String busNumber;
        private int arrivalTimeSeconds;
        private int capacity;
        private String status;

        public BusArrival(String routeNumber, String destination, String busNumber,
                          int arrivalTimeSeconds, int capacity, String status) {
            this.routeNumber = routeNumber;
            this.destination = destination;
            this.busNumber = busNumber;
            this.arrivalTimeSeconds = arrivalTimeSeconds;
            this.capacity = capacity;
            this.status = status;
        }

        // Getters and setters
        public String getRouteNumber() { return routeNumber; }
        public String getDestination() { return destination; }
        public String getBusNumber() { return busNumber; }
        public int getArrivalTimeSeconds() { return arrivalTimeSeconds; }
        public void setArrivalTimeSeconds(int arrivalTimeSeconds) { this.arrivalTimeSeconds = arrivalTimeSeconds; }
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        public String getStatus() { return status; }
    }
}
