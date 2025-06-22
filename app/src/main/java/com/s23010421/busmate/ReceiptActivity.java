package com.s23010421.busmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView; // fixed import

import androidx.appcompat.app.AppCompatActivity;

/**
 * ReceiptActivity for displaying trip receipts
 */
public class ReceiptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        // Get trip details from intent
        Intent intent = getIntent();
        String route = intent.getStringExtra("trip_route");
        String price = intent.getStringExtra("trip_price");
        String date = intent.getStringExtra("trip_date");
        String duration = intent.getStringExtra("trip_duration");
        String routeNumber = intent.getStringExtra("route_number");
        String paymentMethod = intent.getStringExtra("payment_method");

        // Setup UI
        TextView textViewReceiptTitle = findViewById(R.id.textViewReceiptTitle);
        TextView textViewReceiptDetails = findViewById(R.id.textViewReceiptDetails);
        Button buttonClose = findViewById(R.id.buttonClose);

        textViewReceiptTitle.setText("Trip Receipt");

        String details = "Route: " + route + "\n" +
                "Date: " + date + "\n" +
                "Duration: " + duration + "\n" +
                "Route Number: " + routeNumber + "\n" +
                "Payment: " + paymentMethod + "\n" +
                "Amount: " + price;
        textViewReceiptDetails.setText(details);

        buttonClose.setOnClickListener(v -> finish());
    }
}
