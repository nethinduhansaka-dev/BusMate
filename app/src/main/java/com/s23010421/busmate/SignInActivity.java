package com.s23010421.busmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SignInActivity handles authentication for both passengers and bus operators
 * Enhanced with dual user type support and proper dashboard redirection
 */
public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    // UI component declarations
    private EditText editTextEmail;
    private EditText editTextPassword;
    private CheckBox checkBoxRememberMe;
    private Button buttonSignIn;
    private Button buttonGoogleSignIn;
    private Button buttonFacebookSignIn;
    private TextView textViewForgotPassword;
    private TextView textViewSignUp;

    // Database helper instance
    private DatabaseHelper databaseHelper;

    // SharedPreferences for session management
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "BusMatePrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_TYPE = "user_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load saved credentials if remember me was checked
        loadSavedCredentials();

        // Check if user is already logged in
        checkExistingLogin();

        // Handle pre-filled email from registration
        handleRegisteredEmail();
    }

    /**
     * Initialize all UI components with proper error handling
     */
    private void initializeViews() {
        try {
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);
            buttonSignIn = findViewById(R.id.buttonSignIn);
            buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);
            buttonFacebookSignIn = findViewById(R.id.buttonFacebookSignIn);
            textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
            textViewSignUp = findViewById(R.id.textViewSignUp);

        } catch (Exception e) {
            Log.e(TAG, "Error initializing interface", e);
            Toast.makeText(this, "Error initializing interface", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Sign in button click listener
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignIn();
            }
        });

        // Forgot password link click listener
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Sign up link click listener
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, UserTypeSelectionActivity.class);
                startActivity(intent);
            }
        });

        // Google sign in button (placeholder for future implementation)
        buttonGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignInActivity.this, "Google Sign-In will be available soon",
                        Toast.LENGTH_SHORT).show();
                // TODO: Implement Google OAuth integration
            }
        });

        // Facebook sign in button (placeholder for future implementation)
        buttonFacebookSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignInActivity.this, "Facebook Sign-In will be available soon",
                        Toast.LENGTH_SHORT).show();
                // TODO: Implement Facebook SDK integration
            }
        });
    }

    /**
     * Handle pre-filled email from registration activities
     */
    private void handleRegisteredEmail() {
        String registeredEmail = getIntent().getStringExtra("registered_email");
        if (registeredEmail != null && !registeredEmail.isEmpty()) {
            editTextEmail.setText(registeredEmail);
            editTextPassword.requestFocus();

            Toast.makeText(this, "Registration successful! Please sign in with your new account.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Attempt user sign in with validation and authentication
     */
    private void attemptSignIn() {
        // Get input values
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();

        Log.d(TAG, "Sign in attempt for email: " + email);

        // Validate input fields
        if (!validateInput(email, password)) {
            return;
        }

        // Show loading state
        buttonSignIn.setEnabled(false);
        buttonSignIn.setText("Signing In...");

        // Perform authentication in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulate network delay
                    Thread.sleep(1000);

                    // Authenticate user with database
                    Cursor userCursor = databaseHelper.authenticateUser(email, password);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Reset button state
                            buttonSignIn.setEnabled(true);
                            buttonSignIn.setText("Sign In");

                            if (userCursor != null && userCursor.moveToFirst()) {
                                // Authentication successful
                                handleSuccessfulLogin(userCursor);
                            } else {
                                // Authentication failed
                                Toast.makeText(SignInActivity.this,
                                        "Invalid email or password. Please check your credentials and try again.",
                                        Toast.LENGTH_LONG).show();

                                // Clear password field for security
                                editTextPassword.setText("");
                                editTextPassword.requestFocus();
                            }

                            if (userCursor != null) {
                                userCursor.close();
                            }
                        }
                    });

                } catch (InterruptedException e) {
                    Log.e(TAG, "Sign in process interrupted", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonSignIn.setEnabled(true);
                            buttonSignIn.setText("Sign In");
                            Toast.makeText(SignInActivity.this,
                                    "Sign in failed. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error during sign in", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonSignIn.setEnabled(true);
                            buttonSignIn.setText("Sign In");
                            Toast.makeText(SignInActivity.this,
                                    "Sign in failed due to unexpected error. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Validate user input for email and password
     * @param email User email address
     * @param password User password
     * @return True if validation passes, false otherwise
     */
    private boolean validateInput(String email, String password) {
        // Reset previous error messages
        editTextEmail.setError(null);
        editTextPassword.setError(null);

        boolean isValid = true;

        // Validate email field
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address");
            editTextEmail.requestFocus();
            isValid = false;
        }

        // Validate password field
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            if (isValid) {
                editTextPassword.requestFocus();
            }
            isValid = false;
        } else if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            if (isValid) {
                editTextPassword.requestFocus();
            }
            isValid = false;
        }

        return isValid;
    }

    /**
     * ENHANCED: Handle successful login with dual user type support
     * @param userCursor Database cursor containing user data
     */
    private void handleSuccessfulLogin(Cursor userCursor) {
        // Extract user information
        int userIdIndex = userCursor.getColumnIndex(DatabaseHelper.COL_USER_ID);
        int emailIndex = userCursor.getColumnIndex(DatabaseHelper.COL_EMAIL);
        int userTypeIndex = userCursor.getColumnIndex(DatabaseHelper.COL_USER_TYPE);

        if (userIdIndex >= 0 && emailIndex >= 0 && userTypeIndex >= 0) {
            long userId = userCursor.getLong(userIdIndex);
            String userEmail = userCursor.getString(emailIndex);
            String userType = userCursor.getString(userTypeIndex);

            Log.d(TAG, "Successful login: userId=" + userId + ", userType=" + userType);

            // Save login session with user type
            saveLoginSession(userId, userEmail, userType);

            // Save credentials if remember me is checked
            if (checkBoxRememberMe.isChecked()) {
                saveCredentials(editTextEmail.getText().toString(),
                        editTextPassword.getText().toString());
            } else {
                clearSavedCredentials();
            }

            // MODIFIED: Redirect to appropriate dashboard based on user type
            redirectToUserDashboard(userType, userId, userEmail);

        } else {
            Log.e(TAG, "Error extracting user data from database cursor");
            Toast.makeText(this, "Error retrieving user information. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * NEW METHOD: Redirect to appropriate dashboard based on user type
     * @param userType Type of user ("passenger" or "bus_operator")
     * @param userId User ID from database
     * @param email User email address
     */
    private void redirectToUserDashboard(String userType, long userId, String email) {
        Intent intent;
        String welcomeMessage;

        try {
            if ("bus_operator".equals(userType)) {
                // Redirect bus operators to operator dashboard
                intent = new Intent(SignInActivity.this, BusOperatorDashboardActivity.class);
                welcomeMessage = "Welcome back, Operator!";
                Log.d(TAG, "Redirecting bus operator to operator dashboard");
            } else {
                // Default to passenger dashboard for passengers or unknown types
                intent = new Intent(SignInActivity.this, PassengerDashboardActivity.class);
                welcomeMessage = "Welcome to BusMate!";
                Log.d(TAG, "Redirecting passenger to passenger dashboard");
            }

            // FIXED: Pass user data to dashboard with validation
            intent.putExtra("user_id", userId);
            intent.putExtra("user_email", email);
            intent.putExtra("user_type", userType);

            // FIXED: Clear activity stack to prevent back navigation to login
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Start new activity and finish current one
            startActivity(intent);
            finish();

            // Add smooth transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error redirecting to dashboard: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading dashboard. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * ENHANCED: Save user login session with user type in SharedPreferences
     * @param userId User ID
     * @param email User email
     * @param userType User type ("passenger" or "bus_operator")
     */
    private void saveLoginSession(long userId, String email, String userType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USER_TYPE, userType);
        editor.apply();

        Log.d(TAG, "Login session saved for user type: " + userType);
    }

    /**
     * Save user credentials for remember me functionality
     * @param email User email
     * @param password User password
     */
    private void saveCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_REMEMBER_ME, true);
        editor.apply();
    }

    /**
     * Load saved credentials if remember me was previously checked
     */
    private void loadSavedCredentials() {
        if (sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)) {
            String savedEmail = sharedPreferences.getString(KEY_EMAIL, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");

            editTextEmail.setText(savedEmail);
            editTextPassword.setText(savedPassword);
            checkBoxRememberMe.setChecked(true);
        }
    }

    /**
     * Clear saved credentials from SharedPreferences
     */
    private void clearSavedCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_PASSWORD);
        editor.putBoolean(KEY_REMEMBER_ME, false);
        editor.apply();
    }

    /**
     * ENHANCED: Check if user is already logged in and redirect to appropriate dashboard
     */
    private void checkExistingLogin() {
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            long userId = sharedPreferences.getLong(KEY_USER_ID, -1);
            String email = sharedPreferences.getString(KEY_EMAIL, "");
            String userType = sharedPreferences.getString(KEY_USER_TYPE, "passenger");

            Log.d(TAG, "Existing login found, redirecting to dashboard for user type: " + userType);

            // Redirect to appropriate dashboard
            redirectToUserDashboard(userType, userId, email);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection when activity is destroyed
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }

    @Override
    public void onBackPressed() {
        // Navigate back to main activity
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
