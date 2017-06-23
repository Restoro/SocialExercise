package com.embedded.socialexercise.gui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;


import com.embedded.socialexercise.R;
import com.embedded.socialexercise.events.OnMessageReceivedListener;
import com.embedded.socialexercise.mqtt.IMqtt;
import com.embedded.socialexercise.mqtt.Message;
import com.embedded.socialexercise.mqtt.MqttForTesting;
import com.embedded.socialexercise.mqtt.MqttImpl;

public class ChatActivity extends AppCompatActivity implements OnMessageReceivedListener, AdapterView.OnItemSelectedListener {

    private LinearLayout contMsgs;
    private ScrollView scrV;
    private IMqtt mqtt;
    private String sender = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        scrV = (ScrollView) findViewById(R.id.scrvMsgs);
        contMsgs = (LinearLayout) findViewById(R.id.contMsgs);
        mqtt = new MqttImpl();
        mqtt.addOnMessageReceivedListener(this);
        Spinner spinner = (Spinner) findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.chat_choices, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);


        final EditText txt = new EditText(this);
        txt.setHint("USERNAME");
        new AlertDialog.Builder(this)
            .setMessage("   ENTER YOUR USERNAME HERE!")
            .setView(txt)
            .setPositiveButton("ENTER", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int whichButton) {
                  String name = txt.getText().toString();
                  setSender(name);
               }
            }).show();


    }

    private void setSender(String name){
        this.sender = name;
        ((MqttImpl)mqtt).setSender(sender);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((MqttImpl)mqtt).close();
        mqtt.removeOnMessageReceivedListener(this);
    }

    public void onSendMsgClick(View v) {
        EditText txt = (EditText) findViewById(R.id.txtNewMsg);
      //  TextView name = (TextView) findViewById(R.id.txtMsgName);
        String msg = txt.getText().toString();
       // String sender = name.getText().toString();
        if(!msg.equals("")) {
            addMessageView(getMyMessageView(mqtt.sendMessage(msg)));
            txt.setText(new char[0], 0, 0);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        contMsgs.removeAllViews();
        for(Message msg : mqtt.setTopic((String) adapterView.getItemAtPosition(i))){
            contMsgs.addView(getMessageView(msg));
        }
        scrollToBottom();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void messageRecieved(Message msg) {
        if(!sender.equals(msg.sender)){
            final View v = getMessageView(msg);
            contMsgs.post(new Runnable() {
                @Override
                public void run() {
                    addMessageView(v);
                }
            });
        }
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

    private void addMessageView(View msg){
        contMsgs.addView(msg);
        scrollToBottom();
    }

    private void scrollToBottom(){
        scrV.post(new Runnable() {

            @Override
            public void run() {
                scrV.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
