package com.embedded.socialexercise;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.embedded.socialexercise.events.OnPositionLocationChangedListener;
import com.embedded.socialexercise.gui.ChatActivity;
import com.embedded.socialexercise.gui.MapsActivity;
import com.embedded.socialexercise.movement.MovementDetection;
import com.embedded.socialexercise.position.PositionDetection;
import com.google.android.gms.maps.model.LatLng;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    MovementDetection detection;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detection = App.getMovementDetection((TextView)findViewById(R.id.text_view));
    }

    @Override
    protected void onResume() {
        super.onResume();
        detection.onResume();
    }

    @Override
    protected  void onPause() {
        super.onPause();
        detection.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MainActivityPermissionsDispatcher.registerForLocationUpdatesWithCheck(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        App.getPositionDetection().onStop();
    }

    @NeedsPermission({Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void registerForLocationUpdates() {
        PositionDetection positionDetection = App.getPositionDetection();
        positionDetection.addPositionEventListener(new OnPositionLocationChangedListener() {
            @Override
            public void onLocationChanged(LatLng position) {
                String msg = "Updated position \n" + position.latitude + " " + position.longitude;
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Needs to be implemented to redirect to called method if permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
