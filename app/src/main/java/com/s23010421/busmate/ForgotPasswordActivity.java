package com.s23010421.busmate;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

/**
 * ForgotPasswordActivity handles password reset functionality for BusMate
 * Enhanced with debugging and better error handling
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    // UI component declarations
    private EditText editTextEmailPhone;
    private Button buttonSendResetLink;
    private TextView textViewContactSupport;
    private TextView textViewBackToSignIn;
    private TextView textViewDebugInfo; // For debugging

    // Database helper instance
    private DatabaseHelper databaseHelper;

    // Generated OTP for verification
    private String generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Show debug information
        showDebugInfo();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        editTextEmailPhone = findViewById(R.id.editTextEmailPhone);
        buttonSendResetLink = findViewById(R.id.buttonSendResetLink);
        textViewContactSupport = findViewById(R.id.textViewContactSupport);
        textViewBackToSignIn = findViewById(R.id.textViewBackToSignIn);

        // Add debug text view if it exists
        textViewDebugInfo = findViewById(R.id.textViewDebugInfo);
    }

    /**
     * Set up click listeners for interactive elements
     */
    private void setupClickListeners() {
        // Send reset link button click listener
        buttonSendResetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPasswordReset();
            }
        });

        // Contact support link click listener
        textViewContactSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Contact Support: support@busmate.com",
                        Toast.LENGTH_LONG).show();
            }
        });

        // Back to sign in link click listener
        textViewBackToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Show debug information about registered users
     */
    private void showDebugInfo() {
        int userCount = databaseHelper.getUserCount();
        String debugMessage = "Users in database: " + userCount;

        if (userCount > 0) {
            // Show registered emails for testing
            Cursor allUsers = databaseHelper.getAllUsers();
            if (allUsers != null) {
                StringBuilder emails = new StringBuilder();
                while (allUsers.moveToNext()) {
                    int emailIndex = allUsers.getColumnIndex(DatabaseHelper.COL_EMAIL);
                    if (emailIndex >= 0) {
                        String email = allUsers.getString(emailIndex);
                        if (emails.length() > 0) emails.append(", ");
                        emails.append(email);
                    }
                }
                allUsers.close();
                debugMessage += "\nRegistered emails: " + emails.toString();
            }
        } else {
            debugMessage += "\nNo users registered yet. Please register first!";
        }

        Log.d(TAG, debugMessage);

        // Show debug info in UI if debug text view exists
        if (textViewDebugInfo != null) {
            textViewDebugInfo.setText(debugMessage);
        } else {
            // Show as toast for quick debugging
            Toast.makeText(this, debugMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Attempt password reset with enhanced error checking
     */
    private void attemptPasswordReset() {
        String emailPhone = editTextEmailPhone.getText().toString().trim();

        // Validate input
        if (!validateInput(emailPhone)) {
            return;
        }

        // Show loading state
        buttonSendResetLink.setEnabled(false);
        buttonSendResetLink.setText("Checking...");

        // Check email existence immediately for better debugging
        boolean emailExists = databaseHelper.isEmailExists(emailPhone);
        Log.d(TAG, "Email check result for '" + emailPhone + "': " + emailExists);

        if (!emailExists) {
            // Reset button state
            buttonSendResetLink.setEnabled(true);
            buttonSendResetLink.setText("Send Reset Link");

            // Show more helpful error message
            String errorMessage = "Email address not found.\n\n";
            int userCount = databaseHelper.getUserCount();
            if (userCount == 0) {
                errorMessage += "No users registered yet. Please sign up first!";
            } else {
                errorMessage += "Please check your email address and try again.";
            }

            editTextEmailPhone.setError(errorMessage);
            editTextEmailPhone.requestFocus();

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            return;
        }

        // Simulate sending reset link in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulate network delay
                    Thread.sleep(1000);

                    // Generate OTP for verification
                    generatedOTP = generateOTP();
                    Log.d(TAG, "Generated OTP: " + generatedOTP); // For testing

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Reset button state
                            buttonSendResetLink.setEnabled(true);
                            buttonSendResetLink.setText("Send Reset Link");

                            // Navigate to OTP verification screen
                            Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", emailPhone);
                            intent.putExtra("generated_otp", generatedOTP);
                            startActivity(intent);

                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Verification code sent!\nOTP: " + generatedOTP,
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (InterruptedException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonSendResetLink.setEnabled(true);
                            buttonSendResetLink.setText("Send Reset Link");
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Failed to send reset link. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Validate user input for email/phone
     * @param emailPhone User email or phone number
     * @return True if validation passes, false otherwise
     */
    private boolean validateInput(String emailPhone) {
        // Reset previous error messages
        editTextEmailPhone.setError(null);

        // Validate email/phone field
        if (TextUtils.isEmpty(emailPhone)) {
            editTextEmailPhone.setError("Email or phone number is required");
            editTextEmailPhone.requestFocus();
            return false;
        }

        // Check if input is email format
        if (emailPhone.contains("@")) {
            if (!Patterns.EMAIL_ADDRESS.matcher(emailPhone).matches()) {
                editTextEmailPhone.setError("Please enter a valid email address");
                editTextEmailPhone.requestFocus();
                return false;
            }
        } else {
            // Check if input is phone number format
            if (emailPhone.length() < 10) {
                editTextEmailPhone.setError("Please enter a valid phone number");
                editTextEmailPhone.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Generate 6-digit OTP for verification
     * @return Generated OTP string
     */
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate 6-digit number
        return String.valueOf(otp);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
