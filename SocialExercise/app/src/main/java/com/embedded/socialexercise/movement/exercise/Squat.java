package com.embedded.socialexercise.movement.exercise;

import com.embedded.socialexercise.helper.Helper;
import com.embedded.socialexercise.movement.SensorData;
import com.embedded.socialexercise.movement.enums.Movement;

import java.util.Map;

public class Squat extends Exercise {
    @Override
    public boolean isExercise(Map<String, Float> quartileMap) {
        //Phone should online move in z
        boolean rangeX = Helper.inRange(quartileMap.get("Q2x"), 0, 3f);
        boolean rangeY = Helper.inRange(quartileMap.get("Q2y"), 0, 1.5f);

        //Quartiles should differ and movement should be recognized
        boolean rangeZ = !Helper.inRange(quartileMap.get("Q1z"), quartileMap.get("Q3z"), 1.0f);
        boolean rangeQ1Z = Helper.inRange(quartileMap.get("Q1z"), 9, 2);
        boolean rangeQ3Z = Helper.inRange(quartileMap.get("Q3z"), 10.5f, 2);
        return rangeX && rangeY && rangeZ && rangeQ1Z && rangeQ3Z;
    }

    @Override
    public Movement getMovementType() {
        return Movement.SQUAT;
    }

    @Override
    public boolean increaseCounter(boolean moveUp, SensorData current) {
        return checkCurForCounter(moveUp, Helper.inRange(current.getTotal(), 8, 1f), Helper.inRange(current.getTotal(), 12, 1f));
    }
}
