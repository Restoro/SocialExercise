package com.embedded.socialexercise.mqtt;

import com.embedded.socialexercise.events.OnMessageReceivedListener;

/**
 * Created by hoellinger on 22.06.2017.
 */

public interface IMqtt {
    Message sendMessage(String s);
    Message[] setTopic(String topic);
    void addOnMessageReceivedListener(OnMessageReceivedListener listener);
    void removeOnMessageReceivedListener(OnMessageReceivedListener listener);
}
