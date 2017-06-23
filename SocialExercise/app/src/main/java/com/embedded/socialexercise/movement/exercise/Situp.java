package com.embedded.socialexercise.movement.exercise;

import com.embedded.socialexercise.helper.Helper;
import com.embedded.socialexercise.movement.SensorData;
import com.embedded.socialexercise.movement.enums.Movement;

import java.util.Map;


public class Situp extends Exercise{

    @Override
    public boolean isExercise(Map<String, Float> quartileMap) {
        //Phone should not move sideways / Phone held in Portrait mode
        boolean rangeX = Helper.inRange(quartileMap.get("Q2x"), 0, 2);

        boolean rangeQ1Z = Helper.inRange(quartileMap.get("Q1z"), 2, 3);
        //Checks if Q3 of z and y are approaching the same value (Movement consists of high y and low z and vice versa -> ~Same Q3 range)
        boolean rangeQ3YZ = Helper.inRange(quartileMap.get("Q3y"), quartileMap.get("Q3z"), 2);
        //Checks if Q3 of y is in range of 10, as the movement ends in a upward position
        boolean rangeQ3Y = Helper.inRange(quartileMap.get("Q3y"), 9, 1.5f);
        boolean rangeQ3Z = Helper.inRange(quartileMap.get("Q3z"), 9, 1.5f);

        //All of the conditions should be true
        return rangeX && rangeQ1Z && rangeQ3YZ && rangeQ3Y && rangeQ3Z;
    }

    @Override
    public Movement getMovementType() {
        return Movement.SITUP;
    }

    @Override
    public boolean increaseCounter(boolean moveUp, SensorData current) {
        return checkCurForCounter(moveUp, Helper.inRange(current.getY(), 10, 2), Helper.inRange(current.getZ(), 10, 2));
    }
}
