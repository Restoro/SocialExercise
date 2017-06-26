package com.embedded.socialexercise;

import android.app.Application;
import android.hardware.SensorManager;

import com.embedded.socialexercise.movement.MovementDetection;
import com.embedded.socialexercise.mqtt.MqttDetection;
import com.embedded.socialexercise.person.ProfileDetection;
import com.embedded.socialexercise.position.PositionDetection;

public class App extends Application {
    private PositionDetection positionDetection;
    private MovementDetection movementDetection;
    private MqttDetection mqttDetection;
    private ProfileDetection profileDetection;
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

    public MovementDetection getMovementDetectionInstance() {
        if(movementDetection == null)
            movementDetection = new MovementDetection(mInstance,(SensorManager)getSystemService(SENSOR_SERVICE));
        return movementDetection;
    }

    public MqttDetection getMqttDetectionInstance(){
        if(mqttDetection == null)
            mqttDetection = new MqttDetection(mInstance);
        else if(!mqttDetection.isConnected())
            mqttDetection.connect();
        return mqttDetection;
    }

    public ProfileDetection getProfileDetectionInstance() {
        if (profileDetection == null)
            profileDetection = new ProfileDetection();
        return profileDetection;
    }
    public static PositionDetection getPositionDetection() {
        return getInstance().getPositionDetectionInstance();
    }

    public static MovementDetection getMovementDetection() {
        return  getInstance().getMovementDetectionInstance();
    }

    public static MqttDetection getMqttDetection() {
        return  getInstance().getMqttDetectionInstance();
    }

    public static ProfileDetection getProfileDetection() {
        return  getInstance().getProfileDetectionInstance();
    }


}
