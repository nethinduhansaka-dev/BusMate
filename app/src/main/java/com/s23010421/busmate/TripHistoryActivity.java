package com.s23010421.busmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

/**
 * TripHistoryActivity for EEI4369 BusMate Project
 * Implements the exact UI design shown in the image
 */
public class TripHistoryActivity extends AppCompatActivity {

    private static final String TAG = "TripHistory";

    // UI Components
    private Button buttonBack;
    private Button buttonAllTrips;
    private Button buttonThisMonth;
    private Button buttonLast30Days;
    private TextView textViewTotalTrips;
    private TextView textViewTotalSpent;
    private LinearLayout layoutTripHistory;

    // Current filter
    private String currentFilter = "All Trips";

    // Sample trip data
    private List<TripRecord> allTrips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "TripHistoryActivity onCreate started");
            setContentView(R.layout.activity_trip_history);

            initializeViews();
            initializeTripData();
            setupClickListeners();
            updateTripDisplay();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorAndFinish("Error loading trip history");
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        buttonBack = findViewById(R.id.buttonBack);
        buttonAllTrips = findViewById(R.id.buttonAllTrips);
        buttonThisMonth = findViewById(R.id.buttonThisMonth);
        buttonLast30Days = findViewById(R.id.buttonLast30Days);
        textViewTotalTrips = findViewById(R.id.textViewTotalTrips);
        textViewTotalSpent = findViewById(R.id.textViewTotalSpent);
        layoutTripHistory = findViewById(R.id.layoutTripHistory);
    }

    /**
     * Initialize sample trip data exactly as shown in the image
     */
    private void initializeTripData() {
        allTrips = new ArrayList<>();

        // JUNE 2025 trips
        allTrips.add(new TripRecord(
                "Downtown → Airport", "Today, 2:30 PM", "45 min", "$12.50",
                "Route 42A", "Card", 5.0f, "JUNE 2025"
        ));
        allTrips.add(new TripRecord(
                "Home → Office", "Yesterday, 8:15 AM", "32 min", "$8.75",
                "Route 15B", "Cash", 4.0f, "JUNE 2025"
        ));
        allTrips.add(new TripRecord(
                "Mall → Home", "Jun 16, 6:45 PM", "28 min", "$6.25",
                "Route 23", "Mobile", 5.0f, "JUNE 2025"
        ));

        // MAY 2025 trips
        allTrips.add(new TripRecord(
                "Airport → Downtown", "May 28, 3:20 PM", "38 min", "$11.00",
                "Route 42A", "Card", 3.0f, "MAY 2025"
        ));
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        // Back button
        buttonBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Filter buttons
        buttonAllTrips.setOnClickListener(v -> {
            currentFilter = "All Trips";
            updateFilterButtons();
            updateTripDisplay();
        });

        buttonThisMonth.setOnClickListener(v -> {
            currentFilter = "This Month";
            updateFilterButtons();
            updateTripDisplay();
        });

        buttonLast30Days.setOnClickListener(v -> {
            currentFilter = "Last 30 Days";
            updateFilterButtons();
            updateTripDisplay();
        });

        // Set initial filter
        updateFilterButtons();
    }

    /**
     * Update filter button states
     */
    private void updateFilterButtons() {
        // Reset all buttons
        resetFilterButton(buttonAllTrips);
        resetFilterButton(buttonThisMonth);
        resetFilterButton(buttonLast30Days);

        // Set active button
        Button activeButton;
        switch (currentFilter) {
            case "This Month":
                activeButton = buttonThisMonth;
                break;
            case "Last 30 Days":
                activeButton = buttonLast30Days;
                break;
            default:
                activeButton = buttonAllTrips;
                break;
        }

        // Highlight active button
        activeButton.setBackgroundColor(getResources().getColor(android.R.color.black));
        activeButton.setTextColor(getResources().getColor(android.R.color.white));
    }

    /**
     * Reset filter button to default state
     */
    private void resetFilterButton(Button button) {
        button.setBackgroundColor(getResources().getColor(android.R.color.white));
        button.setTextColor(getResources().getColor(android.R.color.black));
    }

    /**
     * Update trip display based on current filter
     */
    private void updateTripDisplay() {
        layoutTripHistory.removeAllViews();

        // Update statistics (always show totals as in image)
        textViewTotalTrips.setText("47");
        textViewTotalSpent.setText("$284.50");

        // Filter trips based on selection
        List<TripRecord> filteredTrips = getFilteredTrips();

        // Group trips by month
        String currentMonth = "";
        for (TripRecord trip : filteredTrips) {
            // Add month header if changed
            if (!trip.month.equals(currentMonth)) {
                currentMonth = trip.month;
                addMonthHeader(currentMonth);
            }

            // Add trip card
            View tripCard = createTripCard(trip);
            layoutTripHistory.addView(tripCard);
        }
    }

    /**
     * Get filtered trips based on current filter
     */
    private List<TripRecord> getFilteredTrips() {
        // For demo purposes, return all trips regardless of filter
        // In real implementation, filter based on actual dates
        return allTrips;
    }

    /**
     * Add month header exactly as shown in image
     */
    private void addMonthHeader(String month) {
        TextView monthHeader = new TextView(this);
        monthHeader.setText(month);
        monthHeader.setTextSize(14);
        monthHeader.setTextColor(getResources().getColor(android.R.color.darker_gray));
        monthHeader.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 32;
        params.bottomMargin = 16;
        params.leftMargin = 16;
        monthHeader.setLayoutParams(params);

        layoutTripHistory.addView(monthHeader);
    }

    /**
     * Create trip card exactly matching the image design
     */
    private View createTripCard(TripRecord trip) {
        // Create main card container
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 8, 16, 8);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(4);
        cardView.setRadius(12);
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

        // Main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(20, 16, 20, 16);

        // Top row: Route and Price
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Route
        TextView routeText = new TextView(this);
        routeText.setText(trip.route);
        routeText.setTextSize(16);
        routeText.setTextColor(getResources().getColor(android.R.color.black));
        routeText.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams routeParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        routeText.setLayoutParams(routeParams);

        // Price
        TextView priceText = new TextView(this);
        priceText.setText(trip.price);
        priceText.setTextSize(16);
        priceText.setTextColor(getResources().getColor(android.R.color.black));
        priceText.setTypeface(null, android.graphics.Typeface.BOLD);

        topRow.addView(routeText);
        topRow.addView(priceText);

        // DateTime and Duration row
        LinearLayout detailsRow = new LinearLayout(this);
        detailsRow.setOrientation(LinearLayout.HORIZONTAL);
        detailsRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        detailsParams.topMargin = 4;
        detailsParams.bottomMargin = 4;
        detailsRow.setLayoutParams(detailsParams);

        // DateTime
        TextView dateTimeText = new TextView(this);
        dateTimeText.setText(trip.dateTime);
        dateTimeText.setTextSize(14);
        dateTimeText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams dateTimeParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        dateTimeText.setLayoutParams(dateTimeParams);

        // Duration
        TextView durationText = new TextView(this);
        durationText.setText(trip.duration);
        durationText.setTextSize(14);
        durationText.setTextColor(getResources().getColor(android.R.color.darker_gray));

        detailsRow.addView(dateTimeText);
        detailsRow.addView(durationText);

        // Payment method and rating row
        LinearLayout paymentRow = new LinearLayout(this);
        paymentRow.setOrientation(LinearLayout.HORIZONTAL);
        paymentRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams paymentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        paymentParams.topMargin = 4;
        paymentParams.bottomMargin = 12;
        paymentRow.setLayoutParams(paymentParams);

        // Route and payment method
        TextView routePaymentText = new TextView(this);
        routePaymentText.setText(trip.routeNumber + " - " + trip.paymentMethod);
        routePaymentText.setTextSize(12);
        routePaymentText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams routePaymentParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        routePaymentText.setLayoutParams(routePaymentParams);

        // Rating
        TextView ratingText = new TextView(this);
        ratingText.setText(String.valueOf(trip.rating));
        ratingText.setTextSize(14);
        ratingText.setTextColor(getResources().getColor(android.R.color.black));
        ratingText.setTypeface(null, android.graphics.Typeface.BOLD);

        paymentRow.addView(routePaymentText);
        paymentRow.addView(ratingText);

        // Action buttons row
        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(android.view.Gravity.END);

        // Receipt button
        Button receiptButton = new Button(this);
        receiptButton.setText("Receipt");
        receiptButton.setTextSize(12);
        receiptButton.setTextColor(getResources().getColor(android.R.color.black));
        receiptButton.setBackground(createButtonBorder());
        LinearLayout.LayoutParams receiptParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 80
        );
        receiptParams.rightMargin = 12;
        receiptButton.setLayoutParams(receiptParams);
        receiptButton.setOnClickListener(v -> showReceipt(trip));

        // Repeat button
        Button repeatButton = new Button(this);
        repeatButton.setText("Repeat");
        repeatButton.setTextSize(12);
        repeatButton.setTextColor(getResources().getColor(android.R.color.black));
        repeatButton.setBackground(createButtonBorder());
        LinearLayout.LayoutParams repeatParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, 80
        );
        repeatButton.setLayoutParams(repeatParams);
        repeatButton.setOnClickListener(v -> repeatTrip(trip));

        buttonRow.addView(receiptButton);
        buttonRow.addView(repeatButton);

        // Add all views to main container
        mainContainer.addView(topRow);
        mainContainer.addView(detailsRow);
        mainContainer.addView(paymentRow);
        mainContainer.addView(buttonRow);

        cardView.addView(mainContainer);
        return cardView;
    }

    /**
     * Create button border for action buttons
     */
    private android.graphics.drawable.Drawable createButtonBorder() {
        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setColor(getResources().getColor(android.R.color.white));
        drawable.setStroke(2, getResources().getColor(android.R.color.darker_gray));
        drawable.setCornerRadius(6);
        return drawable;
    }

    /**
     * Show receipt for selected trip
     */
    private void showReceipt(TripRecord trip) {
        Intent intent = new Intent(this, ReceiptActivity.class);
        intent.putExtra("trip_route", trip.route);
        intent.putExtra("trip_price", trip.price);
        intent.putExtra("trip_date", trip.dateTime);
        intent.putExtra("trip_duration", trip.duration);
        intent.putExtra("route_number", trip.routeNumber);
        intent.putExtra("payment_method", trip.paymentMethod);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Repeat selected trip
     */
    private void repeatTrip(TripRecord trip) {
        Intent intent = new Intent(this, TicketBookingActivity.class);

        // Extract from and to from route
        String[] routeParts = trip.route.split(" → ");
        if (routeParts.length == 2) {
            intent.putExtra("from_location", routeParts[0]);
            intent.putExtra("to_location", routeParts[1]);
        }

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        Toast.makeText(this, "Booking trip: " + trip.route, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show error and finish
     */
    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Trip record data class
     */
    private static class TripRecord {
        String route;
        String dateTime;
        String duration;
        String price;
        String routeNumber;
        String paymentMethod;
        float rating;
        String month;

        TripRecord(String route, String dateTime, String duration, String price,
                   String routeNumber, String paymentMethod, float rating, String month) {
            this.route = route;
            this.dateTime = dateTime;
            this.duration = duration;
            this.price = price;
            this.routeNumber = routeNumber;
            this.paymentMethod = paymentMethod;
            this.rating = rating;
            this.month = month;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TripHistoryActivity destroyed");
    }
}
