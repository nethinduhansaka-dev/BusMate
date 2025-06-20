package com.s23010421.busmate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * FIXED: DatabaseHelper class with enhanced error handling and missing methods
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database configuration constants
    public static final String DATABASE_NAME = "BusMate.db";
    public static final int DATABASE_VERSION = 3; // UPDATED: Increment version for schema changes

    // User authentication table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_USER_TYPE = "user_type";
    public static final String COL_CREATED_AT = "created_at";
    public static final String COL_IS_VERIFIED = "is_verified";

    // Passenger profile table
    public static final String TABLE_PASSENGERS = "passengers";
    public static final String COL_PASSENGER_ID = "passenger_id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_PHONE = "phone_number";
    public static final String COL_DATE_OF_BIRTH = "date_of_birth";
    public static final String COL_GENDER = "gender";
    public static final String COL_ADDRESS = "address";
    public static final String COL_EMERGENCY_CONTACT = "emergency_contact";
    public static final String COL_EMERGENCY_PHONE = "emergency_phone";
    public static final String COL_BLOOD_TYPE = "blood_type";

    // Bus operator profile table
    public static final String TABLE_BUS_OPERATORS = "bus_operators";
    public static final String COL_OPERATOR_ID = "operator_id";
    public static final String COL_LICENSE_NUMBER = "license_number";
    public static final String COL_VEHICLE_REGISTRATION = "vehicle_registration";
    public static final String COL_ROUTE_NUMBER = "route_number";
    public static final String COL_YEARS_EXPERIENCE = "years_experience";
    public static final String COL_VEHICLE_TYPE = "vehicle_type";
    public static final String COL_OPERATING_COMPANY = "operating_company";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DatabaseHelper constructor called");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        try {
            // Create users table for authentication
            String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    COL_PASSWORD + " TEXT NOT NULL, " +
                    COL_USER_TYPE + " TEXT NOT NULL, " +
                    COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    COL_IS_VERIFIED + " INTEGER DEFAULT 0)";

            db.execSQL(createUsersTable);
            Log.d(TAG, "Users table created successfully");

            // Create passengers profile table
            String createPassengersTable = "CREATE TABLE " + TABLE_PASSENGERS + " (" +
                    COL_PASSENGER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_ID + " INTEGER, " +
                    COL_FULL_NAME + " TEXT NOT NULL, " +
                    COL_PHONE + " TEXT, " +
                    COL_DATE_OF_BIRTH + " TEXT, " +
                    COL_GENDER + " TEXT, " +
                    COL_ADDRESS + " TEXT, " +
                    COL_EMERGENCY_CONTACT + " TEXT, " +
                    COL_EMERGENCY_PHONE + " TEXT, " +
                    COL_BLOOD_TYPE + " TEXT, " +
                    "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";

            db.execSQL(createPassengersTable);
            Log.d(TAG, "Passengers table created successfully");

            // FIXED: Create bus operators profile table with proper schema
            String createOperatorsTable = "CREATE TABLE " + TABLE_BUS_OPERATORS + " (" +
                    COL_OPERATOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_ID + " INTEGER, " +
                    COL_FULL_NAME + " TEXT NOT NULL, " +
                    COL_PHONE + " TEXT, " +
                    COL_DATE_OF_BIRTH + " TEXT, " +
                    COL_GENDER + " TEXT, " +
                    COL_ADDRESS + " TEXT, " +
                    COL_LICENSE_NUMBER + " TEXT NOT NULL, " +
                    COL_VEHICLE_REGISTRATION + " TEXT NOT NULL, " +
                    COL_ROUTE_NUMBER + " TEXT, " +
                    COL_YEARS_EXPERIENCE + " INTEGER, " +
                    COL_VEHICLE_TYPE + " TEXT, " +
                    COL_OPERATING_COMPANY + " TEXT, " +
                    COL_EMERGENCY_CONTACT + " TEXT, " +
                    COL_EMERGENCY_PHONE + " TEXT, " +
                    "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";

            db.execSQL(createOperatorsTable);
            Log.d(TAG, "Bus operators table created successfully");

            Log.d(TAG, "All database tables created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Drop existing tables and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUS_OPERATORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASSENGERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        onCreate(db);
    }

    /**
     * FIXED: Enhanced user insertion with better error handling
     */
    public long insertUser(String email, String password, String userType) {
        Log.d(TAG, "Attempting to insert user: " + email + ", type: " + userType);

        SQLiteDatabase db = null;
        long userId = -1;

        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_EMAIL, email.toLowerCase().trim());
            values.put(COL_PASSWORD, password);
            values.put(COL_USER_TYPE, userType);
            values.put(COL_IS_VERIFIED, 0);

            userId = db.insert(TABLE_USERS, null, values);

            if (userId != -1) {
                Log.d(TAG, "User inserted successfully with ID: " + userId);
            } else {
                Log.e(TAG, "Failed to insert user");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inserting user: " + e.getMessage(), e);
            userId = -1;
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return userId;
    }

    /**
     * FIXED: Enhanced email existence check
     */
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            Log.d(TAG, "Email is null or empty");
            return false;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;
        boolean exists = false;

        try {
            db = this.getReadableDatabase();
            String normalizedEmail = email.toLowerCase().trim();

            String query = "SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?";
            cursor = db.rawQuery(query, new String[]{normalizedEmail});

            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                exists = count > 0;
                Log.d(TAG, "Email check for '" + normalizedEmail + "': count = " + count);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error checking email existence: " + e.getMessage(), e);
            exists = false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        Log.d(TAG, "Email '" + email + "' exists: " + exists);
        return exists;
    }

    /**
     * FIXED: Enhanced authentication with proper error handling
     */
    public Cursor authenticateUser(String email, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            if (db == null) {
                Log.e(TAG, "Database is null");
                return null;
            }

            String normalizedEmail = email.toLowerCase().trim();
            String query = "SELECT * FROM " + TABLE_USERS +
                    " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?";

            cursor = db.rawQuery(query, new String[]{normalizedEmail, password});

            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "User authentication successful for: " + normalizedEmail);
                return cursor; // Don't close cursor here, let caller handle it
            } else {
                Log.d(TAG, "User authentication failed for: " + normalizedEmail);
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage(), e);
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        // Don't close db here as cursor needs it
    }

    /**
     * FIXED: Enhanced passenger profile insertion
     */
    public boolean insertPassengerProfile(long userId, String fullName, String phone,
                                          String dateOfBirth, String gender, String address,
                                          String emergencyContact, String emergencyPhone,
                                          String bloodType) {

        Log.d(TAG, "Attempting to insert passenger profile for user ID: " + userId);

        SQLiteDatabase db = null;
        boolean success = false;

        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_USER_ID, userId);
            values.put(COL_FULL_NAME, fullName);
            values.put(COL_PHONE, phone);
            values.put(COL_DATE_OF_BIRTH, dateOfBirth);
            values.put(COL_GENDER, gender);
            values.put(COL_ADDRESS, address);
            values.put(COL_EMERGENCY_CONTACT, emergencyContact);
            values.put(COL_EMERGENCY_PHONE, emergencyPhone);
            values.put(COL_BLOOD_TYPE, bloodType);

            long result = db.insert(TABLE_PASSENGERS, null, values);
            success = result != -1;

            if (success) {
                Log.d(TAG, "Passenger profile inserted successfully with ID: " + result);
            } else {
                Log.e(TAG, "Failed to insert passenger profile");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inserting passenger profile: " + e.getMessage(), e);
            success = false;
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return success;
    }

    /**
     * FIXED: MISSING METHOD - Insert bus operator profile
     */
    public boolean insertBusOperatorProfile(long userId, String fullName, String phone,
                                            String dateOfBirth, String gender, String address,
                                            String licenseNumber, String vehicleRegistration,
                                            String routeNumber, int yearsExperience, String vehicleType,
                                            String operatingCompany, String emergencyContact,
                                            String emergencyPhone) {

        Log.d(TAG, "Attempting to insert bus operator profile for user ID: " + userId);

        SQLiteDatabase db = null;
        boolean success = false;

        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(COL_USER_ID, userId);
            values.put(COL_FULL_NAME, fullName);
            values.put(COL_PHONE, phone);
            values.put(COL_DATE_OF_BIRTH, dateOfBirth);
            values.put(COL_GENDER, gender);
            values.put(COL_ADDRESS, address);
            values.put(COL_LICENSE_NUMBER, licenseNumber);
            values.put(COL_VEHICLE_REGISTRATION, vehicleRegistration);
            values.put(COL_ROUTE_NUMBER, routeNumber);
            values.put(COL_YEARS_EXPERIENCE, yearsExperience);
            values.put(COL_VEHICLE_TYPE, vehicleType);
            values.put(COL_OPERATING_COMPANY, operatingCompany);
            values.put(COL_EMERGENCY_CONTACT, emergencyContact);
            values.put(COL_EMERGENCY_PHONE, emergencyPhone);

            long result = db.insert(TABLE_BUS_OPERATORS, null, values);
            success = result != -1;

            if (success) {
                Log.d(TAG, "Bus operator profile inserted successfully with ID: " + result);
            } else {
                Log.e(TAG, "Failed to insert bus operator profile");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inserting bus operator profile: " + e.getMessage(), e);
            success = false;
        } finally {
            if (db != null) {
                db.close();
            }
        }

        return success;
    }

    /**
     * FIXED: Enhanced getPassengerProfile method
     */
    public Cursor getPassengerProfile(long userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            if (db == null) {
                Log.e(TAG, "Database is null");
                return null;
            }

            String query = "SELECT p.*, u." + COL_EMAIL + ", u." + COL_USER_TYPE + " FROM " + TABLE_PASSENGERS + " p " +
                    "JOIN " + TABLE_USERS + " u ON p." + COL_USER_ID + " = u." + COL_USER_ID +
                    " WHERE p." + COL_USER_ID + " = ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            Log.d(TAG, "Query executed for user ID: " + userId + ", cursor count: " +
                    (cursor != null ? cursor.getCount() : "null"));

            return cursor;

        } catch (Exception e) {
            Log.e(TAG, "Error getting passenger profile: " + e.getMessage(), e);
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        // Don't close db here as cursor needs it
    }

    /**
     * FIXED: Enhanced getBusOperatorProfile method
     */
    public Cursor getBusOperatorProfile(long userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            if (db == null) {
                Log.e(TAG, "Database is null");
                return null;
            }

            String query = "SELECT o.*, u." + COL_EMAIL + ", u." + COL_USER_TYPE + " FROM " + TABLE_BUS_OPERATORS + " o " +
                    "JOIN " + TABLE_USERS + " u ON o." + COL_USER_ID + " = u." + COL_USER_ID +
                    " WHERE o." + COL_USER_ID + " = ?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            Log.d(TAG, "Bus operator query executed for user ID: " + userId + ", cursor count: " +
                    (cursor != null ? cursor.getCount() : "null"));

            return cursor;

        } catch (Exception e) {
            Log.e(TAG, "Error getting bus operator profile: " + e.getMessage(), e);
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        // Don't close db here as cursor needs it
    }

    public boolean updateUserPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_PASSWORD, newPassword);

        int rowsAffected = db.update(TABLE_USERS, values,
                COL_EMAIL + " = ?",
                new String[]{email.toLowerCase().trim()});

        db.close();
        return rowsAffected > 0;
    }

    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_USERS;
        Cursor cursor = db.rawQuery(query, null);

        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }

        db.close();
        Log.d(TAG, "Total users in database: " + count);
        return count;
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS;
        return db.rawQuery(query, null);
    }
}
