package com.example.shakeuppgift;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView xAxisTextView;
    private TextView yAxisTextView;
    private TextView zAxisTextView;
    private Switch sensorSwitch;
    private Button resetButton;
    private CheckBox toastCheckBox;

    private boolean sensorActive = false;
    private boolean showToastOnShake = false;

    // tröskelvärde för skakning
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private long mShakeTimestamp;
    private float last_x, last_y, last_z;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xAxisTextView = findViewById(R.id.xAxisTextView);
        yAxisTextView = findViewById(R.id.yAxisTextView);
        zAxisTextView = findViewById(R.id.zAxisTextView);
        sensorSwitch = findViewById(R.id.sensorSwitch);
        resetButton = findViewById(R.id.resetButton);
        toastCheckBox = findViewById(R.id.toastCheckBox);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Toast.makeText(this, "Accelerometer not available on this device.", Toast.LENGTH_LONG).show();
            sensorSwitch.setEnabled(false); // Inaktiverar switchen om sensorn saknas
        }

        sensorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sensorActive = isChecked;
            if (isChecked) {
                if (accelerometer != null) {
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                    Toast.makeText(this, "Accelerometer Activated", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (accelerometer != null) {
                    sensorManager.unregisterListener(this);
                }
                Toast.makeText(this, "Accelerometer Deactivated", Toast.LENGTH_SHORT).show();
                // Återställer TextViews när sensorn stängs av
                xAxisTextView.setText("X-Axis: ");
                yAxisTextView.setText("Y-Axis: ");
                zAxisTextView.setText("Z-Axis: ");
            }
        });

        resetButton.setOnClickListener(v -> {
            Toast.makeText(this, "Reset Button Clicked!", Toast.LENGTH_SHORT).show();
            last_x = 0; // Återställer skakningsdetektionsvariabler
            last_y = 0;
            last_z = 0;
            mShakeTimestamp = System.currentTimeMillis(); // Undviker direkt skakning efter reset
        });

        toastCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showToastOnShake = isChecked;
            if (isChecked) {
                Toast.makeText(this, "Shake Toasts Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Shake Toasts Disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensorActive && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            xAxisTextView.setText(String.format("X-Axis: %.2f", x));
            yAxisTextView.setText(String.format("Y-Axis: %.2f", y));
            zAxisTextView.setText(String.format("Z-Axis: %.2f", z));

            Log.d("Accelerometer", "X: " + x + ", Y: " + y + ", Z: " + z);


            // skakningsdetektering
            long now = System.currentTimeMillis();
            if ((now - mShakeTimestamp) > SHAKE_SLOP_TIME_MS) {
                float gX = x / SensorManager.GRAVITY_EARTH;
                float gY = y / SensorManager.GRAVITY_EARTH;
                float gZ = z / SensorManager.GRAVITY_EARTH;


                float gForce = (float)Math.sqrt(gX * gX + gY * gY + gZ * gZ);

                if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                    mShakeTimestamp = now;
                    String shakeMessage = String.format("Shake detected! Force: %.2f", gForce);
                    Log.d("SensorShake", shakeMessage);

                    if (showToastOnShake) {
                        Toast.makeText(this, shakeMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorActive && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (accelerometer != null) {
            sensorManager.unregisterListener(this);
        }
    }
}