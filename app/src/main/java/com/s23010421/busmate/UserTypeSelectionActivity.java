package com.s23010421.busmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * UserTypeSelectionActivity - User type selection screen for BusMate
 * Allows users to choose between Passenger and Bus Operator registration
 */
public class UserTypeSelectionActivity extends AppCompatActivity {

    // UI component declarations
    private CardView cardViewPassenger;
    private CardView cardViewBusOperator;
    private TextView textViewBackToMain;
    private TextView textViewAlreadyHaveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_selection);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        cardViewPassenger = findViewById(R.id.cardViewPassenger);
        cardViewBusOperator = findViewById(R.id.cardViewBusOperator);
        textViewBackToMain = findViewById(R.id.textViewBackToMain);
        textViewAlreadyHaveAccount = findViewById(R.id.textViewAlreadyHaveAccount);
    }

    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Passenger card click listener
        cardViewPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPassengerRegistration();
            }
        });

        // Bus operator card click listener
        cardViewBusOperator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToBusOperatorRegistration();
            }
        });

        // Back button click listener
        textViewBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserTypeSelectionActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Already have account click listener
        textViewAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserTypeSelectionActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Navigate to passenger registration
     */
    private void navigateToPassengerRegistration() {
        Intent intent = new Intent(UserTypeSelectionActivity.this, SignUpActivity.class);
        intent.putExtra("user_type", "passenger");
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Navigate to bus operator registration
     */
    private void navigateToBusOperatorRegistration() {
        Intent intent = new Intent(UserTypeSelectionActivity.this, BusOperatorRegistrationActivity.class);
        intent.putExtra("user_type", "bus_operator");
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}