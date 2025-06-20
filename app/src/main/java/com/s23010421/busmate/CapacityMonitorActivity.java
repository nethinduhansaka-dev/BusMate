package com.s23010421.busmate;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * CapacityMonitorActivity - Monitor bus capacity and crowding
 */
public class CapacityMonitorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capacity_monitor);

        String routeNumber = getIntent().getStringExtra("route_number");

        TextView capacityInfo = findViewById(R.id.textViewCapacityInfo);
        capacityInfo.setText("Capacity Monitor for Route " + routeNumber);
    }
}
