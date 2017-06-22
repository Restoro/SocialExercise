package com.embedded.socialexercise.helper;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class Helper {
    private static final StringBuilder SB = new StringBuilder();

    public static String iterableToString(Iterable<? extends String> it){
        SB.setLength(0);
        for(String s : it) {
            SB.append(s).append(", ");
        }
        //remove last ", "
        SB.setLength(SB.length()-2);
        return SB.toString();
    }

    //Needs permission! Make sure to request these before calling this method
    public static void writeToFile(String fileName, String msg) {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard,"socialExercise");

            File file = new File(dir, fileName);
            FileOutputStream f = new FileOutputStream(file, true);

            try {
                f.write(msg.getBytes());
                f.flush();
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean inRange(float number, float range, float offset) {
        return range-offset <= number && number <= range+offset;
    }
}
