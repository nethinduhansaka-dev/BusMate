package com.s23010421.busmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * PaymentSuccessActivity for EEI4369 BusMate Project
 * Shows payment confirmation and ticket details
 */
public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        // Get payment details from intent
        Intent intent = getIntent();
        String busName = intent.getStringExtra("bus_name");
        String route = intent.getStringExtra("route");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String totalAmount = intent.getStringExtra("total_amount");

        // Setup UI with payment details
        TextView textViewSuccessMessage = findViewById(R.id.textViewSuccessMessage);
        TextView textViewBookingDetails = findViewById(R.id.textViewBookingDetails);
        Button buttonDone = findViewById(R.id.buttonDone);

        // Set success message
        textViewSuccessMessage.setText("✅ Payment Successful!\n\nYour ticket has been booked successfully.");

        // Set booking details
        String details = "Bus: " + busName + "\n" +
                "Route: " + route + "\n" +
                "Date: " + date + "\n" +
                "Time: " + time + "\n" +
                "Amount Paid: " + totalAmount;
        textViewBookingDetails.setText(details);

        // Done button to return to dashboard
        buttonDone.setOnClickListener(v -> {
            Intent dashboardIntent = new Intent(this, PassengerDashboardActivity.class);
            dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(dashboardIntent);
            finish();
        });
    }
}
