package com.embedded.socialexercise.movement;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private Context context;

    public MovementDetection(Context con, SensorManager manager, TextView viewToShow) {
        this.mSensorManager = manager;
        this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.viewToShow = viewToShow;
        this.moveCounter = new HashMap<>();
        this.context = con;
        initMap();
    }

    private void initMap() {
        for(Movement m : Movement.values()) {
            if(m != Movement.NONE)
                moveCounter.put(m, new AtomicInteger());
        }
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard,"socialExercise");
        dir.deleteOnExit();
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
                writeToCsvFile(createWriteMessage(toAdd, quartileMap));
            }
        } else {
            timeWindow.add(new SensorData(timeStamp, x, y, z));
        }

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

    private void writeToCsvFile(String msg) {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard,"socialExercise");
            //Log.v("Accel", dir.getAbsolutePath());

            File file = new File(dir, "output.csv");
            FileOutputStream f = new FileOutputStream(file, true);

            try {
                f.write(msg.getBytes());
                f.flush();
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mapCounterIncrease(SensorData current) {
        switch(currentPrediction) {
            case SITUP:
                increaseCounter(inRange(current.getY(), 10, 2),inRange(current.getZ(), 10, 2));
                break;
            case TRUNK_ROTATION:
                increaseCounter(inRange(current.getTotal(), 8, 0.5f), inRange(current.getTotal(), 15, 0.5f));
                break;
            case SQUAT:
                increaseCounter(inRange(current.getTotal(), 8, 0.5f),inRange(current.getTotal(), 12, 0.5f));
                break;
            case TOE_TOUCH:
                increaseCounter(inRange(current.getTotal(), 8, 0.5f),inRange(current.getTotal(), 12, 0.5f));
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
        } else if(isTrunkRotation(quartileMap)) {
            zwPrediction = Movement.TRUNK_ROTATION;
        } else if(isToeTouch(quartileMap)) {
            zwPrediction = Movement.TOE_TOUCH;
        } else if (isSquat(quartileMap)) {
            zwPrediction = Movement.SQUAT;
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




    //Checks if movement is trunk rotation
    private boolean isTrunkRotation(Map<String, Float> quartileMap) {
        boolean rangeQ1X = inRange(quartileMap.get("Q1x"), -4, 2);
        boolean rangeQ3X = inRange(quartileMap.get("Q3x"), -1, 2);

        boolean rangeQ2Y = inRange(quartileMap.get("Q2y"), 0, 3);
        boolean rangeY = !inRange(quartileMap.get("Q1y"), quartileMap.get("Q3y"), 4);

        boolean rangeQ2Z = inRange(quartileMap.get("Q2z"), 9, 3);

        Log.i("Trunk Rotation" , rangeQ1X + " " + rangeQ3X + " " + rangeQ2Y + " " + rangeY + " " + rangeQ2Z + " " + quartileMap.get("Q1x") + " " + quartileMap.get("Q3x"));
        return rangeQ1X && rangeQ3X && rangeQ2Y && rangeY && rangeQ2Z;
    }

    //Checks if movement is toe touch
    private boolean isToeTouch(Map<String, Float> quartileMap) {

        boolean rangeQ2X = inRange(quartileMap.get("Q2x"), -9, 1.5f);
        boolean rangeQ2Y = inRange(quartileMap.get("Q2y"), -0.5f, 1);

        boolean rangeQ1z = inRange(quartileMap.get("Q1z"), 0, 2);
        boolean rangeQ3z = inRange(quartileMap.get("Q3z"), 1, 2);

        return rangeQ2X && rangeQ2Y && rangeQ1z && rangeQ3z;
    }

    //Checks if movement is squat
    private boolean isSquat(Map<String, Float> quartileMap) {
        //Phone should online move in z
        boolean rangeX = inRange(quartileMap.get("Q2x"), 0, 1.5f);
        boolean rangeY = inRange(quartileMap.get("Q2y"), 0, 1.5f);

        //Quartiles should differ and movement should be recognized
        boolean rangeZ = !inRange(quartileMap.get("Q1z"), quartileMap.get("Q3z"), 1.0f);
        boolean rangeQ1Z = inRange(quartileMap.get("Q1z"), 9, 2);
        boolean rangeQ3Z = inRange(quartileMap.get("Q3z"), 10.5f, 2);
        return rangeX && rangeY && rangeZ && rangeQ1Z && rangeQ3Z;
    }

    //Checks if movement is situp
    private boolean isSitup(Map<String, Float> quartileMap) {
        //Phone should not move sideways / Phone held in Portrait mode
        boolean rangeX = inRange(quartileMap.get("Q2x"), 0, 2);

        //Checks if 25% of the data is smaller than 3 in z-axis
        boolean rangeQ1Y = inRange(quartileMap.get("Q1y"), 5, 1.5f);
        boolean rangeQ1Z = inRange(quartileMap.get("Q1z"), 2, 3);
        //Checks if Q3 of z and y are approaching the same value (Movement consists of high y and low z and vice versa -> ~Same Q3 range)
        boolean rangeQ3YZ = inRange(quartileMap.get("Q3y"), quartileMap.get("Q3z"), 2);
        //Checks if Q3 of y is in range of 10, as the movement ends in a upward position
        boolean rangeQ3Y = inRange(quartileMap.get("Q3y"), 9, 1.5f);
        boolean rangeQ3Z = inRange(quartileMap.get("Q3z"), 9, 1.5f);

        //All of the conditions should be true
        return rangeX && rangeQ1Y && rangeQ3YZ && rangeQ3Y && rangeQ3Z;
    }
}


class SensorData {
    private long timeStamp;
    private float x;
    private float y;
    private float z;


    protected SensorData(long timeStamp, float x, float y, float z) {
        this.timeStamp = timeStamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    protected float getX() {
        return x;
    }

    protected float getY() {
        return y;
    }

    protected float getZ() {
        return z;
    }

    protected float getTotal() {
        return (float) Math.sqrt((x*x)+(y*y)+(z*z));
    }

    protected long getTimeStamp() {
        return timeStamp;
    }
}
