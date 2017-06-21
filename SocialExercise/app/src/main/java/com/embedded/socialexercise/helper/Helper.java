package com.embedded.socialexercise.helper;

/**
 * Created by hoellinger on 21.06.2017.
 */

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
}
