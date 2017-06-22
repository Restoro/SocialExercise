package com.embedded.socialexercise.mqtt;

import com.embedded.socialexercise.events.OnMessageReceivedListener;

import java.util.ArrayList;

/**
 * Created by hoellinger on 22.06.2017.
 */

public class MqttForTesting implements IMqtt {
    private static IMqtt mqtt;
    ArrayList<OnMessageReceivedListener> listeners = new ArrayList<>();

    public static IMqtt getMqtt(){
        if(mqtt==null)
            mqtt = new MqttForTesting();
        return mqtt;
    }

    public Message sendMessage(String msg) {
        Message m = new Message();
        m.sender = "Nik";
        m.time = "12:35";
        m.message = msg;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {

                }
                Message m = new Message();
                m.sender = "asdf";
                m.time = "12:56";
                m.message = "okay";
                fireOnMessageReceived(m);
            }
        }).start();
        return m;
    }

    @Override
    public Message[] setTopic(String topic) {
        Message[] m = new Message[3];
        m[0] = new Message();
        m[0].message = "Will wer laufen gehen?";
        m[0].sender = "Nik";
        m[0].time = "13:12";
        m[1] = new Message();
        m[1].message = "Gerne :) 17:15 ok?";
        m[1].sender = "asdf";
        m[1].time = "13:13";
        m[2] = new Message();
        m[2].message = "Ist ok für mich ;)";
        m[2].sender = "Nik";
        m[2].time = "13:19";
        return m;
    }

    @Override
    public void addOnMessageReceivedListener(OnMessageReceivedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeOnMessageReceivedListener(OnMessageReceivedListener listener) {
        listeners.remove(listener);
    }

    private void fireOnMessageReceived(Message msg) {
        for(OnMessageReceivedListener listener : listeners) {
            try{
                listener.messageRecieved(msg);
            } catch (Exception e){
                listeners.remove(listener);
            }
        }
    }
}
