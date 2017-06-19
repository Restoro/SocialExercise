package com.embedded.socialexercise;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.embedded.socialexercise.movement.MovementDetection;

public class MainActivity extends AppCompatActivity {
    MovementDetection detection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detection = new MovementDetection((SensorManager)getSystemService(SENSOR_SERVICE), (TextView)findViewById(R.id.text_view));
    }

    @Override
    protected void onResume() {
        super.onResume();
        detection.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detection.onPause();
    }
}
