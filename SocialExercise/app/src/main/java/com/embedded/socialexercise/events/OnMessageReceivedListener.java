package com.embedded.socialexercise.events;

import com.embedded.socialexercise.mqtt.Message;

public interface OnMessageReceivedListener {
    void messageRecieved(Message msg);
}
