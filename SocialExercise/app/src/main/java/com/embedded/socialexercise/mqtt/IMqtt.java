package com.embedded.socialexercise.mqtt;

import com.embedded.socialexercise.events.OnMessageReceivedListener;


public interface IMqtt {
    Message sendMessage(String msg);
    Message[] setTopic(String topic);
    void addOnMessageReceivedListener(OnMessageReceivedListener listener);
    void removeOnMessageReceivedListener(OnMessageReceivedListener listener);
}
