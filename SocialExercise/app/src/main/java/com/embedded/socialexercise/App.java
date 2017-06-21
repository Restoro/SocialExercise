package com.embedded.socialexercise;

import android.app.Application;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.embedded.socialexercise.movement.MovementDetection;
import com.embedded.socialexercise.position.PositionDetection;

public class App extends Application {
    private PositionDetection positionDetection;
    private MovementDetection movementDetection;
    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }


    public static synchronized App getInstance() {
        return mInstance;
    }

    public PositionDetection getPositionDetectionInstance() {
        if(positionDetection == null)
            positionDetection = new PositionDetection(mInstance);
        return positionDetection;
    }

    public MovementDetection getMovementDetectionInstance(TextView view) {
        if(movementDetection == null)
            movementDetection = new MovementDetection((SensorManager)getSystemService(SENSOR_SERVICE), view);
        return movementDetection;
    }

    public static PositionDetection getPositionDetection() {
        return getInstance().getPositionDetectionInstance();
    }

    public static MovementDetection getMovementDetection(TextView view) {
        return  getInstance().getMovementDetectionInstance(view);
    }
}
