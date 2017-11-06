package com.isaac.smartdrawer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Isaac on 2016/9/3 0003.
 */
public class DateFactory {
    private static final SimpleDateFormat sDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat sTimeFormatter = new SimpleDateFormat("HH:mm", Locale.US);

    public static String dateToDateTime(Date date) {
        return sDateTimeFormatter.format(date);
    }

    public static String dateToTime(Date date) {
        return sTimeFormatter.format(date);
    }

    public static Date toDate(String string) {
        Date date = null;
        try {
            date = sDateTimeFormatter.parse(string);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return date;
    }
}
