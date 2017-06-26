package com.embedded.socialexercise.gui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.embedded.socialexercise.App;
import com.embedded.socialexercise.R;
import com.embedded.socialexercise.person.Person;
import com.embedded.socialexercise.person.ProfileDetection;

/**
 * Created by Verena on 26.06.2017.
 */

public class ProfileActivity extends BasicMenuActivity{

    ProfileDetection detection;
    Person p = new Person();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setup(R.id.nav_profile);
        detection = App.getProfileDetection();
        p = detection.getProfile();

        EditText txt1 = (EditText) findViewById(R.id.txtFirstName);
        txt1.setText(p.firstName);
        EditText txt2 = (EditText) findViewById(R.id.txtLastName);
        txt2.setText(p.lastName);
        EditText txt3 = (EditText) findViewById(R.id.txtAddress);
        txt3.setText(p.address);
        EditText txt4 = (EditText) findViewById(R.id.txtAbout);
        txt4.setText(p.favouriteActivities);


        Switch s = (Switch) findViewById(R.id.isMale);
        s.setChecked(!p.isMale);

        ImageView img = (ImageView) findViewById(R.id.imgAvatar);
        if(p.isMale){
            img.setImageResource(R.drawable.boy0);
        }else{
            img.setImageResource(R.drawable.girl0);
        }
    }

    public void onSendMsgClick(View v) {
        EditText txt1 = (EditText) findViewById(R.id.txtFirstName);
        String firstName = txt1.getText().toString();
        EditText txt2 = (EditText) findViewById(R.id.txtLastName);
        String lastName = txt2.getText().toString();
        EditText txt3 = (EditText) findViewById(R.id.txtAddress);
        String address = txt3.getText().toString();
        EditText txt4 = (EditText) findViewById(R.id.txtAbout);
        String about = txt4.getText().toString();

        Switch s = (Switch) findViewById(R.id.isMale);
        Boolean isMale = !s.isChecked();

        ImageView img = (ImageView) findViewById(R.id.imgAvatar);
        if(isMale){
            img.setImageResource(R.drawable.boy0);
        }else{
            img.setImageResource(R.drawable.girl0);
        }

        Person p = new Person();
        p.firstName = firstName;
        p.lastName = lastName;
        p.address = address;
        p.favouriteActivities =  about;
        p.isMale = isMale;

        Log.i("Profile",p.firstName);
        detection.changeProfile(p);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}
