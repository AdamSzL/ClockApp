package com.example.clockapp.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Helpers {
    public static String convertMillisToTime(long ms, boolean withMs) {
        Date date = new Date(ms);
        DateFormat formatter;
        if (withMs) {
            formatter = new SimpleDateFormat("HH:mm:ss.SS");
        } else {
            formatter = new SimpleDateFormat("HH:mm:ss");
        }
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateFormatted = formatter.format(date);
        return dateFormatted;
    }

    public static String formatIntegerToTwoDigits(int value) {
        if (value <= 9) {
            return String.format("0%d", value);
        }
        return String.valueOf(value);
    }

    public static long convertTimeToMs(int hour, int minute) {
        String formattedHour = formatIntegerToTwoDigits(hour);
        String formattedMinute = formatIntegerToTwoDigits(minute);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = null;
        try {
            date = sdf.parse(formattedHour + ":" + formattedMinute);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (date == null) ? 0 : date.getTime();
    }

    public static long convertTimeToMs(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SS");
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (date == null) ? 0 : date.getTime();
    }
}
