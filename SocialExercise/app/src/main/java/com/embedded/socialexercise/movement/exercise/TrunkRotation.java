package com.embedded.socialexercise.movement.exercise;

import com.embedded.socialexercise.helper.Helper;
import com.embedded.socialexercise.movement.enums.Movement;
import com.embedded.socialexercise.movement.SensorData;

import java.util.Map;

public class TrunkRotation extends Exercise {
    @Override
    public boolean isExercise(Map<String, Float> quartileMap) {
        boolean rangeQ1X = Helper.inRange(quartileMap.get("Q1x"), -4, 2);
        boolean rangeQ3X = Helper.inRange(quartileMap.get("Q3x"), -1, 2);

        boolean rangeQ2Y = Helper.inRange(quartileMap.get("Q2y"), 0, 3);
        boolean rangeY = !Helper.inRange(quartileMap.get("Q1y"), quartileMap.get("Q3y"), 4);

        boolean rangeQ2Z = Helper.inRange(quartileMap.get("Q2z"), 9, 3);

        return rangeQ1X && rangeQ3X && rangeQ2Y && rangeY && rangeQ2Z;
    }

    @Override
    public Movement getMovementType() {
        return Movement.TRUNK_ROTATION;
    }

    @Override
    public boolean increaseCounter(boolean moveUp, SensorData current) {
        return checkCurForCounter(moveUp, Helper.inRange(current.getTotal(), 7.5f, 0.5f), Helper.inRange(current.getZ(), 15, 0.5f));
    }
}
