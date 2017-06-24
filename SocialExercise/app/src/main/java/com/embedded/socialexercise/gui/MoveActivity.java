package com.embedded.socialexercise.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class MoveActivity extends AppCompatActivity implements OnMovementChangedListener {
    MovementDetection detection;
    boolean startWorkout = false;
    boolean showAllExercises = false;
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
        if (detection != null) {
            detection.addPositionEventListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterForMovementUpdates();
    }

    private void unregisterForMovementUpdates() {
        if (detection != null) {
            detection.removePositionEventListener(this);
            detection.onStop();
        }
    }

    @Override
    public void onMovementPredictionChanged(Movement curMovement) {
        TextView curMovementText = (TextView) findViewById(R.id.card_textview_current_prediction);
        curMovementText.setText(curMovement.toString());
        TextView currentTxt = (TextView) findViewById(R.id.cardview_current_title);
        if (curMovement == Movement.NONE) {
            currentTxt.setVisibility(View.INVISIBLE);
        } else {
            currentTxt.setVisibility(View.VISIBLE);
        }
        addCardForMovement(curMovement);
    }

    private void addCardForMovement(Movement movement) {
        if (!mapForWorkout.containsKey(movement) && movement != Movement.NONE) {
            View cardItem = getLayoutInflater().inflate(R.layout.workout_item, null);
            cardItem.setId(movement.hashCode());
            TextView titleView = (TextView) cardItem.findViewById(R.id.cardview_movement_title);
            titleView.setText(movement.toString());
            int iconId = detection.movementToIcon(movement);
            ImageView iconView = (ImageView) cardItem.findViewById(R.id.cardview_image);
            iconView.setBackgroundResource(iconId);
            LinearLayout root = (LinearLayout) findViewById(R.id.workout_item_list);
            root.addView(cardItem);
            mapForWorkout.put(movement, movement.hashCode());
        }
    }


    @Override
    public void onMovementCounterIncreased(Movement curMovement, int moveCounter) {
        if (mapForWorkout.containsKey(curMovement)) {
            LinearLayout root = (LinearLayout) findViewById(R.id.workout_item_list);
            int cardItemId = mapForWorkout.get(curMovement);
            View cardItem = root.findViewById(cardItemId);
            TextView counterTextView = cardItem.findViewById(R.id.cardview_movement_counter);
            counterTextView.setText(String.valueOf(moveCounter));
        }
    }

    public void startStopWorkout(View view) {
        TextView txt = (TextView) findViewById(R.id.card_textview_current_prediction);
        TextView currentTxt = (TextView) findViewById(R.id.cardview_current_title);
        currentTxt.setVisibility(View.INVISIBLE);
        startWorkout = !startWorkout;
        txt.setText(startWorkout ? "Detecting Movement..." : "No Workout started!");
        if (startWorkout) {
            detection.onStart();
        } else {
            detection.onStop();
        }
    }

    public void showAllExercises(View view) {
        showAllExercises = !showAllExercises;
        for (Movement m : Movement.values()) {
            if (m != Movement.NONE) {
                if (showAllExercises)
                    addCardForMovement(m);
                else {
                    LinearLayout root = (LinearLayout) findViewById(R.id.workout_item_list);
                    int cardItemId = mapForWorkout.get(m);
                    View cardItem = root.findViewById(cardItemId);
                    TextView counterTextView = cardItem.findViewById(R.id.cardview_movement_counter);
                    if (counterTextView.getText().equals("0")) {
                        root.removeView(cardItem);
                        mapForWorkout.remove(m);
                    }
                }
            }
        }
    }
}
