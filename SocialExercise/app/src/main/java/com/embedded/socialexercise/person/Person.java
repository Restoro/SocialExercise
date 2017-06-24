package com.embedded.socialexercise.person;

import com.embedded.socialexercise.R;

import java.util.List;

public class Person {
    public boolean isMale;
    public String mqttID;
    public int avatar;
    public float longitude;
    public float latitude;
    public String firstName;
    public String lastName;
    public String address;
    public List<String> favouriteActivities;

    public int getAvatarId() {
        if(isMale) {
            switch (avatar){
                case 0: return R.drawable.boy0;
                case 1: return R.drawable.boy1;
                case 2: return R.drawable.boy2;
                case 3: return R.drawable.boy3;
                case 4: return R.drawable.boy4;
                case 5: return R.drawable.boy5;
                case 6: return R.drawable.boy6;
                case 7: return R.drawable.boy7;
                case 8: return R.drawable.boy8;
                default: return R.drawable.boy9;
            }
        } else {
            switch (avatar) {

                case 0: return R.drawable.girl0;
                case 1: return R.drawable.girl1;
                case 2: return R.drawable.girl2;
                case 3: return R.drawable.girl3;
                case 4: return R.drawable.girl4;
                case 5: return R.drawable.girl5;
                case 6: return R.drawable.girl6;
                case 7: return R.drawable.girl7;
                case 8: return R.drawable.girl8;
                default: return R.drawable.girl9;
            }
        }
    }
}
