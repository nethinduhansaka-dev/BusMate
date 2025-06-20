package com.s23010421.busmate;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * DriverCommunicationActivity - Communication interface with bus drivers
 */
public class DriverCommunicationActivity extends AppCompatActivity {

    private TextView driverName, busNumber;
    private EditText messageInput;
    private Button sendButton;
    private RecyclerView messagesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_communication);

        initializeViews();
        setupClickListeners();
        loadDriverInfo();
    }

    private void initializeViews() {
        driverName = findViewById(R.id.textViewDriverName);
        busNumber = findViewById(R.id.textViewBusNumber);
        messageInput = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSendMessage);
        messagesRecycler = findViewById(R.id.recyclerViewMessages);
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadDriverInfo() {
        String busNum = getIntent().getStringExtra("bus_number");
        String routeNum = getIntent().getStringExtra("route_number");

        if (busNum != null) {
            busNumber.setText("Bus " + busNum);
        }
        driverName.setText("Driver");
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            Toast.makeText(this, "Message sent: " + message, Toast.LENGTH_SHORT).show();
            messageInput.setText("");
        }
    }
}
