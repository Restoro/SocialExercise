package com.embedded.socialexercise.movement;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MovementDetection implements SensorEventListener {
    private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;
    private final LimitedSizeQueue<SensorData> timeWindow = new LimitedSizeQueue<>(25);
    //Use of atomic as its mutable!
    private final Map<Movement,AtomicInteger> moveCounter;

    private Movement currentPrediction = Movement.NONE;

    private final int PREDICTION_COUNTER = 20;
    //Milsec to next update
    private final int UPDATES = 100;

    private int curPredCounter = PREDICTION_COUNTER;
    private boolean moveUp = false;

    private TextView viewToShow;

    public MovementDetection(SensorManager manager, TextView viewToShow) {
        this.mSensorManager = manager;
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.viewToShow = viewToShow;
        this.moveCounter = new HashMap<>();
        initMap();
    }

    private void initMap() {
        for(Movement m : Movement.values()) {
            if(m != Movement.NONE)
                moveCounter.put(m, new AtomicInteger());
        }
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
                sb.append("Last added:" + x + " " + y + " " + z + "\n");
                //Log.i("Sensor", "Last added:" + x + " " + y + " " + z );

                sb.append("Quartil 1:"+ quartileMap.get("Q1x") + "  " + quartileMap.get("Q1y") + "  " + quartileMap.get("Q1z") + "\n");
                sb.append("Current mean:" + quartileMap.get("Q2x") + "  " + quartileMap.get("Q2y") + "  " + quartileMap.get("Q2z") + "\n");
                sb.append("Quartil 3:"+ quartileMap.get("Q3x") + "  " + quartileMap.get("Q3y") + "  " + quartileMap.get("Q3z") + "\n");
                sb.append("Total" + Math.sqrt((x*x)+(y*y)+(z*z))+"\n");
                setPrediction(quartileMap);
                mapCounterIncrease(toAdd);
                sb.append("Current Prediction:" + currentPrediction + "\n");
                for(Map.Entry<Movement,AtomicInteger> entry : moveCounter.entrySet()) {
                    if(entry.getKey() != Movement.NONE)
                    sb.append("Move Counter:"  + entry.getKey() + " - " + entry.getValue().get() + "\n");
                }
                viewToShow.setText(sb.toString());
            }
        } else {
            timeWindow.add(new SensorData(timeStamp, x, y, z));
        }

    }


    private void mapCounterIncrease(SensorData current) {
        switch(currentPrediction) {
            case SITUP:
                increaseCounter(inRange(current.getY(), 10, 2),inRange(current.getZ(), 10, 2));
                break;
            case SQUAT:
                increaseCounter(inRange(current.getZ(), 8, 0.5f),inRange(current.getZ(), 11, 0.5f));
                break;
            case JUMPING_JACK:
                //Not working correctly
                increaseCounter(inRange(current.getX(), 8, 2f),inRange(current.getX(), -8, 2f));
                break;
        }
    }

    private void increaseCounter(boolean upMoveReached, boolean downMoveReached) {
        if(moveUp && upMoveReached) {
            moveCounter.get(currentPrediction).incrementAndGet();
            moveUp = false;
            Log.i("Sensor","Counter:" + moveCounter);
        } else if(!moveUp && downMoveReached){
            moveUp = true;
        }
    }

    private void setPrediction(Map<String, Float> quartileMap) {
        Movement zwPrediction = Movement.NONE;

        if (isSitup(quartileMap)) {
            zwPrediction = Movement.SITUP;
        } else if (isSquat(quartileMap)) {
            zwPrediction = Movement.SQUAT;
        } else if(isJumpingJack(quartileMap)) {
            zwPrediction = Movement.JUMPING_JACK;
        }

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

    private boolean isJumpingJack(Map<String, Float> quartileMap) {

        boolean rangeY = !inRange(quartileMap.get("Q3y"), quartileMap.get("Q1y"), 3);
        boolean rangeX = !inRange(quartileMap.get("Q1x"),quartileMap.get("Q3x"),3);
        return rangeY && rangeX;
    }

    //Checks if movement is squat
    private boolean isSquat(Map<String, Float> quartileMap) {
        //Phone should online move in z
        boolean rangeX = inRange(quartileMap.get("Q2x"), 0, 2);
        boolean rangeY = inRange(quartileMap.get("Q2y"), 0, 2);

        //Quartiles should differ and movement should be recognized
        boolean rangeZ = !inRange(quartileMap.get("Q1z"), quartileMap.get("Q3z"), 1.5f);
        return rangeX && rangeY && rangeZ;
    }

    //Checks if movement is situp
    private boolean isSitup(Map<String, Float> quartileMap) {
        //Phone should not move sideways / Phone held in Portrait mode
        boolean rangeX = inRange(quartileMap.get("Q2x"), 0, 2);

        //Checks if 25% of the data is smaller than 3 in z-axis
        boolean rangeQ1Z = quartileMap.get("Q1z") < 3;
        //Checks if Q3 of z and y are approaching the same value (Movement consists of high y and low z and vice versa -> ~Same Q3 range)
        boolean rangeQ3YZ = inRange(quartileMap.get("Q3y"), quartileMap.get("Q3z"), 3);
        //Checks if Q3 of y is in range of 10, as the movement ends in a upward position
        boolean rangeQ3Y = inRange(quartileMap.get("Q3y"), 10, 2);

        //All of the conditions should be true
        return rangeX && rangeQ1Z && rangeQ3YZ;
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
