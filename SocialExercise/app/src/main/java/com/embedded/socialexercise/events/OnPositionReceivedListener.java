package com.embedded.socialexercise.events;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Verena on 23.06.2017.
 */

public interface OnPositionReceivedListener {
    void positionRecieved(LatLng position, String id);
}
