package com.embedded.socialexercise.movement.exercise;

import com.embedded.socialexercise.helper.Helper;
import com.embedded.socialexercise.movement.enums.Movement;
import com.embedded.socialexercise.movement.SensorData;

import java.util.Map;

public class ToeTouch extends Exercise {
    @Override
    public boolean isExercise(Map<String, Float> quartileMap) {
        boolean rangeQ2X = Helper.inRange(quartileMap.get("Q2x"), -9, 1.5f);
        boolean rangeQ2Y = Helper.inRange(quartileMap.get("Q2y"), -0.5f, 1);

        boolean rangeQ1z = Helper.inRange(quartileMap.get("Q1z"), 0, 2);
        boolean rangeQ3z = Helper.inRange(quartileMap.get("Q3z"), 1, 2);

        return rangeQ2X && rangeQ2Y && rangeQ1z && rangeQ3z;
    }

    @Override
    public Movement getMovementType() {
        return Movement.TOE_TOUCH;
    }

    @Override
    public boolean increaseCounter(boolean moveUp, SensorData current) {
        return checkCurForCounter(moveUp, Helper.inRange(current.getTotal(), 8, 0.5f), Helper.inRange(current.getZ(), 12, 0.5f));
    }
}
