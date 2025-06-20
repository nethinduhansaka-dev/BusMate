package com.s23010421.busmate;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * BusDetailsActivity - Detailed information about a specific bus
 */
public class BusDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_details);

        String busNumber = getIntent().getStringExtra("bus_number");
        String routeNumber = getIntent().getStringExtra("route_number");

        TextView busInfo = findViewById(R.id.textViewBusInfo);
        busInfo.setText("Bus Details: " + busNumber + " on Route " + routeNumber);
    }
}
