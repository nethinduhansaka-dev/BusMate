package com.s23010421.busmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

/**
 * FIXED: BusOperatorRegistrationActivity with enhanced error handling
 */
public class BusOperatorRegistrationActivity extends AppCompatActivity {

    private static final String TAG = "BusOperatorReg";

    // UI components
    private EditText editTextFullName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextDateOfBirth;
    private RadioGroup radioGroupGender;
    private EditText editTextPhone;
    private EditText editTextAddress;
    private EditText editTextLicenseNumber;
    private EditText editTextVehicleRegistration;
    private EditText editTextRouteNumber;
    private EditText editTextYearsExperience;
    private Spinner spinnerVehicleType;
    private EditText editTextOperatingCompany;
    private EditText editTextEmergencyContactName;
    private EditText editTextEmergencyContactPhone;
    private Button buttonRegister;
    private Button buttonBackToSelection;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_operator_registration);

        Log.d(TAG, "BusOperatorRegistrationActivity created");

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Test database connection
        testDatabaseConnection();

        // Initialize UI components
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Setup spinners
        setupSpinners();
    }

    /**
     * ADDED: Test database connection on activity start
     */
    private void testDatabaseConnection() {
        try {
            int userCount = databaseHelper.getUserCount();
            Log.d(TAG, "Database connection test successful. User count: " + userCount);
        } catch (Exception e) {
            Log.e(TAG, "Database connection test failed: " + e.getMessage(), e);
            Toast.makeText(this, "Database initialization error. Please restart the app.", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        // Personal Information
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextAddress = findViewById(R.id.editTextAddress);

        // Professional Information
        editTextLicenseNumber = findViewById(R.id.editTextLicenseNumber);
        editTextVehicleRegistration = findViewById(R.id.editTextVehicleRegistration);
        editTextRouteNumber = findViewById(R.id.editTextRouteNumber);
        editTextYearsExperience = findViewById(R.id.editTextYearsExperience);
        spinnerVehicleType = findViewById(R.id.spinnerVehicleType);
        editTextOperatingCompany = findViewById(R.id.editTextOperatingCompany);

        // Emergency Contact
        editTextEmergencyContactName = findViewById(R.id.editTextEmergencyContactName);
        editTextEmergencyContactPhone = findViewById(R.id.editTextEmergencyContactPhone);

        // Buttons
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBackToSelection = findViewById(R.id.buttonBackToSelection);

        Log.d(TAG, "Views initialized successfully");
    }

    private void setupClickListeners() {
        editTextDateOfBirth.setOnClickListener(v -> showDatePickerDialog());
        buttonRegister.setOnClickListener(v -> attemptRegistration());

        if (buttonBackToSelection != null) {
            buttonBackToSelection.setOnClickListener(v -> {
                Intent intent = new Intent(BusOperatorRegistrationActivity.this, UserTypeSelectionActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupSpinners() {
        String[] vehicleTypes = {"Select Vehicle Type", "City Bus", "Intercity Bus", "Express Bus", "School Bus", "Tourist Bus", "Mini Bus"};
        ArrayAdapter<String> vehicleTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, vehicleTypes);
        vehicleTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(vehicleTypeAdapter);
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                    editTextDateOfBirth.setText(selectedDate);
                },
                year, month, day
        );

        calendar.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    /**
     * FIXED: Enhanced registration with comprehensive error handling and debugging
     */
    private void attemptRegistration() {
        Log.d(TAG, "Registration attempt started");

        // Get form values
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        String dateOfBirth = editTextDateOfBirth.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String licenseNumber = editTextLicenseNumber.getText().toString().trim();
        String vehicleRegistration = editTextVehicleRegistration.getText().toString().trim();
        String routeNumber = editTextRouteNumber.getText().toString().trim();
        String yearsExperience = editTextYearsExperience.getText().toString().trim();
        String operatingCompany = editTextOperatingCompany.getText().toString().trim();
        String emergencyContactName = editTextEmergencyContactName.getText().toString().trim();
        String emergencyContactPhone = editTextEmergencyContactPhone.getText().toString().trim();

        // Get selected gender
        String gender = "";
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedGenderButton = findViewById(selectedGenderId);
            gender = selectedGenderButton.getText().toString();
        }

        // Get vehicle type
        String vehicleType = spinnerVehicleType.getSelectedItem().toString();
        if ("Select Vehicle Type".equals(vehicleType)) {
            vehicleType = "";
        }

        Log.d(TAG, "Form data collected: email=" + email + ", fullName=" + fullName);

        // Validate all form fields
        if (!validateRegistrationForm(fullName, email, password, confirmPassword,
                dateOfBirth, gender, phone, address,
                licenseNumber, vehicleRegistration, routeNumber,
                yearsExperience, vehicleType, operatingCompany,
                emergencyContactName, emergencyContactPhone)) {
            Log.d(TAG, "Form validation failed");
            return;
        }

        // Create final variables for inner class access
        final String finalFullName = fullName;
        final String finalEmail = email;
        final String finalPassword = password;
        final String finalDateOfBirth = dateOfBirth;
        final String finalGender = gender;
        final String finalPhone = phone;
        final String finalAddress = address;
        final String finalLicenseNumber = licenseNumber;
        final String finalVehicleRegistration = vehicleRegistration;
        final String finalRouteNumber = routeNumber;
        final String finalYearsExperience = yearsExperience;
        final String finalVehicleType = vehicleType;
        final String finalOperatingCompany = operatingCompany;
        final String finalEmergencyContactName = emergencyContactName;
        final String finalEmergencyContactPhone = emergencyContactPhone;

        // Show loading state
        buttonRegister.setEnabled(false);
        buttonRegister.setText("Creating Account...");

        Log.d(TAG, "Starting registration process...");

        // Perform registration in background thread
        new Thread(() -> {
            try {
                Log.d(TAG, "Background registration thread started");

                // Simulate network delay
                Thread.sleep(1000);

                // Check if email already exists
                boolean emailExists = databaseHelper.isEmailExists(finalEmail);
                Log.d(TAG, "Email exists check: " + emailExists);

                if (emailExists) {
                    runOnUiThread(() -> {
                        buttonRegister.setEnabled(true);
                        buttonRegister.setText("Create Account");
                        Toast.makeText(BusOperatorRegistrationActivity.this,
                                "Email already exists. Please use a different email.",
                                Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                // Insert user account
                long userId = databaseHelper.insertUser(finalEmail, finalPassword, "bus_operator");
                Log.d(TAG, "User insertion result: userId = " + userId);

                if (userId != -1) {
                    // Parse years of experience
                    int years = 0;
                    try {
                        years = Integer.parseInt(finalYearsExperience);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Invalid years format: " + finalYearsExperience, e);
                        years = 0;
                    }

                    // Insert bus operator profile
                    boolean profileInserted = databaseHelper.insertBusOperatorProfile(
                            userId, finalFullName, finalPhone, finalDateOfBirth, finalGender, finalAddress,
                            finalLicenseNumber, finalVehicleRegistration, finalRouteNumber,
                            years, finalVehicleType, finalOperatingCompany,
                            finalEmergencyContactName, finalEmergencyContactPhone
                    );

                    Log.d(TAG, "Profile insertion result: " + profileInserted);

                    final boolean registrationSuccess = profileInserted;

                    runOnUiThread(() -> {
                        buttonRegister.setEnabled(true);
                        buttonRegister.setText("Create Account");

                        if (registrationSuccess) {
                            Log.d(TAG, "Registration successful!");
                            Toast.makeText(BusOperatorRegistrationActivity.this,
                                    "Registration successful! Please sign in.",
                                    Toast.LENGTH_LONG).show();

                            // Navigate to sign in screen
                            Intent intent = new Intent(BusOperatorRegistrationActivity.this, SignInActivity.class);
                            intent.putExtra("registered_email", finalEmail);
                            intent.putExtra("user_type", "bus_operator");
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "Profile insertion failed");
                            Toast.makeText(BusOperatorRegistrationActivity.this,
                                    "Registration failed during profile creation. Please try again.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Log.e(TAG, "User insertion failed");
                    runOnUiThread(() -> {
                        buttonRegister.setEnabled(true);
                        buttonRegister.setText("Create Account");
                        Toast.makeText(BusOperatorRegistrationActivity.this,
                                "Registration failed during account creation. Please try again.",
                                Toast.LENGTH_LONG).show();
                    });
                }

            } catch (InterruptedException e) {
                Log.e(TAG, "Registration thread interrupted", e);
                runOnUiThread(() -> {
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("Create Account");
                    Toast.makeText(BusOperatorRegistrationActivity.this,
                            "Registration process interrupted. Please try again.",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error during registration", e);
                runOnUiThread(() -> {
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("Create Account");
                    Toast.makeText(BusOperatorRegistrationActivity.this,
                            "Registration failed due to unexpected error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    /**
     * Enhanced form validation with detailed error reporting
     */
    private boolean validateRegistrationForm(String fullName, String email, String password,
                                             String confirmPassword, String dateOfBirth, String gender,
                                             String phone, String address, String licenseNumber,
                                             String vehicleRegistration, String routeNumber,
                                             String yearsExperience, String vehicleType,
                                             String operatingCompany, String emergencyContactName,
                                             String emergencyContactPhone) {
        boolean isValid = true;

        // Reset previous error messages
        clearAllErrors();

        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            editTextFullName.setError("Full name is required");
            if (isValid) editTextFullName.requestFocus();
            isValid = false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            if (isValid) editTextEmail.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email address");
            if (isValid) editTextEmail.requestFocus();
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            if (isValid) editTextPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            if (isValid) editTextPassword.requestFocus();
            isValid = false;
        }

        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Please confirm your password");
            if (isValid) editTextConfirmPassword.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            if (isValid) editTextConfirmPassword.requestFocus();
            isValid = false;
        }

        // Validate date of birth
        if (TextUtils.isEmpty(dateOfBirth)) {
            editTextDateOfBirth.setError("Date of birth is required");
            if (isValid) editTextDateOfBirth.requestFocus();
            isValid = false;
        }

        // Validate gender
        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            if (isValid) radioGroupGender.requestFocus();
            isValid = false;
        }

        // Validate phone
        if (TextUtils.isEmpty(phone)) {
            editTextPhone.setError("Phone number is required");
            if (isValid) editTextPhone.requestFocus();
            isValid = false;
        }

        // Validate license number
        if (TextUtils.isEmpty(licenseNumber)) {
            editTextLicenseNumber.setError("License number is required");
            if (isValid) editTextLicenseNumber.requestFocus();
            isValid = false;
        }

        // Validate vehicle registration
        if (TextUtils.isEmpty(vehicleRegistration)) {
            editTextVehicleRegistration.setError("Vehicle registration is required");
            if (isValid) editTextVehicleRegistration.requestFocus();
            isValid = false;
        }

        // Validate years of experience
        if (TextUtils.isEmpty(yearsExperience)) {
            editTextYearsExperience.setError("Years of experience is required");
            if (isValid) editTextYearsExperience.requestFocus();
            isValid = false;
        } else {
            try {
                int years = Integer.parseInt(yearsExperience);
                if (years < 0) {
                    editTextYearsExperience.setError("Please enter valid years of experience");
                    if (isValid) editTextYearsExperience.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                editTextYearsExperience.setError("Please enter a valid number");
                if (isValid) editTextYearsExperience.requestFocus();
                isValid = false;
            }
        }

        // Validate vehicle type
        if (TextUtils.isEmpty(vehicleType)) {
            Toast.makeText(this, "Please select vehicle type", Toast.LENGTH_SHORT).show();
            if (isValid) spinnerVehicleType.requestFocus();
            isValid = false;
        }

        Log.d(TAG, "Form validation result: " + isValid);
        return isValid;
    }

    private void clearAllErrors() {
        editTextFullName.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);
        editTextDateOfBirth.setError(null);
        editTextPhone.setError(null);
        editTextAddress.setError(null);
        editTextLicenseNumber.setError(null);
        editTextVehicleRegistration.setError(null);
        editTextRouteNumber.setError(null);
        editTextYearsExperience.setError(null);
        editTextOperatingCompany.setError(null);
        editTextEmergencyContactName.setError(null);
        editTextEmergencyContactPhone.setError(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
