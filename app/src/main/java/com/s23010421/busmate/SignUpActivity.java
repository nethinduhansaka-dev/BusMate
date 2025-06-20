package com.s23010421.busmate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

/**
 * SignUpActivity handles passenger registration for BusMate
 * FIXED: insertUser method call with correct parameters
 */
public class SignUpActivity extends AppCompatActivity {

    // UI component declarations for all form sections
    private EditText editTextFullName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextDateOfBirth;
    private RadioGroup radioGroupGender;
    private EditText editTextPhone;
    private EditText editTextAddress;

    // Emergency contact fields
    private EditText editTextEmergencyContactName;
    private EditText editTextEmergencyContactPhone;
    private Spinner spinnerBloodType;
    private EditText editTextMedicalConditions;

    // Special assistance checkboxes
    private CheckBox checkBoxWheelchair;
    private CheckBox checkBoxGuideDog;
    private CheckBox checkBoxHearingImpaired;

    // Registration button
    private Button buttonRegister;
    private Button buttonBackToSelection;

    // Database helper
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Setup spinners
        setupSpinners();
    }

    /**
     * Initialize all UI components
     */
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

        // Emergency & Safety
        editTextEmergencyContactName = findViewById(R.id.editTextEmergencyContactName);
        editTextEmergencyContactPhone = findViewById(R.id.editTextEmergencyContactPhone);
        spinnerBloodType = findViewById(R.id.spinnerBloodType);
        editTextMedicalConditions = findViewById(R.id.editTextMedicalConditions);

        // Special Assistance
        checkBoxWheelchair = findViewById(R.id.checkBoxWheelchair);
        checkBoxGuideDog = findViewById(R.id.checkBoxGuideDog);
        checkBoxHearingImpaired = findViewById(R.id.checkBoxHearingImpaired);

        // Buttons
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonBackToSelection = findViewById(R.id.buttonBackToSelection);
    }

    /**
     * Set up click listeners for interactive elements
     */
    private void setupClickListeners() {
        // Date of birth picker
        editTextDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Register button click listener
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });

        // Back to selection button
        if (buttonBackToSelection != null) {
            buttonBackToSelection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SignUpActivity.this, UserTypeSelectionActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    /**
     * Setup spinner adapters
     */
    private void setupSpinners() {
        // Blood type spinner
        String[] bloodTypes = {"Select Blood Type", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> bloodTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bloodTypes);
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(bloodTypeAdapter);
    }

    /**
     * Show date picker dialog for date of birth selection
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        // Format and set selected date
                        String selectedDate = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                        editTextDateOfBirth.setText(selectedDate);
                    }
                },
                year, month, day
        );

        // Set minimum age requirement (13 years for passengers)
        calendar.add(Calendar.YEAR, -13);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    /**
     * FIXED: Attempt passenger registration with correct insertUser method call
     */
    private void attemptRegistration() {
        // Get form values
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        String dateOfBirth = editTextDateOfBirth.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String emergencyContactName = editTextEmergencyContactName.getText().toString().trim();
        String emergencyContactPhone = editTextEmergencyContactPhone.getText().toString().trim();
        String medicalConditions = editTextMedicalConditions.getText().toString().trim();

        // Get selected gender
        String gender = "";
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            RadioButton selectedGenderButton = findViewById(selectedGenderId);
            gender = selectedGenderButton.getText().toString();
        }

        // Get blood type
        String bloodType = spinnerBloodType.getSelectedItem().toString();
        if ("Select Blood Type".equals(bloodType)) {
            bloodType = "";
        }

        // Validate all form fields
        if (!validateRegistrationForm(fullName, email, password, confirmPassword,
                dateOfBirth, gender, phone, address,
                emergencyContactName, emergencyContactPhone)) {
            return;
        }

        // Show loading state
        buttonRegister.setEnabled(false);
        buttonRegister.setText("Creating Account...");

        // Create final variables for inner class access
        final String finalFullName = fullName;
        final String finalEmail = email;
        final String finalPassword = password;
        final String finalDateOfBirth = dateOfBirth;
        final String finalGender = gender;
        final String finalPhone = phone;
        final String finalAddress = address;
        final String finalEmergencyContactName = emergencyContactName;
        final String finalEmergencyContactPhone = emergencyContactPhone;
        final String finalBloodType = bloodType;

        // Perform registration in background thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Simulate network delay
                    Thread.sleep(1500);

                    // Check if email already exists
                    boolean emailExists = databaseHelper.isEmailExists(finalEmail);

                    if (emailExists) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonRegister.setEnabled(true);
                                buttonRegister.setText("Create Account");
                                Toast.makeText(SignUpActivity.this,
                                        "Email already exists. Please use a different email.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }

                    // FIXED: Insert user account with user_type parameter (passenger)
                    long userId = databaseHelper.insertUser(finalEmail, finalPassword, "passenger");

                    if (userId != -1) {
                        // Insert passenger profile
                        boolean profileInserted = databaseHelper.insertPassengerProfile(
                                userId, finalFullName, finalPhone, finalDateOfBirth, finalGender, finalAddress,
                                finalEmergencyContactName, finalEmergencyContactPhone, finalBloodType
                        );

                        final boolean registrationSuccess = profileInserted;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonRegister.setEnabled(true);
                                buttonRegister.setText("Create Account");

                                if (registrationSuccess) {
                                    Toast.makeText(SignUpActivity.this,
                                            "Registration successful! Please sign in.",
                                            Toast.LENGTH_LONG).show();

                                    // Navigate to sign in screen
                                    Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                    intent.putExtra("registered_email", finalEmail);
                                    intent.putExtra("user_type", "passenger");
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(SignUpActivity.this,
                                            "Registration failed. Please try again.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buttonRegister.setEnabled(true);
                                buttonRegister.setText("Create Account");
                                Toast.makeText(SignUpActivity.this,
                                        "Registration failed. Please try again.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (InterruptedException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            buttonRegister.setEnabled(true);
                            buttonRegister.setText("Create Account");
                            Toast.makeText(SignUpActivity.this,
                                    "Registration failed. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Validate passenger registration form
     */
    private boolean validateRegistrationForm(String fullName, String email, String password,
                                             String confirmPassword, String dateOfBirth, String gender,
                                             String phone, String address, String emergencyContactName,
                                             String emergencyContactPhone) {
        boolean isValid = true;

        // Reset previous error messages
        editTextFullName.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);
        editTextDateOfBirth.setError(null);
        editTextPhone.setError(null);
        editTextAddress.setError(null);
        editTextEmergencyContactName.setError(null);
        editTextEmergencyContactPhone.setError(null);

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

        return isValid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}