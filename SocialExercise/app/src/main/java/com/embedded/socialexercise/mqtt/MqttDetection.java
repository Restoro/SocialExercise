package com.embedded.socialexercise.mqtt;

import android.content.Context;
import android.util.Log;

import com.embedded.socialexercise.App;
import com.embedded.socialexercise.events.OnMessageReceivedListener;
import com.embedded.socialexercise.events.OnPositionLocationChangedListener;
import com.embedded.socialexercise.events.OnPositionReceivedListener;
import com.embedded.socialexercise.events.OnProfileChangeListener;
import com.embedded.socialexercise.helper.Helper;
import com.embedded.socialexercise.person.Person;
import com.embedded.socialexercise.person.ProfileDetection;
import com.google.android.gms.maps.model.LatLng;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqttDetection implements IMqtt, OnPositionLocationChangedListener, OnProfileChangeListener{
    private Context context;
    private String broker;
    private String clientId;
    private String topic;
    private Person person = new Person();
    private MqttAsyncClient sampleClient;
    private final int qos = 0;
    private boolean connected;
    private ArrayList<OnMessageReceivedListener> listeners = new ArrayList<>();
    private ArrayList<OnPositionReceivedListener> listenersPos = new ArrayList<>();
    private LatLng position = new LatLng(0.0,0.0);
    private Map<String, List<Message>> memory = new HashMap<>();
    private List<String> topics = new ArrayList<>();
    private ProfileDetection detection;
    private double MESSAGE_RANGE = 15000;


    public MqttDetection(Context con) {
        context = con;
        this.broker = "tcp://iot.eclipse.org:1883";
        this.clientId = MqttAsyncClient.generateClientId();
        this.topic = "SocialExercise";
        topics.add(topic);
        detection = App.getProfileDetection();
        detection.addOnProfileChangedListener(this);
        person = detection.getProfile();
        person.mqttID = clientId;
        setup();
    }

    public boolean isConnected() {
        return connected;
    }

    public synchronized void connect() {
        Log.i("MQTT","Connect...");
        if(!connected) {
            setup();
        }
    }

    private void setup(){
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            sampleClient = new MqttAsyncClient(this.broker, this.clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            sampleClient.setCallback(new MqttCallback() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.i("MQTT", "Message arrived" + message.toString());
                    String msg = message.toString();
                    String[] split = msg.split(";");

                    //check if message or position update
                    if(split[0].equals("Position")){
                        String id = split[3];
                        Person person = new Person();
                        person.latitude = Double.parseDouble(split[1]);
                        person.longitude = Double.parseDouble(split[2]);
                        if(Helper.isInRange(getOwnPosition(), person.latitude, person.longitude, MESSAGE_RANGE)) {
                            person.mqttID = split[3];
                            person.isMale = Boolean.parseBoolean(split[4]);
                            person.avatar = Integer.parseInt(split[5]);
                            person.firstName = split[6];
                            person.lastName = split[7];
                            person.address = split[8];
                            person.favouriteActivities = split[9];

                            fireOnPositionReceived(person);
                        }
                    }else{
                        //create Message
                        Message m = new Message();
                        if(split.length >= 5){
                            m.latitude = Double.parseDouble(split[0]);
                            m.longitude = Double.parseDouble(split[1]);
                            m.id = split[2];
                            m.sender = split[3];
                            m.message = split[4];
                           /* for(int i = 5; i < split.length; i++) {
                                m.message = m.message + ";" + split[i];
                            }*/
                        }
                        m.topic = topic;
                        m.time = String.valueOf(getCurrentTimeStamp());
                        if(Helper.isInRange(getOwnPosition(), m.latitude, m.longitude, MESSAGE_RANGE)) {
                            saveMessageInMemory(m);
                            fireOnMessageReceived(m);
                        }
                    }

                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i("MQTT", "Delivery Complete");
                }

                public void connectionLost(Throwable cause) {
                    Log.e("MQTT", "Lost Connection", cause);
                }
            });

            sampleClient.connect(connOpts, null, new IMqttActionListener() {

                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("MQTT","Connection succeed");
                    subscribeToTopic(topic, qos);
                    connected = true;
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("MQTT", "Connection failed");
                    connected = false;
                }
            });

            App.getPositionDetection().addPositionEventListener(this);

        } catch (MqttException me) {
            Log.e("MQTT", me.getMessage());
        }
    }

    private void saveMessageInMemory(Message m){
        List<Message> msgs = memory.get(m.topic);
        if(msgs == null) {
            msgs = new ArrayList<>();
            memory.put(m.topic, msgs);
        }
        msgs.add(m);
    }

    private void subscribeToTopic(String topic, int qos) {
        try {
            Log.i("MQTT", "Subscribe to topic:" + topic);
            sampleClient.subscribe(topic, qos);
        } catch (MqttException e) {
            Log.e("MQTT", e.getMessage());
        }
    }

    @Override
    public void sendPosition(LatLng position){
        this.connect();

        Log.i("MQTT", "Sending pos");
        String p = "Position;" + Double.toString(position.latitude)+";"+Double.toString(position.longitude)+";"+clientId
                +";"+ Boolean.toString(person.isMale)
                +";"+ Integer.toString(person.avatar)
                +";"+ (person.firstName.equals("")?"Anonymous":person.firstName)
                +";"+ (person.lastName.equals("")?"Muster":person.lastName)
                +";"+ (person.address.equals("")?"Rainbowland":person.address)
                +";"+ (person.favouriteActivities.equals("")?"Everything":person.favouriteActivities);
        Log.i("MQTT", "Send Position" + p);
        MqttMessage message = new MqttMessage(p.getBytes());
        message.setQos(qos);
        try {
            IMqttDeliveryToken publish = sampleClient.publish(topic, message);
            publish.getMessage();
        } catch (MqttException e) {
            Log.e("MQTT", e.getMessage());
        }

    }

    @Override
    public void sendMessage(String msg) {
        if (!connected) {
            new MqttDetection(context);
        }
        String m = Double.toString(position.latitude)+";"+Double.toString(position.longitude)+";"+clientId+";"+person.firstName+";"+msg;
        Log.i("MQTT", "Sending msg to" + topic);
        MqttMessage message = new MqttMessage(m.getBytes());
        message.setQos(qos);
        try {
            IMqttDeliveryToken publish = sampleClient.publish(topic, message);
            publish.getMessage();
        } catch (MqttException e) {
            Log.e("MQTT", e.getMessage());
        }
    }

    public synchronized void close() {
        if (connected) {
            try {
                sampleClient.disconnect();
                Log.i("MQTT","Close connection");
            } catch (MqttException e) {
                Log.e("MQTT", e.getMessage());
            }
            App.getPositionDetection().removePositionEventListener(this);
        }

    }

    @Override
    public List<Message> setTopic(String topic) {
        if (topic.equals("SocialExercise")) {
            this.topic = topic;
        } else {
            this.topic = "SocialExercise" + topic;
            subscribeToTopic(this.topic, qos);
        }
        return memory.get(this.topic);
    }

    @Override
    public void addOnMessageReceivedListener(OnMessageReceivedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeOnMessageReceivedListener(OnMessageReceivedListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void addOnPositionReceivedListener(OnPositionReceivedListener listener) {
        listenersPos.add(listener);
    }

    @Override
    public void removeOnPositionReceivedListener(OnPositionReceivedListener listener) {
        listenersPos.remove(listener);
    }

    private void fireOnMessageReceived(Message msg) {
        Log.i("Got Message", msg.message);
        for (OnMessageReceivedListener listener : listeners) {
            try {
                listener.messageRecieved(msg);
            } catch (Exception e) {
                Log.e("MQTT", e.getMessage());
            }
        }
    }

    private void fireOnPositionReceived(Person person) {
        Log.i("MQTT","Got Position");
        for (OnPositionReceivedListener listener : listenersPos) {
            try {
                listener.positionRecieved(person);
            } catch (Exception e) {
                Log.e("MQTT", e.getMessage());
            }
        }
    }


    public void addTopic(String topic) {
        topics.add(topic);
    }

    public void removeTopic(String topic) { topics.remove(topic); }
    public List<String> getTopics(){ return topics;}

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }


    public String getClientId(){
        return  clientId;
    }

    @Override
    public void onLocationChanged(LatLng position) {
        person.latitude = position.latitude;
        person.longitude = position.longitude;
        this.position = position;
        if(connected)
            this.sendPosition(position);
    }

    public LatLng getOwnPosition() {
        return position;
    }

    @Override
    public void profileChanged(Person p) {
        this.person = p;
    }
}
