package com.embedded.socialexercise.gui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
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
import com.google.android.gms.maps.model.LatLng;

public class ChatActivity extends BasicMenuActivity implements OnMessageReceivedListener, OnPositionReceivedListener, AdapterView.OnItemSelectedListener {
    private MqttDetection detection;
    private LinearLayout contMsgs;
    private ScrollView scrV;
    private String sender = "Anonymous";
    private LatLng position = new LatLng(0.0,0.0);
    private double MESSAGE_RANGE = 0.3d;

    @Override
    protected void onStart() {
        super.onStart();
        registerForMqtt();
    }

    private void registerForMqtt(){
        detection = App.getMqttDetection();
        if (detection != null) {
            detection.addOnPositionReceivedListener(this);
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
            detection.removeOnPositionReceivedListener(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setup(R.id.nav_chat);
        scrV = (ScrollView) findViewById(R.id.scrvMsgs);
        contMsgs = (LinearLayout) findViewById(R.id.contMsgs);
        Spinner spinner = (Spinner) findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.chat_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        final EditText txt = new EditText(this);
        txt.setHint("Anonymous");
        new AlertDialog.Builder(this)
                .setMessage("   ENTER YOUR USERNAME HERE!")
                .setView(txt)
                .setPositiveButton("ENTER", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sender = txt.getText().toString();
                    }
                }).show();
    }

    public void onSendMsgClick(View v) {
        EditText txt = (EditText) findViewById(R.id.txtNewMsg);
        String msg = txt.getText().toString();
        if (!msg.equals("")) {
            detection.setSender(sender);
            detection.sendMessage(msg);
            txt.setText(new char[0], 0, 0);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Object o = menu.getItem(0);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        contMsgs.removeAllViews();
        for (Message msg : detection.setTopic((String) adapterView.getItemAtPosition(i))) {
            contMsgs.addView(getMessageView(msg));
        }
        scrollToBottom();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void messageRecieved(Message msg) {
       if (isInRange(msg)) {
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
        return ((Math.abs(msg.latitude-this.position.latitude) <= MESSAGE_RANGE)&& (Math.abs(msg.longitude-this.position.longitude) < MESSAGE_RANGE));
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

    @Override
    public void positionRecieved(LatLng position, String id) {
        if(detection.getClientId().equals(id)){
            this.position = position;
        }
        this.position = position;
    }

}
