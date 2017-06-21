package com.embedded.socialexercise.events;

import com.google.android.gms.maps.model.LatLng;


public interface OnPositionLocationChangedListener {
    void onLocationChanged(LatLng position);
}
