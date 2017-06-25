package com.embedded.socialexercise.position;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.embedded.socialexercise.events.OnPositionLocationChangedListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import permissions.dispatcher.PermissionUtils;

public class PositionDetection extends Application implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private ArrayList<OnPositionLocationChangedListener> listenerArrayList;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private final String[] PERMISSION = new String[] {"android.permission.INTERNET", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_NETWORK_STATE", "android.permission.ACCESS_COARSE_LOCATION"};

    public PositionDetection(Context con) {
        context = con;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        listenerArrayList = new ArrayList<>();
    }

    public void addPositionEventListener(OnPositionLocationChangedListener eventListener)
    {
        if(!mGoogleApiClient.isConnected()) this.onStart();
        listenerArrayList.add(eventListener);
    }

    public void removePositionEventListener(OnPositionLocationChangedListener eventListener)
    {
        listenerArrayList.remove(eventListener);
    }

    private void updatePositionEventListener(LatLng position) {
        for(OnPositionLocationChangedListener listener : listenerArrayList) {
            try {
                listener.onLocationChanged(position);
            } catch (Exception e) {
                Log.e("PositionDetection", e.getMessage());
            }
        }
    }

    public void onStart() {
        if(PermissionUtils.hasSelfPermissions(context, PERMISSION)) {
            Log.i("Position Detection", "Start");
            mGoogleApiClient.connect();
        }
    }

    public void onStop() {
        Log.i("Position Detection", "Stop");
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

            // only stop if it's connected, otherwise we crash
            if (mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) throws SecurityException{
        // Get last known recent location.
        Location mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        // Note that this can be NULL if last location isn't already known.
        if (mCurrentLocation != null) {
            // Print current location if not null
            Log.d("DEBUG", "current location: " + mCurrentLocation.toString());
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            updatePositionEventListener(latLng);
        }
        // Begin polling for new location updates.
        startLocationUpdates();
    }

    protected void startLocationUpdates() throws SecurityException{
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        // Create LatLng with new Position
        Log.i("PositionDetection","Get Position");
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        updatePositionEventListener(latLng);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("DEBUG", "Connection failed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(context, "Connection suspended. Pls reconnect!",Toast.LENGTH_SHORT).show();
        Log.d("DEBUG", "Connection failed");
    }
}
