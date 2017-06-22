package com.embedded.socialexercise.movement;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import com.embedded.socialexercise.movement.enums.Movement;
import com.embedded.socialexercise.movement.exercise.Exercise;
import com.embedded.socialexercise.movement.exercise.Situp;
import com.embedded.socialexercise.movement.exercise.Squat;
import com.embedded.socialexercise.movement.exercise.ToeTouch;
import com.embedded.socialexercise.movement.exercise.TrunkRotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovementDetection implements SensorEventListener {
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;

    private final LimitedSizeQueue<SensorData> timeWindow = new LimitedSizeQueue<>(25);
    private final List<Exercise> exerciseToDetect;
    private Movement currentPrediction = Movement.NONE;

    private final int PREDICTION_COUNTER = 20;
    //Milsec to next update
    private final int UPDATES = 100;

    private int curPredCounter = PREDICTION_COUNTER;
    private boolean moveUp = false;

    private TextView viewToShow;

    private Context context;

    public MovementDetection(Context con, SensorManager manager, TextView viewToShow) {
        this.mSensorManager = manager;
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.viewToShow = viewToShow;
        this.exerciseToDetect = new ArrayList<>();
        this.context = con;
        initMap();
    }

    private void initMap() {
        exerciseToDetect.add(new Situp());
        exerciseToDetect.add(new Squat());
        exerciseToDetect.add(new ToeTouch());
        exerciseToDetect.add(new TrunkRotation());
    }

    public void onResume() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL );
    }

    public void onPause() {
        mSensorManager.unregisterListener(this);
    }

    public Movement getPrediction() {
        return currentPrediction;
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
            if (timeStamp - lastAddedData.getTimeStamp() > UPDATES) {
                SensorData toAdd = new SensorData(timeStamp, x, y, z);
                timeWindow.add(toAdd);
                Map<String, Float> quartileMap = quartile(timeWindow.toArray(new SensorData[timeWindow.size()]));
                setPrediction(quartileMap);
                mapCounterIncrease(toAdd);
                sb.append("Current Prediction:" + currentPrediction + "\n");
                for(Exercise exercise :exerciseToDetect) {
                    if(exercise.getMovementType() != Movement.NONE)
                    sb.append("Move Counter:"  + exercise.getMovementType() + " - " + exercise.getMoveCounter() + "\n");
                }
                viewToShow.setText(sb.toString());
            }
        } else {
            timeWindow.add(new SensorData(timeStamp, x, y, z));
        }
    }

    private String createQuartileMapString(Map<String, Float> quartileMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("Quartil 1:"+ quartileMap.get("Q1x") + "  " + quartileMap.get("Q1y") + "  " + quartileMap.get("Q1z") + "\n");
        sb.append("Current mean:" + quartileMap.get("Q2x") + "  " + quartileMap.get("Q2y") + "  " + quartileMap.get("Q2z") + "\n");
        sb.append("Quartil 3:"+ quartileMap.get("Q3x") + "  " + quartileMap.get("Q3y") + "  " + quartileMap.get("Q3z") + "\n");
        return sb.toString();
    }

    private String createWriteMessage(SensorData cur, Map<String, Float> quartileMap) {
        StringBuilder builder = new StringBuilder();
        for(int i=1;i<=3;i++) {
            builder.append(quartileMap.get("Q"+i+"x").toString().replace('.',','));
            builder.append(";");
            builder.append(quartileMap.get("Q"+i+"y").toString().replace('.',','));
            builder.append(";");
            builder.append(quartileMap.get("Q"+i+"z").toString().replace('.',','));
            builder.append(";");
        }
        builder.append(String.valueOf(cur.getTotal()).replace('.',','));
        builder.append(System.lineSeparator());
        return builder.toString();
    }

    private void mapCounterIncrease(SensorData current) {
        for(Exercise exercise : exerciseToDetect) {
            if(exercise.getMovementType() == currentPrediction) {
                moveUp = exercise.increaseCounter(moveUp, current);
                return;
            }
        }
    }

    private void setPrediction(Map<String, Float> quartileMap) {
        Movement zwPrediction = Movement.NONE;

        for(Exercise exercise : exerciseToDetect) {
            if(exercise.isExercise(quartileMap)) {
                zwPrediction = exercise.getMovementType();
                break;
            }
        }

        //Prediction is not instant, instead it needs to stay steady for a specific time
        if(currentPrediction != zwPrediction) {
            curPredCounter--;
            if (curPredCounter == 0) {
                currentPrediction = zwPrediction;
                curPredCounter = PREDICTION_COUNTER;
                Log.i("Sensor","Prediction:" + currentPrediction);
            }
        } else {
            curPredCounter = PREDICTION_COUNTER;
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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

        int nQ1 = Math.round(values.length * 25 / 100);
        int nQ2 = Math.round(values.length * 50 / 100);
        int nQ3 = Math.round(values.length * 70 / 100);

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
}