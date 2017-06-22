package com.embedded.socialexercise.movement;

public class SensorData {
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

    public float getTotal() {
        return (float) Math.sqrt((x*x)+(y*y)+(z*z));
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
