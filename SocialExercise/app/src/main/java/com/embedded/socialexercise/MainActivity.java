package com.embedded.socialexercise;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.embedded.socialexercise.gui.ProfileActivity;
import com.embedded.socialexercise.gui.ChatActivity;
import com.embedded.socialexercise.gui.MapsActivity;
import com.embedded.socialexercise.gui.MoveActivity;
import com.embedded.socialexercise.mqtt.MqttDetection;

public class MainActivity extends AppCompatActivity {
    MqttDetection mqttDetection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected  void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mqttDetection = App.getMqttDetection();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void goToProfile(View v) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    public void goToMove(View v) {
        Intent intent = new Intent(this, MoveActivity.class);
        startActivity(intent);
    }

    public void goToMaps(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void goToChat(View v) {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }


}
