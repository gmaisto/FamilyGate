package com.gmaisto.familygate;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by gmaisto on 14/02/16.
 */
public class Utils {
    public static String getFormattedTime(String dt) {
        String reltime = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSSSSSSSS Z z", Locale.ITALIAN);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        try {
            SimpleDateFormat simple = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ITALIAN);
            String test = dateFormat.format(new Date());
            Date date = dateFormat.parse(dt);
            reltime = simple.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reltime;
    }
}
