package com.embedded.socialexercise.events;

import com.embedded.socialexercise.movement.enums.Movement;

public interface OnMovementChangedListener {
    void onMovementPredictionChanged(Movement curMovement);
    void onMovementCounterIncreased(Movement curMovement, int moveCounter);
}
