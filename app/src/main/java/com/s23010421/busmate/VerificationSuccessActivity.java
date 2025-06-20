package com.s23010421.busmate;



import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * VerificationSuccessActivity handles password reset after successful OTP verification
 * Allows user to create a new password with validation
 */
public class VerificationSuccessActivity extends AppCompatActivity {

    // UI component declarations
    private TextView textViewSuccessMessage;
    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonResetPassword;

    // Database helper instance
    private DatabaseHelper databaseHelper;

    // User email from previous activity
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_success);

        // Get user email from intent
        userEmail = getIntent().getStringExtra("email");

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Display success message
        displaySuccessMessage();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        textViewSuccessMessage = findViewById(R.id.textViewSuccessMessage);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
    }

    /**
     * Set up click listeners for interactive elements
     */
    private void setupClickListeners() {
        // Reset password button click listener
        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPasswordReset();
            }
        });
    }

    /**
     * Display verification success message
     */
    private void displaySuccessMessage() {
        if (textViewSuccessMessage != null) {
            textViewSuccessMessage.setText("Verification Successful!\nYour email has been verified successfully. " +
                    "You can now create a new password for your account.");
        }
    }

    /**
     * Attempt password reset with validation
     */
    private void attemptPasswordReset() {
        // Get form values
        String newPassword = editTextNewPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        // Validate input fields
        if (!validatePasswordInput(newPassword, confirmPassword)) {
            return;
        }

        // Show loading state
        buttonResetPassword.setEnabled(false);
        buttonResetPassword.setText("Resetting Password...");

        // Perform password reset in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulate processing delay
                    Thread.sleep(2000);

                    // Update password in database
                    boolean passwordUpdated = updateUserPassword(userEmail, newPassword);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Reset button state
                            buttonResetPassword.setEnabled(true);
                            buttonResetPassword.setText("Reset Password");

                            if (passwordUpdated) {
                                // Password reset successful
                                Toast.makeText(VerificationSuccessActivity.this,
                                        "Password reset successful!",
                                        Toast.LENGTH_LONG).show();

                                // Navigate to sign in screen
                                Intent intent = new Intent(VerificationSuccessActivity.this, SignInActivity.class);
                                intent.putExtra("reset_email", userEmail);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                // Password reset failed
                                Toast.makeText(VerificationSuccessActivity.this,
                                        "Failed to reset password. Please try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } catch (InterruptedException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonResetPassword.setEnabled(true);
                            buttonResetPassword.setText("Reset Password");
                            Toast.makeText(VerificationSuccessActivity.this,
                                    "Password reset failed. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Validate password input fields
     * @param newPassword New password string
     * @param confirmPassword Confirm password string
     * @return True if validation passes, false otherwise
     */
    private boolean validatePasswordInput(String newPassword, String confirmPassword) {
        // Reset previous error messages
        editTextNewPassword.setError(null);
        editTextConfirmPassword.setError(null);

        boolean isValid = true;

        // Validate new password
        if (TextUtils.isEmpty(newPassword)) {
            editTextNewPassword.setError("New password is required");
            editTextNewPassword.requestFocus();
            isValid = false;
        } else if (newPassword.length() < 6) {
            editTextNewPassword.setError("Password must be at least 6 characters");
            editTextNewPassword.requestFocus();
            isValid = false;
        } else if (!isPasswordStrong(newPassword)) {
            editTextNewPassword.setError("Password must contain at least one letter and one number");
            editTextNewPassword.requestFocus();
            isValid = false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Please confirm your password");
            if (isValid) {
                editTextConfirmPassword.requestFocus();
            }
            isValid = false;
        } else if (!newPassword.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            if (isValid) {
                editTextConfirmPassword.requestFocus();
            }
            isValid = false;
        }

        return isValid;
    }

    /**
     * Check if password meets strength requirements
     * @param password Password to check
     * @return True if password is strong, false otherwise
     */
    private boolean isPasswordStrong(String password) {
        // Check for at least one letter and one number
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");

        return hasLetter && hasNumber;
    }

    /**
     * Update user password in database
     * @param email User email
     * @param newPassword New password
     * @return True if update successful, false otherwise
     */
    private boolean updateUserPassword(String email, String newPassword) {
        try {
            // In a real application, you would add this method to DatabaseHelper
            // For now, we'll simulate a successful update

            // TODO: Add updateUserPassword method to DatabaseHelper class
            // return databaseHelper.updateUserPassword(email, newPassword);

            // Simulated successful update
            android.util.Log.d("PASSWORD_RESET", "Password updated for email: " + email);
            return true;

        } catch (Exception e) {
            android.util.Log.e("PASSWORD_RESET", "Failed to update password", e);
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
