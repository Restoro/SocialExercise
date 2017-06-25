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
import com.embedded.socialexercise.helper.Helper;
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
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MapsActivity extends BasicMenuActivity implements OnMapReadyCallback, OnPositionReceivedListener {

    private final Context context = MapsActivity.this;
    private GoogleMap mMap;
    private ArrayList<Marker> markers = new ArrayList<>();
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
                ((TextView) v.findViewById(R.id.txtName)).setText(p.firstName);
                ((TextView) v.findViewById(R.id.txtAddr)).setText(p.address);
                ((TextView) v.findViewById(R.id.txtFavSports)).setText(Helper.iterableToString(p.favouriteActivities));;
                ((ImageView) v.findViewById(R.id.imgAvatar)).setImageResource(p.getAvatarId());
                return v;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng pos = new LatLng(48.337714, 14.319592);
        Marker m = mMap.addMarker(new MarkerOptions().position(pos));
        Person p = new Person();
        p.firstName = "Niki";
        p.mqttID = "paho39p4873294827";
        p.address = "Linz";
        p.isMale = true;
        p.avatar = 0;
        List<String> favAct = new ArrayList<>();
        favAct.add("Football");
        favAct.add("Running");
        favAct.add("Squads");
        p.favouriteActivities = favAct;
        m.setTag(p);



        pos = new LatLng(48.159067, 14.033028);
        m = mMap.addMarker(new MarkerOptions().position(pos));
        p = new Person();
        p.firstName = "Pazi";
        p.address = "Wels";
        p.isMale = true;
        p.avatar = 4;
        favAct = new ArrayList<>();
        favAct.add("Tennis");
        favAct.add("Push-ups");
        favAct.add("Squads");
        p.favouriteActivities = favAct;
        m.setTag(p);

        pos = new LatLng(48.214542, 14.228862);
        m = mMap.addMarker(new MarkerOptions().position(pos));
        p = new Person();
        p.firstName = "Veri";
        p.address = "Traun";
        p.isMale = false;
        p.avatar = 8;
        favAct = new ArrayList<>();
        favAct.add("Dancing");
        favAct.add("Skipping");
        favAct.add("Squads");
        p.favouriteActivities = favAct;
        m.setTag(p);


        double lonDifference = 25.0/111.32*Math.cos(48.214542*Math.PI/180);
        builder = new LatLngBounds.Builder();
       // builder.include(pos);
        builder.include(new LatLng(48.214542, 14.228862+lonDifference));
        builder.include(new LatLng(48.214542, 14.228862-lonDifference));
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, padding));

        detection = App.getMqttDetection();
        detection.addOnPositionReceivedListener(this);
    }


    //TODO does not work yet
    @Override
    public void positionRecieved(final LatLng position,final String id) {
        Log.i("Map","Got Position");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(Marker m : markers){
                    Person p = (Person)m.getTag();
                    if(id.equals(p.mqttID)){
                        m.remove();
                        markers.remove(m);
                        m = mMap.addMarker(new MarkerOptions().position(position));
                        m.setTag(p);
                        markers.add(m);
                        return;
                    }
                }

                Marker m = mMap.addMarker(new MarkerOptions().position(position));
                Person p = new Person();
                p.firstName = "Test";
                p.mqttID = id;
                p.address = "Wels";
                p.isMale = true;
                p.avatar = 4;
                List<String> favAct = new ArrayList<>();
                favAct = new ArrayList<>();
                favAct.add("Tennis");
                favAct.add("Push-ups");
                favAct.add("Squads");
                p.favouriteActivities = favAct;
                m.setTag(p);
                markers.add(m);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                Log.i("Map","Added new position");
            }
        });

    }
}
