package com.embedded.socialexercise.gui;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.embedded.socialexercise.R;
import com.embedded.socialexercise.helper.Helper;
import com.embedded.socialexercise.person.Person;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng pos = new LatLng(48.337714, 14.319592);
        Marker m = mMap.addMarker(new MarkerOptions().position(pos));
        Person p = new Person();
        p.firstName = "Niki";
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

        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }
}
