package com.embedded.socialexercise.person;

import android.util.Log;

import com.embedded.socialexercise.events.OnProfileChangeListener;
import com.embedded.socialexercise.helper.Helper;
import com.embedded.socialexercise.mqtt.MqttDetection;

import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by Verena on 26.06.2017.
 */

public class ProfileDetection {

    Person p;
    private MqttDetection detection;
    private ArrayList<OnProfileChangeListener> listeners = new ArrayList<>();

    public ProfileDetection(){
        p = readProfile();
        if (p == null) {
            p = new Person();
            p.isMale = true;
            p.avatar = 0;
            p.firstName = "";
            p.lastName = "";
            p.address = "";
            p.favouriteActivities =  "";
        }
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
        writeProfile(this.p);
        for (OnProfileChangeListener listener : listeners) {
            try {
                listener.profileChanged(p);
            } catch (Exception e) {
                Log.e("Profile", e.getMessage());
            }
        }
    }

    public void writeProfile(Person p) {
        StringBuilder sb = new StringBuilder();
        sb.append(p.firstName);
        sb.append(";");
        sb.append(p.lastName);
        sb.append(";");
        sb.append(p.address);
        sb.append(";");
        sb.append(p.favouriteActivities);
        sb.append(";");
        sb.append(p.isMale);
        Helper.writeToFile("Profile.txt", sb.toString());
    }

    public Person readProfile() {
        FileInputStream reader = Helper.readFile("Profile.txt");
        if(reader == null) return null;
        StringBuilder contentBuilder = new StringBuilder();
        int content;
        try {
            while ((content = reader.read()) != -1) {
                contentBuilder.append((char)content);
            }
        } catch (Exception e) {
            Log.i("Profile","Error Reading File");
        }
        String personText = contentBuilder.toString();
        String[] personData = personText.split(";");
        Person p = new Person();
        if(personData.length >= 5) {
            p.firstName = personData[0];
            p.lastName = personData[1];
            p.address = personData[2];
            p.favouriteActivities = personData[3];
            p.isMale = Boolean.parseBoolean(personData[4]);
        }
        return p;
    }

    public void addOnProfileChangedListener(OnProfileChangeListener listener) {
        listeners.add(listener);
    }


    public void removeOnProfileChangedListener(OnProfileChangeListener listener) {
        listeners.remove(listener);
    }


}
