package com.s23010421.busmate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * PaymentActivity for EEI4369 BusMate Project
 * Implements secure payment processing with light theme design
 */
public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    // UI Components
    private Button buttonBack;
    private TextView textViewBusName;
    private TextView textViewRoute;
    private TextView textViewDate;
    private TextView textViewTime;
    private TextView textViewBaseFare;
    private TextView textViewServiceFee;
    private TextView textViewTaxes;
    private TextView textViewTotal;

    // Payment Method Selection
    private RadioGroup radioGroupPaymentMethod;
    private RadioButton radioCard;
    private RadioButton radioDigitalWallet;

    // Card Details
    private CardView cardViewCardDetails;
    private EditText editTextCardNumber;
    private EditText editTextExpiry;
    private EditText editTextCVV;
    private EditText editTextCardholderName;

    // Action Buttons
    private Button buttonPay;

    // Booking Data
    private String busName;
    private String price;
    private String time;
    private String fromLocation;
    private String toLocation;
    private String selectedDate;
    private int passengerCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "PaymentActivity onCreate started");
            setContentView(R.layout.activity_payment);

            // Get booking data from intent
            getBookingDataFromIntent();

            // Initialize views
            initializeViews();

            // Setup booking summary
            setupBookingSummary();

            // Setup click listeners
            setupClickListeners();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showErrorAndFinish("Error loading payment screen");
        }
    }

    /**
     * Get booking data from intent
     */
    private void getBookingDataFromIntent() {
        Intent intent = getIntent();
        busName = intent.getStringExtra("bus_name");
        price = intent.getStringExtra("price");
        time = intent.getStringExtra("time");
        fromLocation = intent.getStringExtra("from");
        toLocation = intent.getStringExtra("to");
        selectedDate = intent.getStringExtra("date");
        passengerCount = intent.getIntExtra("passengers", 1);

        Log.d(TAG, "Booking data loaded - Bus: " + busName + ", Price: " + price);
    }

    /**
     * Initialize UI components
     */
    private void initializeViews() {
        // Header
        buttonBack = findViewById(R.id.buttonBack);

        // Booking Summary
        textViewBusName = findViewById(R.id.textViewBusName);
        textViewRoute = findViewById(R.id.textViewRoute);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTime = findViewById(R.id.textViewTime);
        textViewBaseFare = findViewById(R.id.textViewBaseFare);
        textViewServiceFee = findViewById(R.id.textViewServiceFee);
        textViewTaxes = findViewById(R.id.textViewTaxes);
        textViewTotal = findViewById(R.id.textViewTotal);

        // Payment Method
        radioGroupPaymentMethod = findViewById(R.id.radioGroupPaymentMethod);
        radioCard = findViewById(R.id.radioCard);
        radioDigitalWallet = findViewById(R.id.radioDigitalWallet);

        // Card Details
        cardViewCardDetails = findViewById(R.id.cardViewCardDetails);
        editTextCardNumber = findViewById(R.id.editTextCardNumber);
        editTextExpiry = findViewById(R.id.editTextExpiry);
        editTextCVV = findViewById(R.id.editTextCVV);
        editTextCardholderName = findViewById(R.id.editTextCardholderName);

        // Pay Button
        buttonPay = findViewById(R.id.buttonPay);
    }

    /**
     * Setup booking summary with received data
     */
    private void setupBookingSummary() {
        try {
            // Set booking details
            if (textViewBusName != null) {
                textViewBusName.setText(busName != null ? busName : "Express Bus");
            }

            if (textViewRoute != null) {
                String route = (fromLocation != null ? fromLocation : "Negombo") + " → " +
                        (toLocation != null ? toLocation : "Colombo");
                textViewRoute.setText(route);
            }

            if (textViewDate != null) {
                textViewDate.setText(selectedDate != null ? selectedDate : "June 16, 2025");
            }

            if (textViewTime != null) {
                textViewTime.setText(time != null ? time : "10:30 AM");
            }

            // Calculate pricing
            int basePrice = extractPriceFromString(price);
            int serviceFee = 10;
            int taxes = 15;
            int total = basePrice + serviceFee + taxes;

            if (textViewBaseFare != null) {
                textViewBaseFare.setText("₹ " + basePrice);
            }
            if (textViewServiceFee != null) {
                textViewServiceFee.setText("₹ " + serviceFee);
            }
            if (textViewTaxes != null) {
                textViewTaxes.setText("₹ " + taxes);
            }
            if (textViewTotal != null) {
                textViewTotal.setText("₹ " + total);
            }

            // Update pay button
            if (buttonPay != null) {
                buttonPay.setText("Pay ₹ " + total);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up booking summary: " + e.getMessage(), e);
        }
    }

    /**
     * Extract numeric price from price string
     */
    private int extractPriceFromString(String priceStr) {
        try {
            if (priceStr != null) {
                // Remove currency symbols and extract number
                String numericStr = priceStr.replaceAll("[^0-9]", "");
                if (!numericStr.isEmpty()) {
                    return Integer.parseInt(numericStr);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting price: " + e.getMessage(), e);
        }
        return 150; // Default price
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        // Back button
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Payment method selection
        if (radioGroupPaymentMethod != null) {
            radioGroupPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.radioCard) {
                    showCardDetails(true);
                } else if (checkedId == R.id.radioDigitalWallet) {
                    showCardDetails(false);
                }
            });
        }

        // Pay button
        if (buttonPay != null) {
            buttonPay.setOnClickListener(v -> processPayment());
        }

        // Default to card payment
        if (radioCard != null) {
            radioCard.setChecked(true);
            showCardDetails(true);
        }
    }

    /**
     * Show/hide card details based on payment method
     */
    private void showCardDetails(boolean show) {
        if (cardViewCardDetails != null) {
            cardViewCardDetails.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Process payment
     */
    private void processPayment() {
        try {
            // Validate payment method
            int selectedPaymentMethod = radioGroupPaymentMethod.getCheckedRadioButtonId();

            if (selectedPaymentMethod == R.id.radioCard) {
                if (!validateCardDetails()) {
                    return;
                }
            }

            // Show processing
            buttonPay.setText("Processing...");
            buttonPay.setEnabled(false);

            // Simulate payment processing
            buttonPay.postDelayed(() -> {
                // Navigate to success screen
                Intent intent = new Intent(PaymentActivity.this, PaymentSuccessActivity.class);
                intent.putExtra("bus_name", busName);
                intent.putExtra("route", textViewRoute.getText().toString());
                intent.putExtra("date", selectedDate);
                intent.putExtra("time", time);
                intent.putExtra("total_amount", textViewTotal.getText().toString());

                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }, 2000); // 2 second delay to simulate processing

        } catch (Exception e) {
            Log.e(TAG, "Error processing payment: " + e.getMessage(), e);
            Toast.makeText(this, "Payment processing failed", Toast.LENGTH_SHORT).show();

            // Reset button
            buttonPay.setText("Pay " + textViewTotal.getText().toString());
            buttonPay.setEnabled(true);
        }
    }

    /**
     * Validate card details
     */
    private boolean validateCardDetails() {
        if (editTextCardNumber != null && editTextCardNumber.getText().toString().trim().isEmpty()) {
            editTextCardNumber.setError("Card number is required");
            editTextCardNumber.requestFocus();
            return false;
        }

        if (editTextExpiry != null && editTextExpiry.getText().toString().trim().isEmpty()) {
            editTextExpiry.setError("Expiry date is required");
            editTextExpiry.requestFocus();
            return false;
        }

        if (editTextCVV != null && editTextCVV.getText().toString().trim().isEmpty()) {
            editTextCVV.setError("CVV is required");
            editTextCVV.requestFocus();
            return false;
        }

        if (editTextCardholderName != null && editTextCardholderName.getText().toString().trim().isEmpty()) {
            editTextCardholderName.setError("Cardholder name is required");
            editTextCardholderName.requestFocus();
            return false;
        }

        return true;
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
        Log.d(TAG, "PaymentActivity destroyed");
    }
}
