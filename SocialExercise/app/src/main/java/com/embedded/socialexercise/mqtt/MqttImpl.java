package com.embedded.socialexercise.mqtt;

import java.util.ArrayList;
import com.embedded.socialexercise.events.OnMessageReceivedListener;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MqttImpl  implements IMqtt{

    private String broker;
    private String clientId;
    private String topic;
    private String sender;
    private MqttAsyncClient sampleClient;
    private final int qos = 0;
    private boolean connected;
    private ArrayList<OnMessageReceivedListener> listeners = new ArrayList<>();

    public MqttImpl(){
        this( "tcp://iot.soft.uni-linz.ac.at:1883", MqttAsyncClient.generateClientId());
    }
    public void setSender(String sender){
        this.sender = sender;
    }

    private MqttImpl(String broker, String clientId) {
        if (clientId == null || broker == null || broker.isEmpty()
                || clientId.isEmpty()) {
            throw new RuntimeException(String.format("invalid configuration broker: %s  clientId: %s",
                    broker, clientId));
        }
        this.broker = broker;
        this.clientId = clientId;
        this.topic = "SocialExercise";
        setup();
    }

    private void setup() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            sampleClient = new MqttAsyncClient(this.broker, this.clientId, persistence);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.setCallback(new MqttCallback() {

                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Message m = new Message();
                    m.topic = topic;
                    m.time = String.valueOf(getCurrentTimeStamp());
                    String msg = message.toString();
                    m.sender = msg.substring(0,msg.indexOf(":"));
                    m.message = message.toString().substring(msg.indexOf(":")+1);

                    fireOnMessageReceived(m);
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                }

                public void connectionLost(Throwable cause) {
                }
            });

            sampleClient.connect(connOpts, null, new IMqttActionListener() {

                public void onSuccess(IMqttToken asyncActionToken) {
                  try {
                    sampleClient.subscribe(topic, qos);
                  } catch (MqttException e) {}
                  connected = true;
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    connected = false;
                }
            });

        } catch (MqttException me) {
           System.out.println(me.getMessage());
        }
    }

    @Override
    public Message sendMessage(String msg) {
        if (connected) {
            Message m = new Message();
            m.sender = this.sender;
            m.message = msg;
            m.time = String.valueOf(getCurrentTimeStamp());
            m.topic = topic;
            String send = m.sender+":"+m.message;
            MqttMessage message = new MqttMessage(send.getBytes());
            message.setQos(qos);

            try{
               sampleClient.publish(topic, message);
           }catch(MqttException e){
                return null;
            }
            return m;
        } else {
            return null;
        }

    }

    public void close() {
        if (connected) {
            try {
                sampleClient.disconnect();
            } catch (MqttException e) {}
        }
    }

    @Override
    public Message[] setTopic(String topic) {
        if(topic.equals("SocialExercise")){
            this.topic = topic;
        }else{
            this.topic = "SocialExercise" + topic;
        }
        this.topic = "SocialExercise" + topic;
        return new Message[0];
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

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
