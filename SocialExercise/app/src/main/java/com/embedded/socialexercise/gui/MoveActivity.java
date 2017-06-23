package com.embedded.socialexercise.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.embedded.socialexercise.App;
import com.embedded.socialexercise.R;
import com.embedded.socialexercise.events.OnMovementChangedListener;
import com.embedded.socialexercise.movement.MovementDetection;
import com.embedded.socialexercise.movement.enums.Movement;

import java.util.HashMap;
import java.util.Map;

public class MoveActivity extends AppCompatActivity implements OnMovementChangedListener{
    MovementDetection detection;
    boolean startWorkout = false;
    Map<Movement, Integer> mapForWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement);
        mapForWorkout = new HashMap<>();

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerForMovementUpdates();
    }

    private void registerForMovementUpdates() {
        detection = App.getMovementDetection();
        if(detection != null) {
            detection.addPositionEventListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterForMovementUpdates();
    }

    private void unregisterForMovementUpdates() {
        if(detection != null) {
            detection.removePositionEventListener(this);
            detection.onStop();
        }
    }

    @Override
    public void onMovementPredictionChanged(Movement curMovement) {
        TextView curMovementText = (TextView) findViewById(R.id.workout_predicition);
        curMovementText.setText(curMovement.toString());
        addCardForMovement(curMovement);
    }

    private void addCardForMovement(Movement movement) {
        if(!mapForWorkout.containsKey(movement) && movement != Movement.NONE) {
            View cardItem = getLayoutInflater().inflate(R.layout.workout_item, null);
            cardItem.setId(movement.hashCode());
            TextView titleView = (TextView) cardItem.findViewById(R.id.cardview_movement_title);
            titleView.setText(movement.toString());
            int iconId = detection.movementToIcon(movement);
            ImageView iconView = (ImageView) cardItem.findViewById(R.id.cardview_image);
            iconView.setBackgroundResource(iconId);
            LinearLayout root = (LinearLayout) findViewById(R.id.workout_item_list);
            root.addView(cardItem);
            mapForWorkout.put(movement,movement.hashCode());
        }
    }


    @Override
    public void onMovementCounterIncreased(Movement curMovement, int moveCounter) {
        if(mapForWorkout.containsKey(curMovement)) {
            LinearLayout root = (LinearLayout) findViewById(R.id.workout_item_list);
            int cardItemId = mapForWorkout.get(curMovement);
            View cardItem = root.findViewById(cardItemId);
            TextView counterTextView = cardItem.findViewById(R.id.cardview_movement_counter);
            counterTextView.setText(String.valueOf(moveCounter));
        }
    }

    public void startStopWorkout(View view) {
        Button btn = (Button) findViewById(R.id.workout_button);
        startWorkout = !startWorkout;
        btn.setText(startWorkout ? "Stop Workout" : "Start Workout");
        if(startWorkout) {
            detection.onStart();
        } else {
            detection.onStop();
        }
    }
}
