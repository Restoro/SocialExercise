package com.embedded.socialexercise.mqtt;

import com.embedded.socialexercise.events.OnMessageReceivedListener;
import com.embedded.socialexercise.events.OnPositionReceivedListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;


public interface IMqtt {
    void sendMessage(String msg);
    List<Message> setTopic(String topic);
    void addOnMessageReceivedListener(OnMessageReceivedListener listener);
    void removeOnMessageReceivedListener(OnMessageReceivedListener listener);
    void addOnPositionReceivedListener(OnPositionReceivedListener listener);
    void removeOnPositionReceivedListener(OnPositionReceivedListener listener);
    void sendPosition(LatLng position);
}
