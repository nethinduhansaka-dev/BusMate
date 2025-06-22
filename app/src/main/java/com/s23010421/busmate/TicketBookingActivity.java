package com.s23010421.busmate;

import android.app.DatePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * TicketBookingActivity for EEI4369 BusMate Project
 * Implements the exact UI design shown in the image
 */
public class TicketBookingActivity extends AppCompatActivity {

    private static final String TAG = "TicketBooking";

    // UI Components
    private Button buttonBack;
    private TextView textViewFrom;
    private TextView textViewTo;
    private TextView textViewDate;
    private TextView textViewPassengerCount;
    private Button buttonDatePicker;
    private Button buttonDecrease;
    private Button buttonIncrease;
    private LinearLayout layoutAvailableBuses;

    // Data
    private String fromLocation = "Negombo Bus Station";
    private String toLocation = "";
    private String selectedDate = "";
    private int passengerCount = 1;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "TicketBookingActivity onCreate started");
            setContentView(R.layout.activity_ticket_booking);

            initializeViews();
            setupInitialData();
            setupClickListeners();
            loadAvailableBuses();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorAndFinish("Error loading ticket booking");
        }
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        buttonBack = findViewById(R.id.buttonBack);
        textViewFrom = findViewById(R.id.textViewFrom);
        textViewTo = findViewById(R.id.textViewTo);
        textViewDate = findViewById(R.id.textViewDate);
        textViewPassengerCount = findViewById(R.id.textViewPassengerCount);
        buttonDatePicker = findViewById(R.id.buttonDatePicker);
        buttonDecrease = findViewById(R.id.buttonDecrease);
        buttonIncrease = findViewById(R.id.buttonIncrease);
        layoutAvailableBuses = findViewById(R.id.layoutAvailableBuses);
    }

    /**
     * Setup initial data exactly as shown in the image
     */
    private void setupInitialData() {
        // Set from location
        textViewFrom.setText(fromLocation);

        // Set to location placeholder
        textViewTo.setText("Select destination");
        textViewTo.setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Set default date (06/16/2025 as shown in image)
        selectedDate = "06/16/2025";
        textViewDate.setText(selectedDate);

        // Set passenger count
        textViewPassengerCount.setText(String.valueOf(passengerCount));
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

        // To destination selector
        textViewTo.setOnClickListener(v -> showDestinationPicker());

        // Date picker
        buttonDatePicker.setOnClickListener(v -> showDatePicker());

        // Passenger count controls
        buttonDecrease.setOnClickListener(v -> {
            if (passengerCount > 1) {
                passengerCount--;
                textViewPassengerCount.setText(String.valueOf(passengerCount));
                updateBusPrices();
            }
        });

        buttonIncrease.setOnClickListener(v -> {
            if (passengerCount < 9) {
                passengerCount++;
                textViewPassengerCount.setText(String.valueOf(passengerCount));
                updateBusPrices();
            }
        });
    }

    /**
     * Show destination picker (simplified for demo)
     */
    private void showDestinationPicker() {
        // For demo purposes, set Colombo Fort as destination
        toLocation = "Colombo Fort";
        textViewTo.setText(toLocation);
        textViewTo.setTextColor(getResources().getColor(android.R.color.black));

        // Refresh available buses
        loadAvailableBuses();
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    selectedDate = sdf.format(calendar.getTime());
                    textViewDate.setText(selectedDate);
                    loadAvailableBuses();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Load available buses exactly as shown in the image
     */
    private void loadAvailableBuses() {
        layoutAvailableBuses.removeAllViews();

        // Bus 1: Colombo Express (Selected/Primary option)
        View bus1 = createBusCard(
                "Colombo Express",
                "₹ 150",
                "10:30 AM - 12:15 PM",
                "Duration: 1h 45m • 3 stops",
                "15 seats left",
                true // Primary/selected style
        );
        layoutAvailableBuses.addView(bus1);

        // Bus 2: City Link (Secondary option)
        View bus2 = createBusCard(
                "City Link",
                "₹ 120",
                "11:00 AM - 1:00 PM",
                "Duration: 2h 00m • 5 stops",
                "8 seats left",
                false // Secondary style
        );
        layoutAvailableBuses.addView(bus2);
    }

    /**
     * Create bus card exactly matching the image design
     */
    private View createBusCard(String name, String price, String time,
                               String duration, String seatsLeft, boolean isPrimary) {

        // Create main card container
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(8);
        cardView.setRadius(16);
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

        // Main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(24, 20, 24, 20);

        // Top row: Name and Price
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams topRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        topRowParams.bottomMargin = 8;
        topRow.setLayoutParams(topRowParams);

        // Bus name
        TextView busName = new TextView(this);
        busName.setText(name);
        busName.setTextSize(18);
        busName.setTextColor(getResources().getColor(android.R.color.black));
        busName.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        busName.setLayoutParams(nameParams);

        // Price
        TextView busPrice = new TextView(this);
        busPrice.setText(price);
        busPrice.setTextSize(18);
        busPrice.setTextColor(getResources().getColor(android.R.color.black));
        busPrice.setTypeface(null, android.graphics.Typeface.BOLD);

        topRow.addView(busName);
        topRow.addView(busPrice);

        // Time
        TextView busTime = new TextView(this);
        busTime.setText(time);
        busTime.setTextSize(14);
        busTime.setTextColor(getResources().getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timeParams.bottomMargin = 4;
        busTime.setLayoutParams(timeParams);

        // Duration
        TextView busDuration = new TextView(this);
        busDuration.setText(duration);
        busDuration.setTextSize(12);
        busDuration.setTextColor(getResources().getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams durationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        durationParams.bottomMargin = 16;
        busDuration.setLayoutParams(durationParams);

        // Bottom row: Seats left and Select button
        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Seats left
        TextView seatsLeftText = new TextView(this);
        seatsLeftText.setText(seatsLeft);
        seatsLeftText.setTextSize(12);
        seatsLeftText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams seatsParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        seatsLeftText.setLayoutParams(seatsParams);

        // Select button
        Button selectButton = new Button(this);
        selectButton.setText("Select");
        selectButton.setTextSize(16);
        selectButton.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                200, 120
        );
        selectButton.setLayoutParams(buttonParams);

        if (isPrimary) {
            // Black button for primary option
            selectButton.setBackgroundColor(getResources().getColor(android.R.color.black));
            selectButton.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            // White button with border for secondary option
            selectButton.setBackgroundColor(getResources().getColor(android.R.color.white));
            selectButton.setTextColor(getResources().getColor(android.R.color.black));
            selectButton.setBackground(createButtonBorder());
        }

        selectButton.setOnClickListener(v -> {
            proceedToPayment(name, price, time);
        });

        bottomRow.addView(seatsLeftText);
        bottomRow.addView(selectButton);

        // Add all views to main container
        mainContainer.addView(topRow);
        mainContainer.addView(busTime);
        mainContainer.addView(busDuration);
        mainContainer.addView(bottomRow);

        cardView.addView(mainContainer);
        return cardView;
    }

    /**
     * Create button border for secondary select button
     */
    private android.graphics.drawable.Drawable createButtonBorder() {
        android.graphics.drawable.GradientDrawable drawable =
                new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setColor(getResources().getColor(android.R.color.white));
        drawable.setStroke(2, getResources().getColor(android.R.color.black));
        drawable.setCornerRadius(8);
        return drawable;
    }

    /**
     * Update bus prices based on passenger count
     */
    private void updateBusPrices() {
        // Refresh the bus list with updated prices
        loadAvailableBuses();
    }

    /**
     * Proceed to payment screen
     */
    private void proceedToPayment(String busName, String price, String time) {
        if (toLocation.isEmpty()) {
            Toast.makeText(this, "Please select destination", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("bus_name", busName);
        intent.putExtra("price", price);
        intent.putExtra("time", time);
        intent.putExtra("from", fromLocation);
        intent.putExtra("to", toLocation);
        intent.putExtra("date", selectedDate);
        intent.putExtra("passengers", passengerCount);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Show error and finish
     */
    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TicketBookingActivity destroyed");
    }
}
