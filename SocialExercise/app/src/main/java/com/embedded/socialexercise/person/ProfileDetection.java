package com.embedded.socialexercise.person;

import android.util.Log;

import com.embedded.socialexercise.events.OnMessageReceivedListener;
import com.embedded.socialexercise.events.OnPositionReceivedListener;
import com.embedded.socialexercise.events.OnProfileChangeListener;
import com.embedded.socialexercise.mqtt.MqttDetection;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Verena on 26.06.2017.
 */

public class ProfileDetection {

    Person p;
    private MqttDetection detection;
    private ArrayList<OnProfileChangeListener> listeners = new ArrayList<>();

    public ProfileDetection(){
        p = new Person();
        p.isMale = true;
        p.avatar = 0;
        p.firstName = "";
        p.lastName = "";
        p.address = "";
        p.favouriteActivities =  "";
    }

    public Person getProfile(){
        return p;
    }

    public void changeProfile(Person p){
        this.p.firstName = p.firstName;
        this.p.lastName = p.lastName;
        this.p.address = p.address;
        this.p.favouriteActivities =  p.favouriteActivities;
        this.p.isMale = p.isMale;
        for (OnProfileChangeListener listener : listeners) {
            try {
                listener.profileChanged(p);
            } catch (Exception e) {
                Log.e("Profile", e.getMessage());
            }
        }
    }

    public void addOnProfileChangedListener(OnProfileChangeListener listener) {
        listeners.add(listener);
    }


    public void removeOnProfileChangedListener(OnProfileChangeListener listener) {
        listeners.remove(listener);
    }


}
