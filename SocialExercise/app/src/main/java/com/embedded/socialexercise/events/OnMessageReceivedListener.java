package com.embedded.socialexercise.events;

import com.embedded.socialexercise.mqtt.Message;

/**
 * Created by hoellinger on 22.06.2017.
 */

public interface OnMessageReceivedListener {
    void messageRecieved(Message msg);
}
