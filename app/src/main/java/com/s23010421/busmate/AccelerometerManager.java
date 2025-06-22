package com.s23010421.busmate;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * AccelerometerManager for EEI4369 BusMate Project
 * Handles motion detection, shake gestures, and movement tracking
 * Author: V.P.N. Hansaka (S23010421)
 */
public class AccelerometerManager implements SensorEventListener {

    private static final String TAG = "AccelerometerManager";

    // Sensor constants for BusMate functionality
    private static final float SHAKE_THRESHOLD = 12.0f; // Emergency shake detection
    private static final float WALKING_THRESHOLD = 2.0f; // Walking to bus stop detection
    private static final float BUS_MOVEMENT_THRESHOLD = 1.5f; // Bus movement detection
    private static final int SHAKE_SLOP_TIME_MS = 1000; // 1 second between shakes
    private static final int WALKING_DETECTION_TIME = 3000; // 3 seconds for walking

    // Sensor components
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Context context;

    // Motion tracking variables
    private float lastX, lastY, lastZ;
    private long lastShakeTime = 0;
    private long lastMovementTime = 0;
    private boolean isWalking = false;
    private boolean isBusMoving = false;

    // Callback interface for BusMate activities
    public interface AccelerometerListener {
        void onShakeDetected(); // Emergency shake gesture
        void onWalkingDetected(boolean walking); // Walking to bus stop
        void onBusMovementDetected(boolean moving); // Bus movement
        void onAccelerometerChanged(float x, float y, float z); // Raw data
    }

    private AccelerometerListener listener;

    /**
     * Constructor for BusMate AccelerometerManager
     */
    public AccelerometerManager(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                Log.e(TAG, "Accelerometer not available on this device");
            } else {
                Log.d(TAG, "Accelerometer initialized successfully for BusMate");
            }
        }
    }

    /**
     * Set listener for accelerometer events
     */
    public void setAccelerometerListener(AccelerometerListener listener) {
        this.listener = listener;
    }

    /**
     * Start accelerometer monitoring for BusMate features
     */
    public boolean startListening() {
        if (sensorManager != null && accelerometer != null) {
            boolean result = sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Accelerometer listening started: " + result);
            return result;
        }
        return false;
    }

    /**
     * Stop accelerometer monitoring
     */
    public void stopListening() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Accelerometer listening stopped");
        }
    }

    /**
     * Check if accelerometer is available
     */
    public boolean isAccelerometerAvailable() {
        return accelerometer != null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long currentTime = System.currentTimeMillis();

            // Calculate acceleration magnitude
            float accelerationMagnitude = (float) Math.sqrt(x * x + y * y + z * z);

            // 1. EMERGENCY SHAKE DETECTION for BusMate panic button
            detectShakeGesture(x, y, z, currentTime);

            // 2. WALKING DETECTION for bus stop navigation
            detectWalkingMovement(accelerationMagnitude, currentTime);

            // 3. BUS MOVEMENT DETECTION for live tracking
            detectBusMovement(accelerationMagnitude, currentTime);

            // 4. Send raw data to listener
            if (listener != null) {
                listener.onAccelerometerChanged(x, y, z);
            }

            // Update last values
            lastX = x;
            lastY = y;
            lastZ = z;
            lastMovementTime = currentTime;
        }
    }

    /**
     * Detect shake gesture for emergency alert (BusMate Feature)
     */
    private void detectShakeGesture(float x, float y, float z, long currentTime) {
        // Calculate change in acceleration
        float deltaX = Math.abs(x - lastX);
        float deltaY = Math.abs(y - lastY);
        float deltaZ = Math.abs(z - lastZ);

        float deltaAcceleration = deltaX + deltaY + deltaZ;

        // Check if shake threshold exceeded and enough time passed
        if (deltaAcceleration > SHAKE_THRESHOLD &&
                (currentTime - lastShakeTime) > SHAKE_SLOP_TIME_MS) {

            Log.d(TAG, "EMERGENCY SHAKE DETECTED! Magnitude: " + deltaAcceleration);
            lastShakeTime = currentTime;

            if (listener != null) {
                listener.onShakeDetected();
            }
        }
    }

    /**
     * Detect walking movement for bus stop navigation
     */
    private void detectWalkingMovement(float accelerationMagnitude, long currentTime) {
        // Remove gravity (9.8 m/s²) to get linear acceleration
        float linearAcceleration = Math.abs(accelerationMagnitude - 9.8f);

        boolean currentlyWalking = linearAcceleration > WALKING_THRESHOLD;

        // Check for state change
        if (currentlyWalking != isWalking) {
            isWalking = currentlyWalking;

            Log.d(TAG, "Walking state changed: " + (isWalking ? "STARTED" : "STOPPED"));

            if (listener != null) {
                listener.onWalkingDetected(isWalking);
            }
        }
    }

    /**
     * Detect bus movement for live tracking accuracy
     */
    private void detectBusMovement(float accelerationMagnitude, long currentTime) {
        // Detect smooth movement patterns typical of bus travel
        float smoothedAcceleration = Math.abs(accelerationMagnitude - 9.8f);

        boolean currentlyMoving = smoothedAcceleration > BUS_MOVEMENT_THRESHOLD &&
                smoothedAcceleration < WALKING_THRESHOLD;

        // Check for state change
        if (currentlyMoving != isBusMoving) {
            isBusMoving = currentlyMoving;

            Log.d(TAG, "Bus movement state changed: " + (isBusMoving ? "MOVING" : "STOPPED"));

            if (listener != null) {
                listener.onBusMovementDetected(isBusMoving);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Accelerometer accuracy changed: " + accuracy);
    }

    /**
     * Get current walking state
     */
    public boolean isWalking() {
        return isWalking;
    }

    /**
     * Get current bus movement state
     */
    public boolean isBusMoving() {
        return isBusMoving;
    }
}
