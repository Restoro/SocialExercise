package com.embedded.socialexercise.gui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.embedded.socialexercise.App;
import com.embedded.socialexercise.R;
import com.embedded.socialexercise.events.OnMessageReceivedListener;
import com.embedded.socialexercise.events.OnPositionReceivedListener;
import com.embedded.socialexercise.mqtt.Message;
import com.embedded.socialexercise.mqtt.MqttDetection;
import com.embedded.socialexercise.person.Person;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class ChatActivity extends BasicMenuActivity implements OnMessageReceivedListener, AdapterView.OnItemSelectedListener {
    private MqttDetection detection;
    private LinearLayout contMsgs;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;
    private ScrollView scrV;
    private LatLng position = new LatLng(0.0,0.0);
    private double MESSAGE_RANGE = 50000;
    private String curTopic = "SocialExercise";

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerForMqtt();
        if(adapter==null){
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, detection.getTopics());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }

    private void registerForMqtt(){
        detection = App.getMqttDetection();
        if (detection != null) {
            detection.addOnMessageReceivedListener(this);
            position = detection.getOwnPosition();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        unregisterForMqtt();
    }

    private void unregisterForMqtt() {
        if (detection != null) {
            detection.removeOnMessageReceivedListener(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setup(R.id.nav_chat);
        scrV = (ScrollView) findViewById(R.id.scrvMsgs);
        contMsgs = (LinearLayout) findViewById(R.id.contMsgs);
        spinner = (Spinner) findViewById(R.id.spinner3);
        spinner.setOnItemSelectedListener(this);
    }

    public void onSendMsgClick(View v) {
        EditText txt = (EditText) findViewById(R.id.txtNewMsg);
        String msg = txt.getText().toString();
        if (!msg.equals("")) {
            detection.sendMessage(msg);
            txt.setText(new char[0], 0, 0);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        contMsgs.removeAllViews();
        String topicName = (String) adapterView.getItemAtPosition(i);
        List<Message> msgs = detection.setTopic(topicName);
        curTopic = topicName;
        if (!curTopic.equals("SocialExercise")) {
            curTopic = "SocialExercise" + curTopic;
        }
        if(msgs!=null) {
            for (Message msg : msgs) {
                if(msg.id.equals(detection.getClientId())){
                    contMsgs.addView(getMyMessageView(msg));
                } else {
                    contMsgs.addView(getMessageView(msg));
                }
            }
        }
        scrollToBottom();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void messageRecieved(Message msg) {
        Log.i("Message", "Cur:" + curTopic + " Recieved:" + msg.topic);

       if (isInRange(msg) && curTopic.equals(msg.topic)) {
            final View v;
            if((detection.getClientId()).equals(msg.id)){
                v = getMyMessageView(msg);
            }else{
                v = getMessageView(msg);
            }
            contMsgs.post(new Runnable() {
                @Override
                public void run() {
                    addMessageView(v);
                }
            });
        }
    }

    private boolean isInRange(Message msg){
        float[] result = {0};
        Location.distanceBetween(position.latitude, position.longitude, msg.latitude, msg.longitude, result);
        return result[0]<MESSAGE_RANGE;
    }

    private View getMessageView(Message msg) {
        View v = getLayoutInflater().inflate(R.layout.text_message, null);
        ((TextView) v.findViewById(R.id.txtMsgName)).setText(msg.sender);
        ((TextView) v.findViewById(R.id.txtMsgTime)).setText(msg.time);
        ((EditText) v.findViewById(R.id.txtMsg)).setText(msg.message);
        return v;
    }

    private View getMyMessageView(Message msg) {
        View v = getLayoutInflater().inflate(R.layout.my_text_message, null);
        ((TextView) v.findViewById(R.id.txtMyMsgName)).setText(msg.sender);
        ((TextView) v.findViewById(R.id.txtMyMsgTime)).setText(msg.time);
        ((EditText) v.findViewById(R.id.txtMyMsg)).setText(msg.message);
        return v;
    }

    private void addMessageView(View msg) {
        contMsgs.addView(msg);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrV.post(new Runnable() {

            @Override
            public void run() {
                scrV.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

}
