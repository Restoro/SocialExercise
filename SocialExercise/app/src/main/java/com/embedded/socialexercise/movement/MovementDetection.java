package com.embedded.socialexercise.movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MovementDetection implements SensorEventListener {
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    LimitedSizeQueue<SensorData> timeWindow = new LimitedSizeQueue<>(50);

    private Movement currentPrediction = Movement.NONE;

    private final int PREDICTIONCOUNTER = 20;
    private final int UPDATES = 50;

    private int curPredCounter = PREDICTIONCOUNTER;

    private TextView viewToShow;

    public MovementDetection(SensorManager manager, TextView viewToShow) {
        this.mSensorManager = manager;
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.viewToShow = viewToShow;
    }

    public void onResume() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL );
    }

    public void onPause() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];
        float z = sensorEvent.values[2];
        double total = Math.sqrt(x * x + y * y + z * z);

        StringBuilder sb = new StringBuilder();
        //Get timestamp in millliseconds
        long timeStamp = System.currentTimeMillis();
        SensorData lastAddedData = timeWindow.getFirst();
        if(lastAddedData != null){
            //Update whole Time-Window every 2.5 sec
            if (timeStamp - lastAddedData.getTimeStamp() > UPDATES) {
                timeWindow.add(new SensorData(timeStamp, x, y, z));
                Map<String, Float> quartileMap = quartile(timeWindow.toArray(new SensorData[timeWindow.size()]));

                sb.append("Quartil 1:"+ quartileMap.get("Q1x") + "  " + quartileMap.get("Q1y") + "  " + quartileMap.get("Q1z") + "\n");
                sb.append("Current mean:" + quartileMap.get("Q2x") + "  " + quartileMap.get("Q2y") + "  " + quartileMap.get("Q2z") + "\n");
                sb.append("Quartil 3:"+ quartileMap.get("Q3x") + "  " + quartileMap.get("Q3y") + "  " + quartileMap.get("Q3z") + "\n");
                setPrediction(quartileMap);
                sb.append("Current Prediction:" + currentPrediction);
                viewToShow.setText(sb.toString());
            }
        } else {
            timeWindow.add(new SensorData(timeStamp, x, y, z));
        }

    }

    public Movement getPrediction() {
        return currentPrediction;
    }

    private void setPrediction(Map<String, Float> quartileMap) {
        Movement zwPrediction;
        if (isSitup(quartileMap)) {
            zwPrediction = Movement.SITUPS;
        } else if (isSquat(quartileMap)) {
            zwPrediction = Movement.SQUATS;
        } else {
            zwPrediction = Movement.NONE;
        }

        if(currentPrediction != zwPrediction) {
            curPredCounter--;
            if (curPredCounter == 0) {
                currentPrediction = zwPrediction;
                curPredCounter = PREDICTIONCOUNTER;
            }
        } else {
            curPredCounter = PREDICTIONCOUNTER;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private boolean inRange(float number, float range, float offset) {
        return range-offset <= number && number <= range+offset;
    }

    //Returns all 3 Quartiles for all 3 Parameters
    public Map<String, Float> quartile(SensorData[] values) {

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }

        // Rank order the values
        float[] vX = new float[values.length];
        float[] vY = new float[values.length];
        float[] vZ = new float[values.length];

        for (int i=0; i<values.length;i++) {
            vX[i] = values[i].getX();
            vY[i] = values[i].getY();
            vZ[i] = values[i].getZ();
        }
        Arrays.sort(vX);
        Arrays.sort(vY);
        Arrays.sort(vZ);

        int nQ1 = (int) Math.round(values.length * 25 / 100);
        int nQ2 = (int) Math.round(values.length * 50 / 100);
        int nQ3 = (int) Math.round(values.length * 70 / 100);

        Map<String, Float> returnMap = new HashMap<>();
        returnMap.put("Q1x",vX[nQ1]);
        returnMap.put("Q1y",vY[nQ1]);
        returnMap.put("Q1z",vZ[nQ1]);
        returnMap.put("Q2x",vX[nQ2]);
        returnMap.put("Q2y",vY[nQ2]);
        returnMap.put("Q2z",vZ[nQ2]);
        returnMap.put("Q3x",vX[nQ3]);
        returnMap.put("Q3y",vY[nQ3]);
        returnMap.put("Q3z",vZ[nQ3]);
        return returnMap;
    }

    //Not working as intendet!
    private boolean isSquat(Map<String, Float> quartileMap) {
        //Checks if 25% of the data is in range of zero in x-axis (No side movement in situps)
        boolean rangeQ1Y = inRange(quartileMap.get("Q1y"), 0, 2);
        boolean rangeQ2Z = inRange(quartileMap.get("Q2z"), 10, 2);
        boolean rangeQ3Y = inRange(quartileMap.get("Q3y"), 3, 2) && !inRange(quartileMap.get("Q1y"), quartileMap.get("Q3y"), 1f);
        return rangeQ1Y && rangeQ2Z  && rangeQ3Y;
    }

    //Checks if movement is situp
    private boolean isSitup(Map<String, Float> quartileMap) {
        //Checks if 25% of the data is in range of zero in x-axis (No side movement in situps)
        boolean rangeQ1X = inRange(quartileMap.get("Q1x"), 0, 2);
        //Checks if 25% of the data is smaller than 4 in z-axis (Should be a movement in )
        boolean rangeQ1Z = quartileMap.get("Q1z") < 3;

        //Same with Q1 in x-axis
        boolean rangeQ3X = inRange(quartileMap.get("Q3x"), 0, 2);

        //Checks if Q3 of z and y are approaching the same value (Movement consists of high y and low z and vice versa -> Same Q3 range)
        boolean rangeQ3YZ = inRange(quartileMap.get("Q3y"), quartileMap.get("Q3z"), 2) && quartileMap.get("Q3z") > 7;

        //All of the conditions should be true
        return rangeQ1X && rangeQ1Z && rangeQ3X && rangeQ3YZ;
    }
}


class SensorData {
    private long timeStamp;
    private float x;
    private float y;
    private float z;


    public SensorData(long timeStamp, float x, float y, float z) {
        this.timeStamp = timeStamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
