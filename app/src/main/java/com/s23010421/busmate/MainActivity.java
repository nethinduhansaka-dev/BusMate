package com.s23010421.busmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

/**
 * MainActivity - Get Started screen for BusMate application
 * FIXED: Added comprehensive error handling and debugging
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // UI component declarations
    private TextView textViewAppName;
    private TextView textViewTagline;
    private TextView textViewWelcomeMessage;
    private Button buttonGetStarted;
    private TextView textViewSkipForNow;

    // Session management
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "BusMatePrefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_TYPE = "user_type";

    // Loading delay for smooth transition
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Starting MainActivity");

        try {
            // Install splash screen before setContentView
            SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            Log.d(TAG, "onCreate: Layout set successfully");

            // Initialize SharedPreferences
            sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

            // Initialize UI components
            initializeViews();

            // Set up click listeners
            setupClickListeners();

            // Display app introduction
            displayAppIntroduction();

            // Check existing session
            checkExistingSession();

            Log.d(TAG, "onCreate: MainActivity initialization completed");

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error initializing MainActivity", e);
            Toast.makeText(this, "Error starting BusMate. Please try again.", Toast.LENGTH_LONG).show();
            // Fallback: Show a simple error message and allow retry
            showErrorFallback();
        }
    }

    /**
     * FIXED: Initialize UI components with null checks and error handling
     */
    private void initializeViews() {
        try {
            textViewAppName = findViewById(R.id.textViewAppName);
            textViewTagline = findViewById(R.id.textViewTagline);
            textViewWelcomeMessage = findViewById(R.id.textViewWelcomeMessage);
            buttonGetStarted = findViewById(R.id.buttonGetStarted);
            textViewSkipForNow = findViewById(R.id.textViewSkipForNow);

            // Verify all views were found
            if (buttonGetStarted == null) {
                Log.e(TAG, "initializeViews: buttonGetStarted not found in layout");
                throw new RuntimeException("Get Started button not found in layout");
            }

            Log.d(TAG, "initializeViews: All views initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "initializeViews: Error finding views", e);
            Toast.makeText(this, "UI initialization error. Please restart the app.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * FIXED: Setup click listeners with comprehensive error handling
     */
    private void setupClickListeners() {
        try {
            // Get Started button - navigate to user type selection
            if (buttonGetStarted != null) {
                buttonGetStarted.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Get Started button clicked");

                        // Disable button to prevent multiple clicks
                        buttonGetStarted.setEnabled(false);
                        buttonGetStarted.setText("Loading...");

                        // Add small delay for user feedback
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                navigateToSignUp();
                            }
                        }, 500);
                    }
                });
                Log.d(TAG, "setupClickListeners: Get Started button listener set");
            } else {
                Log.e(TAG, "setupClickListeners: buttonGetStarted is null");
            }

            // Skip for now - navigate to guest mode (optional)
            if (textViewSkipForNow != null) {
                textViewSkipForNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Skip for now clicked");
                        navigateToGuestMode();
                    }
                });
            }

            Log.d(TAG, "setupClickListeners: All click listeners set successfully");

        } catch (Exception e) {
            Log.e(TAG, "setupClickListeners: Error setting up click listeners", e);
            Toast.makeText(this, "Button setup error. Please restart the app.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Display app introduction
     */
    private void displayAppIntroduction() {
        try {
            if (textViewWelcomeMessage != null) {
                textViewWelcomeMessage.setText("Experience seamless bus travel with real-time tracking, " +
                        "digital ticketing, and intelligent route planning at your fingertips.");
            }
            Log.d(TAG, "displayAppIntroduction: Welcome message set");
        } catch (Exception e) {
            Log.e(TAG, "displayAppIntroduction: Error setting welcome message", e);
        }
    }

    /**
     * FIXED: Navigate to User Type Selection activity with error handling
     */
    private void navigateToSignUp() {
        try {
            Log.d(TAG, "navigateToSignUp: Attempting to start UserTypeSelectionActivity");

            Intent intent = new Intent(MainActivity.this, UserTypeSelectionActivity.class);

            // Verify the intent can be resolved
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                // Add smooth transition animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Log.d(TAG, "navigateToSignUp: Successfully navigated to UserTypeSelectionActivity");
            } else {
                Log.e(TAG, "navigateToSignUp: UserTypeSelectionActivity not found");
                showNavigationError();
            }

        } catch (Exception e) {
            Log.e(TAG, "navigateToSignUp: Error navigating to user type selection", e);
            showNavigationError();
        } finally {
            // Re-enable button
            if (buttonGetStarted != null) {
                buttonGetStarted.setEnabled(true);
                buttonGetStarted.setText("Get Started");
            }
        }
    }

    /**
     * FIXED: Navigate to guest mode with error handling
     */
    private void navigateToGuestMode() {
        try {
            Toast.makeText(this,
                    "Please select account type to continue with full features.",
                    Toast.LENGTH_LONG).show();

            // Redirect to user type selection after a delay
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    navigateToSignUp();
                }
            }, 2000);

        } catch (Exception e) {
            Log.e(TAG, "navigateToGuestMode: Error in guest mode navigation", e);
            Toast.makeText(this, "Navigation error. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if user is already logged in and redirect accordingly
     */
    private void checkExistingSession() {
        try {
            if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
                // User is already logged in, redirect to dashboard after delay
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        redirectToUserDashboard();
                    }
                }, SPLASH_DELAY);
            }
        } catch (Exception e) {
            Log.e(TAG, "checkExistingSession: Error checking session", e);
        }
    }

    /**
     * Redirect to appropriate user dashboard based on user type
     */
    private void redirectToUserDashboard() {
        try {
            String userType = sharedPreferences.getString(KEY_USER_TYPE, "passenger");

            Intent intent;
            if ("bus_operator".equals(userType)) {
                intent = new Intent(MainActivity.this, BusOperatorDashboardActivity.class);
            } else {
                intent = new Intent(MainActivity.this, PassengerDashboardActivity.class);
            }

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                finish(); // Close main activity
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Log.d(TAG, "redirectToUserDashboard: Successfully redirected to dashboard");
            } else {
                Log.e(TAG, "redirectToUserDashboard: Dashboard activity not found");
                Toast.makeText(this, "Dashboard not available. Please sign in again.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "redirectToUserDashboard: Error redirecting to dashboard", e);
            Toast.makeText(this, "Error accessing dashboard. Please sign in again.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show navigation error message
     */
    private void showNavigationError() {
        Toast.makeText(this,
                "Navigation error. Please check if the app is properly installed.",
                Toast.LENGTH_LONG).show();

        // Provide alternative action
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Navigation Error")
                .setMessage("Unable to proceed. Would you like to restart the app?")
                .setPositiveButton("Restart", (dialog, which) -> {
                    recreate(); // Restart the activity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show error fallback UI
     */
    private void showErrorFallback() {
        setContentView(android.R.layout.activity_list_item);

        // Create a simple fallback UI
        TextView errorText = new TextView(this);
        errorText.setText("BusMate initialization error. Please restart the app.");
        errorText.setTextSize(16);
        errorText.setPadding(50, 50, 50, 50);
        setContentView(errorText);

        // Auto-restart after 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recreate();
            }
        }, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: MainActivity resumed");
    }

    @Override
    public void onBackPressed() {
        // Show exit confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit BusMate")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    super.onBackPressed();
                    finishAffinity(); // Close all activities
                })
                .setNegativeButton("No", null)
                .show();
    }
}
