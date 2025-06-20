package com.s23010421.busmate;


import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

/**
 * OtpVerificationActivity handles OTP verification for password reset
 * Includes resend functionality and countdown timer
 */
public class OtpVerificationActivity extends AppCompatActivity {

    // UI component declarations
    private EditText editTextOtpDigit1;
    private EditText editTextOtpDigit2;
    private EditText editTextOtpDigit3;
    private EditText editTextOtpDigit4;
    private EditText editTextOtpDigit5;
    private EditText editTextOtpDigit6;
    private Button buttonVerifyCode;
    private TextView textViewResendCode;
    private TextView textViewBackButton;
    private TextView textViewEmailDisplay;
    private TextView textViewCountdown;

    // Data from previous activity
    private String userEmail;
    private String generatedOTP;

    // Countdown timer for resend functionality
    private CountDownTimer countDownTimer;
    private boolean canResend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Get data from intent
        userEmail = getIntent().getStringExtra("email");
        generatedOTP = getIntent().getStringExtra("generated_otp");

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Display email and start countdown
        displayEmailAndStartCountdown();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        editTextOtpDigit1 = findViewById(R.id.editTextOtpDigit1);
        editTextOtpDigit2 = findViewById(R.id.editTextOtpDigit2);
        editTextOtpDigit3 = findViewById(R.id.editTextOtpDigit3);
        editTextOtpDigit4 = findViewById(R.id.editTextOtpDigit4);
        editTextOtpDigit5 = findViewById(R.id.editTextOtpDigit5);
        editTextOtpDigit6 = findViewById(R.id.editTextOtpDigit6);
        buttonVerifyCode = findViewById(R.id.buttonVerifyCode);
        textViewResendCode = findViewById(R.id.textViewResendCode);
        textViewBackButton = findViewById(R.id.textViewBackButton);
        textViewEmailDisplay = findViewById(R.id.textViewEmailDisplay);
        textViewCountdown = findViewById(R.id.textViewCountdown);
    }

    /**
     * Set up click listeners for interactive elements
     */
    private void setupClickListeners() {
        // Verify code button click listener
        buttonVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptOtpVerification();
            }
        });

        // Resend code link click listener
        textViewResendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canResend) {
                    resendOTP();
                } else {
                    Toast.makeText(OtpVerificationActivity.this,
                            "Please wait before requesting a new code",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Back button click listener
        textViewBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Setup auto-focus for OTP input fields
        setupOtpAutoFocus();
    }

    /**
     * Setup auto-focus functionality for OTP input fields
     */
    private void setupOtpAutoFocus() {
        EditText[] otpFields = {editTextOtpDigit1, editTextOtpDigit2, editTextOtpDigit3,
                editTextOtpDigit4, editTextOtpDigit5, editTextOtpDigit6};

        for (int i = 0; i < otpFields.length; i++) {
            final int currentIndex = i;
            final EditText currentField = otpFields[i];

            currentField.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpFields.length - 1) {
                        // Move to next field
                        otpFields[currentIndex + 1].requestFocus();
                    } else if (s.length() == 0 && currentIndex > 0) {
                        // Move to previous field on backspace
                        otpFields[currentIndex - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    /**
     * Display user email and start countdown timer
     */
    private void displayEmailAndStartCountdown() {
        // Display masked email
        if (userEmail != null) {
            String maskedEmail = maskEmail(userEmail);
            textViewEmailDisplay.setText("We've sent a 6-digit verification code to " + maskedEmail);
        }

        // Start 60-second countdown
        startCountdownTimer();
    }

    /**
     * Mask email address for privacy
     * @param email Original email address
     * @return Masked email address
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 3) {
            return username.charAt(0) + "***@" + domain;
        } else {
            return username.substring(0, 2) + "***" + username.charAt(username.length() - 1) + "@" + domain;
        }
    }

    /**
     * Start countdown timer for resend functionality
     */
    private void startCountdownTimer() {
        canResend = false;
        textViewResendCode.setEnabled(false);
        textViewResendCode.setTextColor(getResources().getColor(android.R.color.darker_gray));

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                textViewCountdown.setText("Resend code in " + seconds + "s");
            }

            @Override
            public void onFinish() {
                canResend = true;
                textViewResendCode.setEnabled(true);
                textViewResendCode.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
                textViewCountdown.setText("");
            }
        }.start();
    }

    /**
     * Attempt OTP verification
     */
    private void attemptOtpVerification() {
        // Get entered OTP
        String enteredOTP = getEnteredOTP();

        // Validate OTP input
        if (!validateOtpInput(enteredOTP)) {
            return;
        }

        // Show loading state
        buttonVerifyCode.setEnabled(false);
        buttonVerifyCode.setText("Verifying...");

        // Verify OTP in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulate verification delay
                    Thread.sleep(1500);

                    // Check if entered OTP matches generated OTP
                    boolean isValidOTP = enteredOTP.equals(generatedOTP);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Reset button state
                            buttonVerifyCode.setEnabled(true);
                            buttonVerifyCode.setText("Verify Code");

                            if (isValidOTP) {
                                // OTP verification successful
                                Intent intent = new Intent(OtpVerificationActivity.this, VerificationSuccessActivity.class);
                                intent.putExtra("email", userEmail);
                                startActivity(intent);
                                finish();

                                Toast.makeText(OtpVerificationActivity.this,
                                        "Verification successful!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // OTP verification failed
                                clearOtpFields();
                                Toast.makeText(OtpVerificationActivity.this,
                                        "Invalid verification code. Please try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } catch (InterruptedException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonVerifyCode.setEnabled(true);
                            buttonVerifyCode.setText("Verify Code");
                            Toast.makeText(OtpVerificationActivity.this,
                                    "Verification failed. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Get entered OTP from all input fields
     * @return Complete OTP string
     */
    private String getEnteredOTP() {
        StringBuilder otp = new StringBuilder();
        otp.append(editTextOtpDigit1.getText().toString().trim());
        otp.append(editTextOtpDigit2.getText().toString().trim());
        otp.append(editTextOtpDigit3.getText().toString().trim());
        otp.append(editTextOtpDigit4.getText().toString().trim());
        otp.append(editTextOtpDigit5.getText().toString().trim());
        otp.append(editTextOtpDigit6.getText().toString().trim());
        return otp.toString();
    }

    /**
     * Validate OTP input
     * @param otp Entered OTP string
     * @return True if validation passes, false otherwise
     */
    private boolean validateOtpInput(String otp) {
        if (TextUtils.isEmpty(otp) || otp.length() != 6) {
            Toast.makeText(this, "Please enter the complete 6-digit code", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!otp.matches("\\d{6}")) {
            Toast.makeText(this, "Please enter only numbers", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Clear all OTP input fields
     */
    private void clearOtpFields() {
        editTextOtpDigit1.setText("");
        editTextOtpDigit2.setText("");
        editTextOtpDigit3.setText("");
        editTextOtpDigit4.setText("");
        editTextOtpDigit5.setText("");
        editTextOtpDigit6.setText("");
        editTextOtpDigit1.requestFocus();
    }

    /**
     * Resend OTP functionality
     */
    private void resendOTP() {
        // Generate new OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        generatedOTP = String.valueOf(otp);

        // Clear previous OTP input
        clearOtpFields();

        // Restart countdown timer
        startCountdownTimer();

        // Show success message
        Toast.makeText(this, "New verification code sent!", Toast.LENGTH_SHORT).show();

        // Log the new OTP for testing (remove in production)
        android.util.Log.d("OTP_DEBUG", "New OTP: " + generatedOTP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
