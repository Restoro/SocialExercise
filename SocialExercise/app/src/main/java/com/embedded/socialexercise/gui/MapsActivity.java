package com.embedded.socialexercise.gui;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.embedded.socialexercise.App;
import com.embedded.socialexercise.R;
import com.embedded.socialexercise.events.OnPositionReceivedListener;
import com.embedded.socialexercise.mqtt.MqttDetection;
import com.embedded.socialexercise.person.Person;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MapsActivity extends BasicMenuActivity implements OnMapReadyCallback, OnPositionReceivedListener {

    private final Context context = MapsActivity.this;
    private GoogleMap mMap;
    private Map<String, Marker> markers = new HashMap<>();
    private MqttDetection detection;
    private SupportMapFragment mapFragment;

    LatLngBounds.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        MapsActivityPermissionsDispatcher.createMapWithCheck(this);
        setup(R.id.nav_maps);
    }

    @NeedsPermission({Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void createMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Maps","Start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        detection.removeOnPositionReceivedListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MapsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(R.layout.info_window_person, null);
                Person p = (Person) marker.getTag();
                ((TextView) v.findViewById(R.id.txtName)).setText(p.firstName + " " + p.lastName);
                ((TextView) v.findViewById(R.id.txtAddr)).setText(p.address);
                ((TextView) v.findViewById(R.id.txtFavSports)).setText((p.favouriteActivities));;
                ((ImageView) v.findViewById(R.id.imgAvatar)).setImageResource(p.getAvatarId());
                return v;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                
            }
        });

        detection = App.getMqttDetection();
        detection.addOnPositionReceivedListener(this);
        new Thread(getRunnable()).start();
    }

    private boolean cameraSetted;

    @Override
    public void positionRecieved(final Person person) {
        Log.i("Map","Got Position");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Marker m = markers.get(person.mqttID);
                LatLng position = new LatLng(person.latitude, person.longitude);
                if(m==null) {
                    m = mMap.addMarker(new MarkerOptions().position(position));
                    markers.put(person.mqttID, m);
                }
                m.setTag(person);
                m.setPosition(position);
                Log.i("Map","Added new position");
            }
        });
    }

    private boolean setCamera(){
        LatLng position = detection.getOwnPosition();
        if(position.latitude==0.0 && position.longitude==0.0) return false;
        double lonDifference = 25.0/111.32*Math.cos(position.latitude*Math.PI/180);
        builder = new LatLngBounds.Builder();
        // builder.include(pos);
        builder.include(new LatLng(position.latitude, position.longitude+lonDifference));
        builder.include(new LatLng(position.latitude, position.longitude-lonDifference));
        final int width = getResources().getDisplayMetrics().widthPixels;
        final int height = getResources().getDisplayMetrics().heightPixels;
        final int padding = (int) (width * 0.12);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));
            }
        });
        return true;
    }

    private Runnable getRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                if(!setCamera()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new Thread(getRunnable()).start();
                }
            }
        };
    }
}
