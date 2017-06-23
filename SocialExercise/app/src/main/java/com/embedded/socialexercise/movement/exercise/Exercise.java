package com.embedded.socialexercise.movement.exercise;


import com.embedded.socialexercise.movement.enums.Movement;
import com.embedded.socialexercise.movement.SensorData;

import java.util.Map;

public abstract class Exercise {
    protected int moveCounter = 0;
    public abstract boolean isExercise(Map<String, Float> quartileMap);
    public abstract Movement getMovementType();

    //Returns if moveUp should change
    public abstract boolean increaseCounter(boolean moveUp, SensorData current);

    protected boolean checkCurForCounter(boolean moveUp, boolean checkUp, boolean checkDown) {

        if(moveUp && checkUp) {
            moveCounter++;
            return false;
        } else if(!moveUp && checkDown){
            return true;
        }
        return moveUp;
    }

    public int getMoveCounter() {
        return moveCounter;
    }

    public void resetCounter() {
        moveCounter = 0;
    }
}
